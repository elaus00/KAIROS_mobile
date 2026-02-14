package com.flit.app.presentation.calendar.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.flit.app.presentation.calendar.TodoDisplayItem
import com.flit.app.presentation.components.common.SectionHeader
import com.flit.app.presentation.components.common.SwipeableCard
import com.flit.app.ui.theme.FlitTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

/**
 * TaskList 컴포넌트
 * 할 일 목록 (드래그 순서 변경 + 체크박스 토글)
 */
@Composable
fun TaskList(
    tasks: List<TodoDisplayItem>,
    onTaskComplete: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    onReorder: (List<String>) -> Unit,
    onTaskClick: (String) -> Unit = {},
    onDeadlineEdit: (String, Long) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeader(title = "할 일", fontSize = 15.sp)

        if (tasks.isEmpty()) {
            TaskEmptyState()
        } else {
            // 할 일 — 드래그 순서 변경 지원
            DraggableTaskList(
                tasks = tasks,
                onTaskComplete = onTaskComplete,
                onReorder = onReorder,
                onTaskDelete = onTaskDelete,
                onTaskClick = onTaskClick,
                onDeadlineEdit = onDeadlineEdit
            )
        }
    }
}

/**
 * 드래그 순서 변경 가능한 할 일 목록
 */
@Composable
private fun DraggableTaskList(
    tasks: List<TodoDisplayItem>,
    onTaskComplete: (String) -> Unit,
    onReorder: (List<String>) -> Unit,
    onTaskDelete: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onDeadlineEdit: (String, Long) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val cardShape = RoundedCornerShape(12.dp)
    var editingTask by remember { mutableStateOf<TodoDisplayItem?>(null) }

    // 드래그 상태 관리
    val currentList = remember(tasks) { mutableStateListOf(*tasks.toTypedArray()) }
    var draggingTodoId by remember { mutableStateOf<String?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    // 가변 높이 대응: 아이템별 높이 Map
    val itemHeights = remember { mutableStateMapOf<String, Float>() }
    // 카드 간 위치 변경 시 부드러운 전환 애니메이션용 offset
    val placementAnimations = remember { mutableMapOf<String, Animatable<Float, AnimationVector1D>>() }
    val spacingPx = with(density) { 6.dp.toPx() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        currentList.forEach { task ->
            key(task.todoId) {
                val isDragging = draggingTodoId == task.todoId
                val placementAnim = remember(task.todoId) { Animatable(0f) }
                placementAnimations[task.todoId] = placementAnim

                Box(
                    modifier = Modifier
                        .zIndex(if (isDragging) 1f else 0f)
                        // 드래그 시 클리핑 방지를 위해 외부 clip 제거
                        .onSizeChanged { size ->
                            itemHeights[task.todoId] = size.height.toFloat()
                        }
                        .graphicsLayer {
                            shape = cardShape
                            clip = false
                            if (isDragging) {
                                translationY = dragOffsetY
                                shadowElevation = 0f
                            } else {
                                translationY = placementAnim.value
                                shadowElevation = 0f
                            }
                        }
                ) {
                    SwipeableCard(
                        onDismiss = { onTaskDelete(task.captureId) }
                    ) {
                        TaskItemWithDragHandle(
                            task = task,
                            onToggleComplete = { onTaskComplete(task.todoId) },
                            onTaskClick = {
                                onTaskClick(task.captureId)
                                editingTask = task
                            },
                            onDragStart = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = drag@{ deltaY ->
                                val currentIndex = currentList.indexOfFirst { it.todoId == task.todoId }
                                if (currentIndex < 0) return@drag

                                // 실제 이동이 시작될 때만 드래그 상태로 진입 (롱프레스 플래시 방지)
                                if (draggingTodoId == null) {
                                    draggingTodoId = task.todoId
                                    dragOffsetY = 0f
                                }

                                dragOffsetY += deltaY
                                // 누적 높이 기반 정확한 슬롯 계산
                                var accumulatedOffset = 0f
                                var targetIndex = currentIndex
                                if (dragOffsetY > 0) {
                                    // 아래로 드래그
                                    for (i in currentIndex + 1 until currentList.size) {
                                        val itemHeight = itemHeights[currentList[i].todoId] ?: 60f
                                        accumulatedOffset += itemHeight + spacingPx
                                        if (dragOffsetY >= accumulatedOffset) {
                                            targetIndex = i
                                        } else {
                                            break
                                        }
                                    }
                                } else if (dragOffsetY < 0) {
                                    // 위로 드래그
                                    for (i in currentIndex - 1 downTo 0) {
                                        val itemHeight = itemHeights[currentList[i].todoId] ?: 60f
                                        accumulatedOffset -= itemHeight + spacingPx
                                        if (dragOffsetY <= accumulatedOffset) {
                                            targetIndex = i
                                        } else {
                                            break
                                        }
                                    }
                                }
                                if (targetIndex != currentIndex) {
                                    val movedSlots = targetIndex - currentIndex
                                    val oldIndexById = currentList
                                        .mapIndexed { idx, item -> item.todoId to idx }
                                        .toMap()
                                    currentList.add(targetIndex, currentList.removeAt(currentIndex))
                                    val newIndexById = currentList
                                        .mapIndexed { idx, item -> item.todoId to idx }
                                        .toMap()
                                    oldIndexById.forEach { (id, oldIndex) ->
                                        if (id == task.todoId) return@forEach
                                        val newIndex = newIndexById[id] ?: return@forEach
                                        if (oldIndex != newIndex) {
                                            // 이동된 아이템의 실제 높이 사용
                                            val itemHeight = itemHeights[id] ?: 60f
                                            val slotHeight = itemHeight + spacingPx
                                            val deltaY = (oldIndex - newIndex) * slotHeight
                                            val anim = placementAnimations[id] ?: return@forEach
                                            scope.launch {
                                                anim.snapTo(deltaY)
                                                anim.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = tween(durationMillis = 300)
                                                )
                                            }
                                        }
                                    }
                                    // 이동한 만큼 오프셋 조정
                                    val movedDistance = if (movedSlots > 0) {
                                        // 아래로 이동: currentIndex+1 ~ targetIndex 아이템들의 높이 합
                                        (currentIndex + 1..targetIndex).sumOf { i ->
                                            (itemHeights[currentList[i].todoId] ?: 60f).toDouble() + spacingPx
                                        }.toFloat()
                                    } else {
                                        // 위로 이동: targetIndex ~ currentIndex-1 아이템들의 높이 합 (음수)
                                        -(targetIndex until currentIndex).sumOf { i ->
                                            (itemHeights[currentList[i].todoId] ?: 60f).toDouble() + spacingPx
                                        }.toFloat()
                                    }
                                    dragOffsetY -= movedDistance
                                }
                            },
                            onDragEnd = {
                                val didDrag = draggingTodoId != null
                                draggingTodoId = null
                                dragOffsetY = 0f
                                if (didDrag) {
                                    onReorder(currentList.map { it.todoId })
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    editingTask?.let { task ->
        TodoEditDialog(
            task = task,
            onDismiss = { editingTask = null },
            onDeadlineEdit = { todoId, deadlineMs ->
                onDeadlineEdit(todoId, deadlineMs)
                editingTask = null
            },
            onDelete = { captureId ->
                onTaskDelete(captureId)
                editingTask = null
            }
        )
    }
}

/**
 * 할 일 아이템 (아이템 전체에서 long press → 드래그 순서 변경)
 */
@Composable
private fun TaskItemWithDragHandle(
    task: TodoDisplayItem,
    onToggleComplete: () -> Unit,
    onTaskClick: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDrag = { change, offset ->
                        change.consume()
                        onDrag(offset.y)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
            .clickable { onTaskClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 내용
            Column(modifier = Modifier.weight(1f)) {
                val titleColor = if (task.isCompleted) colors.textMuted else colors.text
                Text(
                    text = task.title,
                    color = titleColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                task.deadline?.let { deadlineMs ->
                    Spacer(modifier = Modifier.height(4.dp))
                    val deadlineText = formatDeadline(deadlineMs)
                    val isOverdue = deadlineMs < System.currentTimeMillis()
                    Text(
                        text = deadlineText,
                        color = when {
                            task.isCompleted -> colors.textMuted
                            isOverdue -> colors.danger
                            else -> colors.textSecondary
                        },
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            TaskCheckbox(
                isChecked = task.isCompleted,
                onToggle = onToggleComplete
            )
        }
    }
}

/**
 * 커스텀 체크박스 (둥근 사각형)
 */
@Composable
private fun TaskCheckbox(
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    val backgroundColor by animateColorAsState(
        targetValue = if (isChecked) colors.accent else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isChecked) colors.accent else colors.border,
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    // 48dp 터치 영역, 22dp 시각적 크기
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .semantics {
                role = Role.Checkbox
                stateDescription = if (isChecked) "완료됨" else "미완료"
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(backgroundColor)
                .border(1.5.dp, borderColor, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "완료됨",
                    tint = if (colors.isDark) colors.background else Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * 마감일 포맷 (epoch ms → "M/d HH:mm 마감")
 */
private fun formatDeadline(epochMs: Long): String {
    val dateTime = Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("M/d HH:mm")
    return "${dateTime.format(formatter)} 마감"
}

/**
 * 할 일 없음 상태
 */
@Composable
private fun TaskEmptyState(
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.CheckBox,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "할 일이 없어요",
                color = colors.textMuted,
                fontSize = 14.sp
            )
        }
    }
}
