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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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

    // 48dp 터치 영역으로 감싸고, 시각적 크기 22dp 유지
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
