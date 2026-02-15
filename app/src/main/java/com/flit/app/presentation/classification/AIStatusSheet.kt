package com.flit.app.presentation.classification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.flit.app.domain.model.Capture
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.ui.theme.FlitTheme

/**
 * AI 분류 현황 패널
 * 상단에서 아래로 펼쳐지는 형태
 * 24시간 이내 미확인 분류 목록 표시
 */
@Composable
fun AIStatusSheet(
    onDismiss: () -> Unit,
    onNavigateToHistory: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: AIStatusSheetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FlitTheme.colors

    // 반투명 배경 오버레이 (네비게이션 바 포함 전체 화면)
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0))
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    ) {
        // 상단 패널 (위에서 아래로)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(colors.background)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* 패널 영역 클릭 시 닫히지 않음 */ }
        ) {
            // 상단 여백 (상태바 영역)
            Spacer(modifier = Modifier.statusBarsPadding())

            // 헤더: 제목 + 부제 + 전체 확인
            StatusSheetHeader(
                count = uiState.unconfirmedCaptures.size,
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
                        .padding(vertical = 40.dp),
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
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "미확인 분류가 없어요",
                        color = colors.textMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
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
                        // 항목 구분선
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = colors.borderLight,
                            modifier = Modifier.padding(horizontal = 0.dp)
                        )
                    }
                }
            }

            // 전체 기록 보기 링크
            if (onNavigateToHistory != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onNavigateToHistory()
                        }
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
 * 패널 헤더: 제목 + 부제 + 전체 확인
 */
@Composable
private fun StatusSheetHeader(
    count: Int,
    onConfirmAll: () -> Unit
) {
    val colors = FlitTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "AI 분류 현황",
                color = colors.text,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "24시간 이내 분류된 항목",
                color = colors.textMuted,
                fontSize = 12.sp
            )
        }

        // 전체 확인 버튼 — 최소 터치 타겟 36dp
        if (count > 0) {
            Text(
                text = "전체 확인",
                color = colors.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onConfirmAll() }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
    }
}

/**
 * 개별 분류 항목
 * 수평 레이아웃: [분류 칩 ▾] [제목] [시간] [확인]
 */
@Composable
private fun ClassificationItem(
    capture: Capture,
    onConfirm: () -> Unit,
    onChangeType: (ClassifiedType, com.flit.app.domain.model.NoteSubType?) -> Unit
) {
    val colors = FlitTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 분류 드롭다운 칩
        ClassificationDropdown(
            currentType = capture.classifiedType,
            currentSubType = capture.noteSubType,
            onTypeSelected = onChangeType
        )

        // 제목 텍스트
        Text(
            text = capture.aiTitle ?: capture.originalText.take(30),
            color = colors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // 시간 표시
        Text(
            text = getRelativeTime(capture.createdAt),
            color = colors.textMuted,
            fontSize = 12.sp
        )

        // 확인 버튼 — 최소 터치 타겟 36dp
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.accentBg)
                .clickable { onConfirm() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "확인",
                color = colors.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 상대 시간 표시 (방금, N분 전, N시간 전, 어제)
 */
private fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)

    return when {
        minutes < 1 -> "방금"
        minutes < 60 -> "${minutes}분 전"
        hours < 24 -> "${hours}시간 전"
        else -> "어제"
    }
}
