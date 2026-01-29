package com.example.kairos_mobile.presentation.todo.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.model.TodoPriority
import com.example.kairos_mobile.ui.theme.*
import java.time.format.DateTimeFormatter

/**
 * 투두 상세 바텀시트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailBottomSheet(
    todo: Todo,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textSecondaryColor = if (isDarkTheme) TextSecondary else AiryTextSecondary
    val accentColor = if (isDarkTheme) PrimaryNavy else AiryAccentBlue
    val sheetBackground = if (isDarkTheme) GlassBackground else AiryGlassBackground
    val errorColor = if (isDarkTheme) ErrorRed else AiryErrorRed
    val successColor = if (isDarkTheme) SuccessGreen else AirySuccessGreen

    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy년 M월 d일") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = sheetBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(2.dp),
                    color = textSecondaryColor.copy(alpha = 0.3f)
                ) {}
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (todo.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = successColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "완료됨",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = successColor
                        )
                    } else if (todo.priority != TodoPriority.NONE) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = todo.priority.getColor(),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = todo.priority.getDisplayName(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = todo.priority.getColor()
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = textSecondaryColor
                    )
                }
            }

            // 제목
            Text(
                text = todo.title ?: todo.content,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor
            )

            // 내용 (제목과 다른 경우)
            if (todo.title != null && todo.content != todo.title) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkTheme) GlassCard.copy(alpha = 0.5f) else AiryGlassCard.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = todo.content,
                        fontSize = 14.sp,
                        color = textSecondaryColor,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 마감일/시간
            if (todo.dueDate != null || todo.dueTime != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = if (todo.isOverdue()) errorColor else textSecondaryColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Column {
                        todo.dueDate?.let { date ->
                            Text(
                                text = date.format(dateFormatter),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (todo.isOverdue()) errorColor else textPrimaryColor
                            )
                        }
                        todo.dueTime?.let { time ->
                            Text(
                                text = time.format(timeFormatter),
                                fontSize = 13.sp,
                                color = textSecondaryColor
                            )
                        }
                    }

                    if (todo.isOverdue()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = errorColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "지연됨",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = errorColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 라벨
            if (todo.labels.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    todo.labels.forEach { label ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accentColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "#$label",
                                fontSize = 13.sp,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 액션 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 삭제 버튼
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = errorColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "삭제",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 완료/미완료 버튼
                Button(
                    onClick = onToggleComplete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (todo.isCompleted) textSecondaryColor else accentColor
                    )
                ) {
                    Icon(
                        imageVector = if (todo.isCompleted) Icons.AutoMirrored.Filled.Undo else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (todo.isCompleted) "미완료로 변경" else "완료",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
