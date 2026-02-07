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
import androidx.compose.material.icons.filled.DeleteOutline
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
import com.example.kairos_mobile.presentation.calendar.TodoDisplayItem
import com.example.kairos_mobile.presentation.components.common.SectionHeader
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * TaskList 컴포넌트
 * 할 일 목록 (체크박스 토글)
 */
@Composable
fun TaskList(
    tasks: List<TodoDisplayItem>,
    onTaskComplete: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    completedTasks: List<TodoDisplayItem> = emptyList(),
    showCompleted: Boolean = false,
    onToggleShowCompleted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
                    TaskItem(
                        task = task,
                        onToggleComplete = { onTaskComplete(task.todoId) },
                        onDelete = { onTaskDelete(task.captureId) }
                    )
                }
            }
        }

        // 완료 항목 토글
        val completedCount = completedTasks.size
        if (completedCount > 0 || showCompleted) {
            val colors = KairosTheme.colors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clickable { onToggleShowCompleted() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showCompleted) "완료 항목 숨기기" else "완료 항목 ($completedCount)",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (showCompleted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    completedTasks.forEach { task ->
                        TaskItem(
                            task = task,
                            onToggleComplete = { onTaskComplete(task.todoId) },
                            onDelete = { onTaskDelete(task.captureId) }
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
    task: TodoDisplayItem,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
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
            .alpha(alpha)
            .padding(12.dp, 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 내용
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 제목
                Text(
                    text = task.title,
                    color = colors.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )

                // 마감일 (있는 경우)
                task.deadline?.let { deadlineMs ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val deadlineText = formatDeadline(deadlineMs)
                        val isOverdue = deadlineMs < System.currentTimeMillis() && !task.isCompleted
                        Text(
                            text = deadlineText,
                            color = if (isOverdue) colors.danger else colors.textSecondary,
                            fontSize = 12.sp
                        )
                        // AI 마감일 배지
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

            // 체크박스 (둥근 사각형)
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
