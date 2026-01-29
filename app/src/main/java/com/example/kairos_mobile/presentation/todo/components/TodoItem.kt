package com.example.kairos_mobile.presentation.todo.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.model.TodoPriority
import com.example.kairos_mobile.ui.components.glassPanelThemed
import com.example.kairos_mobile.ui.theme.*
import java.time.format.DateTimeFormatter

/**
 * 투두 아이템 컴포넌트
 */
@Composable
fun TodoItem(
    todo: Todo,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textSecondaryColor = if (isDarkTheme) TextSecondary else AiryTextSecondary
    val accentColor = if (isDarkTheme) PrimaryNavy else AiryAccentBlue

    // 완료 상태에 따른 애니메이션
    val contentAlpha by animateColorAsState(
        targetValue = if (todo.isCompleted) {
            textSecondaryColor.copy(alpha = 0.5f)
        } else {
            textPrimaryColor
        },
        animationSpec = tween(durationMillis = 200),
        label = "todoContentAlpha"
    )

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isDarkTheme) GlassCard.copy(alpha = 0.6f) else AiryGlassCard.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 체크박스
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = accentColor,
                    uncheckedColor = textSecondaryColor,
                    checkmarkColor = Color.White
                )
            )

            // 콘텐츠
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 제목/내용
                Text(
                    text = todo.title ?: todo.content,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentAlpha,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                // 마감일/시간
                if (todo.dueDate != null || todo.dueTime != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (todo.isOverdue()) {
                                if (isDarkTheme) ErrorRed else AiryErrorRed
                            } else {
                                textSecondaryColor
                            }
                        )

                        val dueDateText = buildString {
                            todo.dueDate?.let { date ->
                                append(todo.getDueDateGroupKey())
                            }
                            todo.dueTime?.let { time ->
                                if (isNotEmpty()) append(" ")
                                append(time.format(timeFormatter))
                            }
                        }

                        Text(
                            text = dueDateText,
                            fontSize = 12.sp,
                            color = if (todo.isOverdue()) {
                                if (isDarkTheme) ErrorRed else AiryErrorRed
                            } else {
                                textSecondaryColor
                            }
                        )
                    }
                }

                // 라벨
                if (todo.labels.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        todo.labels.take(3).forEach { label ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = accentColor.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "#$label",
                                    fontSize = 11.sp,
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 우선순위 표시
            if (todo.priority != TodoPriority.NONE) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .then(
                            Modifier
                                .glassPanelThemed(
                                    isDarkTheme = isDarkTheme,
                                    shape = CircleShape
                                )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = todo.priority.getColor()
                    ) {}
                }
            }
        }
    }
}
