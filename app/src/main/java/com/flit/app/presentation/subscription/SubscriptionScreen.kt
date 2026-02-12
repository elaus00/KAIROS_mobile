package com.flit.app.presentation.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.ui.theme.FlitTheme

/**
 * 구독 관리 화면
 * 현재 플랜 + 기능 목록 + 업그레이드 버튼
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FlitTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "구독 관리",
                        color = colors.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 현재 플랜 표시
            CurrentPlanCard(tier = uiState.tier)

            Spacer(modifier = Modifier.height(24.dp))

            // Premium 기능 목록
            Text(
                text = "Premium 기능",
                color = colors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val features = listOf(
                "AI 그룹 분류" to uiState.features.aiGrouping,
                "받은함 자동 분류" to uiState.features.inboxClassify,
                "의미 검색" to uiState.features.semanticSearch,
                "노트 재구성" to uiState.features.noteReorganize,
                "분석 대시보드" to uiState.features.analyticsDashboard,
                "이미지 텍스트 인식 (OCR)" to uiState.features.ocr,
                "분류 프리셋" to uiState.features.classificationPreset,
                "맞춤 지시어" to uiState.features.customInstruction
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.card)
                    .border(0.5.dp, colors.borderLight, RoundedCornerShape(12.dp))
            ) {
                features.forEachIndexed { index, (name, enabled) ->
                    FeatureItem(name = name, enabled = enabled)
                    if (index < features.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = colors.borderLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 업그레이드/구매 버튼
            if (uiState.tier == SubscriptionTier.FREE) {
                Button(
                    onClick = { viewModel.onUpgrade() },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = colors.text
                        )
                    } else {
                        Text(
                            text = "Premium으로 업그레이드",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.error!!,
                    color = colors.danger,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 현재 플랜 카드
 */
@Composable
private fun CurrentPlanCard(
    tier: SubscriptionTier,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    val isPremium = tier == SubscriptionTier.PREMIUM

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isPremium) colors.accent else colors.card)
            .border(
                width = if (isPremium) 0.dp else 0.5.dp,
                color = colors.borderLight,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = if (isPremium) "Premium" else "Free",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPremium) {
                    if (colors.isDark) colors.background else colors.card
                } else {
                    colors.text
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isPremium) "모든 기능을 사용할 수 있습니다" else "기본 기능만 사용 가능합니다",
                fontSize = 14.sp,
                color = if (isPremium) {
                    if (colors.isDark) colors.background.copy(alpha = 0.7f) else colors.card.copy(alpha = 0.7f)
                } else {
                    colors.textMuted
                }
            )
        }
    }
}

/**
 * 기능 아이템
 */
@Composable
private fun FeatureItem(
    name: String,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = if (enabled) colors.text else colors.textMuted,
            fontSize = 15.sp
        )
        if (enabled) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "사용 가능",
                tint = colors.accent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
