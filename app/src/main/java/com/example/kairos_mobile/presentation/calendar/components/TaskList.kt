package com.example.kairos_mobile.presentation.calendar.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.model.TodoPriority
import com.example.kairos_mobile.presentation.components.common.SectionHeader
import com.example.kairos_mobile.presentation.components.common.SwipeableCard
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * TaskList 컴포넌트 (Reference 디자인)
 * 할 일 목록 (드래그 핸들, 체크박스, 스와이프 삭제)
 */
@Composable
fun TaskList(
    tasks: List<Todo>,
    onTaskClick: (Todo) -> Unit,
    onTaskComplete: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Column(modifier = modifier) {
        SectionHeader(title = "할 일")

        if (tasks.isEmpty()) {
            TaskEmptyState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tasks.forEach { task ->
                    SwipeableCard(
                        onDismiss = { onTaskDelete(task.id) }
                    ) {
                        TaskItem(
                            task = task,
                            onClick = { onTaskClick(task) },
                            onToggleComplete = { onTaskComplete(task.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 할 일 아이템 (Reference 디자인)
 */
@Composable
private fun TaskItem(
    task: Todo,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val alpha = if (task.isCompleted) 0.6f else 1f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .alpha(alpha)
            .padding(12.dp, 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 드래그 핸들
            DragHandle()

            Spacer(modifier = Modifier.width(12.dp))

            // 내용
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 제목 + 카테고리 chip (있는 경우)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title ?: task.content,
                        color = colors.text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // 카테고리 표시 (우선순위로 대체)
                    if (task.priority != TodoPriority.NONE) {
                        Spacer(modifier = Modifier.width(8.dp))
                        PriorityChip(priority = task.priority)
                    }
                }

                // 마감 시간 (있는 경우)
                task.dueTime?.let { time ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "마감 ${String.format("%02d:%02d", time.hour, time.minute)}",
                        color = if (task.isOverdue()) colors.danger else colors.danger,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 체크박스 (둥근 사각형)
            TaskCheckbox(
                isChecked = task.isCompleted,
                onToggle = onToggleComplete
            )
        }
    }
}

/**
 * 드래그 핸들 (Reference 디자인)
 */
@Composable
private fun DragHandle(
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(2) {
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(colors.textMuted.copy(alpha = 0.3f))
            )
        }
    }
}

/**
 * 커스텀 체크박스 (둥근 사각형 - Reference 디자인)
 */
@Composable
private fun TaskCheckbox(
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

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

    Box(
        modifier = modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable { onToggle() },
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

/**
 * 우선순위 Chip (Reference 디자인)
 */
@Composable
private fun PriorityChip(
    priority: TodoPriority,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    val (text, bgColor, textColor) = when (priority) {
        TodoPriority.HIGH -> Triple("Work", colors.chipBg, colors.chipText)
        TodoPriority.MEDIUM -> Triple("Work", colors.chipBg, colors.chipText)
        TodoPriority.LOW -> Triple("Personal", colors.chipBg, colors.chipText)
        TodoPriority.NONE -> return
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 할 일 없음 상태
 */
@Composable
private fun TaskEmptyState(
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

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
