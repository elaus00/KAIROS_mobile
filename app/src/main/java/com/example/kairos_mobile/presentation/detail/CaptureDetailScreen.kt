package com.example.kairos_mobile.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 캡처 상세 화면
 * 분류 칩 변경 + 원문 확인 (최소 구현)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureDetailScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: CaptureDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.aiTitle ?: "캡처 상세",
                        color = colors.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = colors.text
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colors.accent,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = colors.textMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 분류 칩 섹션
                Text(
                    text = "분류",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                ClassificationChipRow(
                    currentType = uiState.classifiedType,
                    onTypeSelected = { type ->
                        viewModel.onChangeClassification(type)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 생성 시각
                if (uiState.createdAt > 0) {
                    Text(
                        text = "생성 시각",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatDateTime(uiState.createdAt),
                        color = colors.textMuted,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 원문 텍스트
                Text(
                    text = "원문",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.card)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = uiState.originalText,
                        color = colors.text,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * 분류 칩 Row (4옵션: 할 일 / 일정 / 노트 / 아이디어)
 */
@Composable
private fun ClassificationChipRow(
    currentType: ClassifiedType,
    onTypeSelected: (ClassifiedType) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ClassificationChip(
            label = "할 일",
            isSelected = currentType == ClassifiedType.TODO,
            onClick = { onTypeSelected(ClassifiedType.TODO) }
        )
        ClassificationChip(
            label = "일정",
            isSelected = currentType == ClassifiedType.SCHEDULE,
            onClick = { onTypeSelected(ClassifiedType.SCHEDULE) }
        )
        ClassificationChip(
            label = "노트",
            isSelected = currentType == ClassifiedType.NOTES,
            onClick = { onTypeSelected(ClassifiedType.NOTES) }
        )
        ClassificationChip(
            label = "아이디어",
            isSelected = false,
            onClick = {
                onTypeSelected(ClassifiedType.NOTES)
                // NOTES + IDEA subtype은 ChangeClassificationUseCase에서 처리
            }
        )
    }
}

/**
 * 개별 분류 칩
 */
@Composable
private fun ClassificationChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.accent else colors.chipBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) {
                if (colors.isDark) colors.background else colors.card
            } else {
                colors.textSecondary
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 시간 포맷 (epoch ms → "yyyy.M.d HH:mm")
 */
private fun formatDateTime(epochMs: Long): String {
    val dateTime = Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    return dateTime.format(DateTimeFormatter.ofPattern("yyyy.M.d HH:mm"))
}
