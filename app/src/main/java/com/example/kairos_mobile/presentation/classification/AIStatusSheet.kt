package com.example.kairos_mobile.presentation.classification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * AI 분류 현황 바텀시트
 * 24시간 이내 미확인 분류 목록 표시
 * 확인/변경 액션 제공
 */
@Composable
fun AIStatusSheet(
    onDismiss: () -> Unit,
    onNavigateToHistory: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: AIStatusSheetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors

    // 반투명 배경 오버레이
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.text.copy(alpha = 0.3f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    ) {
        // 바텀시트 본체 (~70% 높이)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(colors.background)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* 시트 영역 클릭 시 닫히지 않음 */ }
        ) {
            // 핸들 바
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.borderLight)
                    .align(Alignment.CenterHorizontally)
            )

            // 헤더: 제목 + 닫기 + 전체 확인
            StatusSheetHeader(
                count = uiState.unconfirmedCaptures.size,
                onDismiss = onDismiss,
                onConfirmAll = { viewModel.confirmAll() }
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = colors.borderLight
            )

            // 분류 목록
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = colors.textMuted
                    )
                }
            } else if (uiState.unconfirmedCaptures.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "미확인 분류가 없습니다",
                        color = colors.textMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.unconfirmedCaptures,
                        key = { it.id }
                    ) { capture ->
                        ClassificationItem(
                            capture = capture,
                            onConfirm = { viewModel.confirmClassification(capture.id) },
                            onChangeType = { type, subType ->
                                viewModel.changeClassification(capture.id, type, subType)
                            }
                        )
                    }
                }
            }

            // 전체 기록 보기 링크
            if (onNavigateToHistory != null) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = colors.borderLight
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToHistory() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "전체 기록 보기",
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 시트 헤더: 제목 + 닫기 버튼 + 전체 확인
 */
@Composable
private fun StatusSheetHeader(
    count: Int,
    onDismiss: () -> Unit,
    onConfirmAll: () -> Unit
) {
    val colors = KairosTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "AI 분류 현황",
                color = colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (count > 0) {
                Text(
                    text = "$count",
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 전체 확인 버튼
            if (count > 0) {
                Text(
                    text = "전체 확인",
                    color = colors.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onConfirmAll() }
                )
            }

            // 닫기 버튼
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = colors.textMuted,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
        }
    }
}

/**
 * 개별 분류 항목
 * AI 제목 + 원문 미리보기 + 분류 칩(변경 가능) + 확인 버튼
 */
@Composable
private fun ClassificationItem(
    capture: Capture,
    onConfirm: () -> Unit,
    onChangeType: (ClassifiedType, com.example.kairos_mobile.domain.model.NoteSubType?) -> Unit
) {
    val colors = KairosTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 텍스트 영역
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // AI 제목 또는 원문 앞부분
            Text(
                text = capture.aiTitle ?: capture.originalText.take(30),
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 원문 미리보기 (AI 제목이 있을 때만)
            if (capture.aiTitle != null) {
                Text(
                    text = capture.originalText,
                    color = colors.textMuted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 분류 드롭다운
            ClassificationDropdown(
                currentType = capture.classifiedType,
                currentSubType = capture.noteSubType,
                onTypeSelected = onChangeType
            )
        }

        // 확인 버튼
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.accentBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onConfirm() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "확인",
                tint = colors.accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
