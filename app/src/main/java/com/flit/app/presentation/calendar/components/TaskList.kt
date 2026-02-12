package com.flit.app.presentation.calendar.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.flit.app.ui.theme.FlitTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * TaskList 컴포넌트
 * 할 일 목록 (드래그 순서 변경 + 체크박스 토글 + 완료 항목 보기)
 */
@Composable
fun TaskList(
    tasks: List<TodoDisplayItem>,
    completedTasks: List<TodoDisplayItem>,
    showCompleted: Boolean,
    onTaskComplete: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    onReorder: (List<String>) -> Unit,
    onToggleShowCompleted: () -> Unit,
    onTaskAction: (TodoDisplayItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeader(title = "할 일", fontSize = 15.sp)

        if (tasks.isEmpty() && completedTasks.isEmpty()) {
            TaskEmptyState()
        } else {
            // 미완료 할 일 — 드래그 순서 변경 지원
            DraggableTaskList(
                tasks = tasks,
                onTaskComplete = onTaskComplete,
                onReorder = onReorder,
                onTaskAction = onTaskAction
            )

            // 완료 항목 토글
            if (completedTasks.isNotEmpty()) {
                CompletedTasksToggle(
                    completedTasks = completedTasks,
                    showCompleted = showCompleted,
                    onToggle = onToggleShowCompleted,
                    onTaskComplete = onTaskComplete
                )
            }
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
    onTaskAction: (TodoDisplayItem) -> Unit = {}
) {
    val colors = FlitTheme.colors
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    // 드래그 상태 관리
    val currentList = remember(tasks) { mutableStateListOf(*tasks.toTypedArray()) }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var measuredItemHeight by remember { mutableFloatStateOf(0f) }
    val spacingPx = with(density) { 6.dp.toPx() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        currentList.forEachIndexed { index, task ->
            key(task.todoId) {
                val isDragging = draggingIndex == index

                Box(
                    modifier = Modifier
                        .zIndex(if (isDragging) 1f else 0f)
                        .onSizeChanged { size ->
                            if (measuredItemHeight == 0f) {
                                measuredItemHeight = size.height.toFloat()
                            }
                        }
                        .graphicsLayer {
                            if (isDragging) {
                                translationY = dragOffsetY
                                shadowElevation = 2f
                                scaleX = 1.01f
                                scaleY = 1.01f
                            }
                        }
                ) {
                    TaskItemWithDragHandle(
                        task = task,
                        isDragging = isDragging,
                        onToggleComplete = { onTaskComplete(task.todoId) },
                        onTaskAction = { onTaskAction(task) },
                        onDragStart = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            draggingIndex = index
                            dragOffsetY = 0f
                        },
                        onDrag = { deltaY ->
                            dragOffsetY += deltaY
                            val slotHeight = if (measuredItemHeight > 0f) measuredItemHeight + spacingPx else 60f
                            val targetIndex = (index + (dragOffsetY / slotHeight).roundToInt())
                                .coerceIn(0, currentList.size - 1)
                            if (targetIndex != index && targetIndex != draggingIndex) {
                                currentList.add(targetIndex, currentList.removeAt(draggingIndex))
                                draggingIndex = targetIndex
                                dragOffsetY = 0f
                            }
                        },
                        onDragEnd = {
                            draggingIndex = -1
                            dragOffsetY = 0f
                            onReorder(currentList.map { it.todoId })
                        }
                    )
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
    onTaskAction: () -> Unit = {},
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    var isExpanded by remember { mutableStateOf(false) }

    // 확장 가능 여부 판단
    val hasExpandableContent = task.deadline != null || task.title.length > 25

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
            .clickable {
                if (isExpanded) {
                    onTaskAction()
                } else if (hasExpandableContent) {
                    isExpanded = true
                } else {
                    onTaskAction()
                }
            }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 내용
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        color = colors.text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
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
                                color = if (isOverdue) colors.danger else colors.textSecondary,
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
                    isChecked = false,
                    onToggle = onToggleComplete
                )
            }

            AnimatedVisibility(visible = isExpanded && hasExpandableContent) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    // 확장 시 제목 전문 표시 (긴 제목일 때)
                    if (task.title.length > 25) {
                        Text(
                            text = task.title,
                            color = colors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 완료 항목 보기 토글 + 완료 항목 리스트
 */
@Composable
private fun CompletedTasksToggle(
    completedTasks: List<TodoDisplayItem>,
    showCompleted: Boolean,
    onToggle: () -> Unit,
    onTaskComplete: (String) -> Unit
) {
    val colors = FlitTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp)
    ) {
        // 토글 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onToggle() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (showCompleted)
                    Icons.Default.KeyboardArrowUp
                else
                    Icons.Default.KeyboardArrowDown,
                contentDescription = if (showCompleted) "완료 항목 숨기기" else "완료 항목 보기",
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "완료 항목 보기 (${completedTasks.size})",
                color = colors.textMuted,
                fontSize = 13.sp
            )
        }

        // 완료 항목 리스트
        AnimatedVisibility(
            visible = showCompleted,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                completedTasks.forEach { task ->
                    key(task.todoId) {
                        CompletedTaskItem(
                            task = task,
                            onToggleComplete = { onTaskComplete(task.todoId) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 완료된 할 일 아이템 (취소선 + 완료 일시)
 */
@Composable
private fun CompletedTaskItem(
    task: TodoDisplayItem,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card.copy(alpha = 0.6f))
            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp, 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = colors.textMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = TextDecoration.LineThrough
                )

                task.deadline?.let { deadlineMs ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatDeadline(deadlineMs),
                        color = colors.textMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            TaskCheckbox(
                isChecked = true,
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
        Text(
            text = "할 일이 없습니다",
            color = colors.textMuted,
            fontSize = 14.sp
        )
    }
}
