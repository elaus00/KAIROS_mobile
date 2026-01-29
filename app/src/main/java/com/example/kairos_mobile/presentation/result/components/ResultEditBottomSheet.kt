package com.example.kairos_mobile.presentation.result.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.ui.theme.*

/**
 * 결과 수정 바텀시트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultEditBottomSheet(
    currentType: CaptureType?,
    currentTitle: String,
    currentTags: List<String>,
    onTypeChanged: (CaptureType) -> Unit,
    onTitleChanged: (String) -> Unit,
    onTagAdded: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    isSaving: Boolean = false,
    isDarkTheme: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    var newTagInput by remember { mutableStateOf("") }

    val textPrimaryColor = if (isDarkTheme) TextPrimary else AiryTextPrimary
    val textSecondaryColor = if (isDarkTheme) TextSecondary else AiryTextSecondary
    val accentColor = if (isDarkTheme) PrimaryNavy else AiryAccentBlue
    val sheetBackground = if (isDarkTheme) GlassBackground else AiryGlassBackground

    val availableTypes = listOf(
        CaptureType.IDEA,
        CaptureType.TODO,
        CaptureType.NOTE,
        CaptureType.QUICK_NOTE
    )

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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "분류 결과 수정",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = textSecondaryColor
                    )
                }
            }

            // 타입 선택
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "유형",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSecondaryColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableTypes.forEach { type ->
                        val isSelected = currentType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { onTypeChanged(type) },
                            label = {
                                Text(
                                    text = type.getDisplayName(),
                                    fontSize = 13.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = type.getColor().copy(alpha = 0.2f),
                                selectedLabelColor = type.getColor()
                            ),
                            border = if (isSelected) {
                                FilterChipDefaults.filterChipBorder(
                                    borderColor = type.getColor(),
                                    selectedBorderColor = type.getColor(),
                                    enabled = true,
                                    selected = true
                                )
                            } else null
                        )
                    }
                }
            }

            // 제목 입력
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "제목",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSecondaryColor
                )

                OutlinedTextField(
                    value = currentTitle,
                    onValueChange = onTitleChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "제목을 입력하세요",
                            color = textSecondaryColor.copy(alpha = 0.5f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = textSecondaryColor.copy(alpha = 0.3f),
                        focusedTextColor = textPrimaryColor,
                        unfocusedTextColor = textPrimaryColor
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
            }

            // 태그 입력
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "태그",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSecondaryColor
                )

                // 현재 태그들
                if (currentTags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentTags.forEach { tag ->
                            InputChip(
                                selected = false,
                                onClick = { },
                                label = { Text("#$tag", fontSize = 13.sp) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { onTagRemoved(tag) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "태그 삭제",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    containerColor = accentColor.copy(alpha = 0.1f),
                                    labelColor = accentColor
                                )
                            )
                        }
                    }
                }

                // 새 태그 입력
                OutlinedTextField(
                    value = newTagInput,
                    onValueChange = { newTagInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "새 태그 입력 후 Enter",
                            color = textSecondaryColor.copy(alpha = 0.5f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = textSecondaryColor.copy(alpha = 0.3f),
                        focusedTextColor = textPrimaryColor,
                        unfocusedTextColor = textPrimaryColor
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            tint = textSecondaryColor
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newTagInput.isNotBlank()) {
                                onTagAdded(newTagInput)
                                newTagInput = ""
                            }
                        }
                    )
                )
            }

            // 저장 버튼
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                ),
                enabled = currentType != null && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "저장하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
