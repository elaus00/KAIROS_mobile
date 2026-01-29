package com.example.kairos_mobile.presentation.calendar.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.kairos_mobile.ui.components.kairosCard
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * TaskList 컴포넌트 (PRD v4.0)
 * 할 일 목록 (드래그 핸들, 체크박스, 스와이프 삭제)
 *
 * @param tasks 할 일 목록
 * @param onTaskClick 할 일 클릭 콜백
 * @param onTaskComplete 완료 토글 콜백
 * @param onTaskDelete 삭제 콜백
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
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = tasks,
                    key = { it.id }
                ) { task ->
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
 * 할 일 아이템
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
            .kairosCard()
            .clickable { onClick() }
            .alpha(alpha)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 드래그 핸들
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "정렬",
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 내용
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 제목
                Text(
                    text = task.title ?: task.content,
                    color = colors.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )

                // 마감일/시간 (있는 경우)
                if (task.dueDate != null || task.dueTime != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        task.dueTime?.let { time ->
                            Text(
                                text = String.format("%02d:%02d", time.hour, time.minute),
                                color = if (task.isOverdue()) colors.danger else colors.textMuted,
                                fontSize = 12.sp
                            )
                        }
                        if (task.dueTime != null && task.dueDate != null) {
                            Text(
                                text = " · ",
                                color = colors.textMuted,
                                fontSize = 12.sp
                            )
                        }
                        task.dueDate?.let { date ->
                            Text(
                                text = "${date.monthValue}/${date.dayOfMonth}",
                                color = if (task.isOverdue()) colors.danger else colors.textMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // 우선순위 표시
                if (task.priority != TodoPriority.NONE) {
                    Spacer(modifier = Modifier.height(4.dp))
                    PriorityIndicator(priority = task.priority)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 체크박스
            TaskCheckbox(
                isChecked = task.isCompleted,
                onToggle = onToggleComplete
            )
        }
    }
}

/**
 * 커스텀 체크박스
 */
@Composable
private fun TaskCheckbox(
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    val iconColor by animateColorAsState(
        targetValue = if (isChecked) colors.accent else colors.textMuted,
        animationSpec = tween(durationMillis = 200),
        label = "iconColor"
    )

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isChecked) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = if (isChecked) "완료됨" else "미완료",
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 우선순위 표시
 */
@Composable
private fun PriorityIndicator(
    priority: TodoPriority,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    val (text, bgColor) = when (priority) {
        TodoPriority.HIGH -> "높음" to colors.danger.copy(alpha = 0.1f)
        TodoPriority.MEDIUM -> "중간" to colors.chipBg
        TodoPriority.LOW -> "낮음" to colors.chipBg
        TodoPriority.NONE -> return
    }

    val textColor = when (priority) {
        TodoPriority.HIGH -> colors.danger
        else -> colors.chipText
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
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

/**
 * 간단한 할 일 아이템 (드래그 핸들 없음)
 */
@Composable
fun SimpleTaskItem(
    task: Todo,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val alpha = if (task.isCompleted) 0.6f else 1f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .alpha(alpha)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 체크박스
        TaskCheckbox(
            isChecked = task.isCompleted,
            onToggle = onToggleComplete
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 내용
        Text(
            text = task.title ?: task.content,
            color = colors.text,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f)
        )

        // 마감 시간
        task.dueTime?.let { time ->
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%02d:%02d", time.hour, time.minute),
                color = if (task.isOverdue()) colors.danger else colors.textMuted,
                fontSize = 12.sp
            )
        }
    }
}
