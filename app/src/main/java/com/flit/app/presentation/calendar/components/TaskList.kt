package com.flit.app.presentation.calendar.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
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
import kotlin.math.roundToInt

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
                            isDragging = isDragging,
                            onToggleComplete = { onTaskComplete(task.todoId) },
                            onTaskDelete = { onTaskDelete(task.captureId) },
                            onTaskClick = { onTaskClick(task.captureId) },
                            onDeadlineEdit = onDeadlineEdit,
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
                                // 가변 높이 기반 슬롯 계산
                                val avgHeight = if (itemHeights.isNotEmpty()) {
                                    itemHeights.values.average().toFloat()
                                } else 60f
                                val slotHeight = avgHeight + spacingPx
                                val targetIndex = (currentIndex + (dragOffsetY / slotHeight).roundToInt())
                                    .coerceIn(0, currentList.size - 1)
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
                                            val deltaY = (oldIndex - newIndex) * slotHeight
                                            val anim = placementAnimations[id] ?: return@forEach
                                            scope.launch {
                                                anim.snapTo(deltaY)
                                                anim.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = tween(durationMillis = 350)
                                                )
                                            }
                                        }
                                    }
                                    // 슬롯 이동만큼 오프셋을 차감해 드래그 연속성을 유지
                                    dragOffsetY -= movedSlots * slotHeight
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
}

/**
 * 할 일 아이템 (아이템 전체에서 long press → 드래그 순서 변경)
 */
@Composable
private fun TaskItemWithDragHandle(
    task: TodoDisplayItem,
    isDragging: Boolean = false,
    onToggleComplete: () -> Unit,
    onTaskDelete: () -> Unit,
    onTaskClick: () -> Unit,
    onDeadlineEdit: (String, Long) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    var isExpanded by remember { mutableStateOf(false) }

    // 원문이 제목과 다를 때만 표시
    val showOriginalText = task.originalText != null &&
            task.originalText != task.title &&
            task.originalText.length > task.title.length

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
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column {
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis
                    )

                    task.deadline?.let { deadlineMs ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val deadlineText = formatDeadline(deadlineMs)
                            val isOverdue = deadlineMs < System.currentTimeMillis()
                            Text(
                                text = deadlineText,
                                color = when {
                                    task.isCompleted -> colors.textMuted
                                    isOverdue -> colors.danger
                                    else -> colors.textSecondary
                                },
                                fontSize = 12.sp
                            )
                            if (task.deadlineSource == "AI") {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI",
                                    color = colors.accent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(colors.accent.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                TaskCheckbox(
                    isChecked = task.isCompleted,
                    onToggle = onToggleComplete
                )
            }

            // 확장 영역: 캡처 원문 + 마감일 편집 + 상세 보기 + 삭제
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    // 1. 캡처 원문 (AI 제목과 다를 때만)
                    if (showOriginalText) {
                        Text(
                            text = task.originalText!!,
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 2. 마감일 행
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = task.deadline?.let { formatDeadline(it) } ?: "마감일 없음",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "마감일 편집",
                            tint = colors.textMuted,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    // DatePicker는 CalendarScreen에서 처리
                                    onDeadlineEdit(
                                        task.todoId,
                                        task.deadline ?: System.currentTimeMillis()
                                    )
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. 하단 액션 행: 상세 보기 + 삭제
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "상세 보기",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onTaskClick() }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "삭제",
                            color = colors.danger,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onTaskDelete() }
                        )
                    }
                }
            }
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

    // 48dp 터치 영역, 24dp 시각적 크기
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
                .size(24.dp)
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
