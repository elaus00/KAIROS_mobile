package com.example.kairos_mobile.presentation.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.ui.theme.KairosTheme
import kotlinx.coroutines.launch

/**
 * 온보딩 화면 (3페이지 HorizontalPager)
 * 기능명세서 11.1:
 *   화면 1: "적으면 알아서 정리됩니다" + 핵심 가치 일러스트
 *   화면 2: "AI가 자동으로 분류합니다" + 분류 안내
 *   화면 3: 첫 캡처 유도 — 입력창 + "첫 번째 생각을 적어보세요"
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 4 }
    )

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OnboardingEvent.NavigateToHome -> onComplete()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
    ) {
        // 상단: 건너뛰기 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "건너뛰기",
                color = colors.textMuted,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { viewModel.skip() }
            )
        }

        // 페이저
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OnboardingPage1(colors = colors)
                1 -> OnboardingPage2(colors = colors)
                2 -> OnboardingPageGoogle(
                    colors = colors,
                    isConnected = uiState.isGoogleConnected,
                    onConnect = viewModel::connectGoogle
                )
                3 -> OnboardingPage3(
                    colors = colors,
                    inputText = uiState.inputText,
                    isSubmitting = uiState.isSubmitting,
                    onInputChange = viewModel::updateInput,
                    onSubmit = viewModel::completeOnboarding
                )
            }
        }

        // 하단: 페이지 인디케이터 + 다음/시작 버튼
        OnboardingBottomBar(
            currentPage = pagerState.currentPage,
            totalPages = 4,
            colors = colors,
            onNext = {
                scope.launch {
                    if (pagerState.currentPage < 3) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    } else {
                        viewModel.completeOnboarding()
                    }
                }
            }
        )
    }
}

/**
 * 온보딩 1페이지: 앱 소개
 */
@Composable
private fun OnboardingPage1(colors: com.example.kairos_mobile.ui.theme.KairosColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 아이콘/일러스트 영역
        Text(
            text = "Kairos",
            color = colors.text,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "적으면\n알아서 정리됩니다",
            color = colors.text,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 40.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "떠오르는 순간, 바로 던지면 끝.\n정리는 AI가 알아서 합니다.",
            color = colors.textSecondary,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 온보딩 2페이지: AI 분류 안내
 */
@Composable
private fun OnboardingPage2(colors: com.example.kairos_mobile.ui.theme.KairosColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // AI 분류 시각적 표현
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClassificationChip(label = "할 일", colors = colors)
            ClassificationChip(label = "일정", colors = colors)
            ClassificationChip(label = "노트", colors = colors)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "AI가 자동으로\n분류합니다",
            color = colors.text,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 40.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "틀리면 탭 한 번으로 수정하세요.\n당신은 기록만 하면 됩니다.",
            color = colors.textSecondary,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 온보딩 Google Calendar 연결 페이지
 */
@Composable
private fun OnboardingPageGoogle(
    colors: com.example.kairos_mobile.ui.theme.KairosColors,
    isConnected: Boolean,
    onConnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Google Calendar\n연동",
            color = colors.text,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 40.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "일정을 캘린더에 자동으로 추가하고\n알림을 받을 수 있습니다.",
            color = colors.textSecondary,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 연결 버튼 / 연결됨 상태
        if (isConnected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.success.copy(alpha = 0.15f))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "연결됨",
                    color = colors.success,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.accent)
                    .clickable { onConnect() }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Google Calendar 연결",
                    color = if (colors.isDark) colors.background else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "나중에 설정에서 연결할 수도 있습니다.",
            color = colors.textMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 분류 칩 컴포넌트
 */
@Composable
private fun ClassificationChip(
    label: String,
    colors: com.example.kairos_mobile.ui.theme.KairosColors
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.chipBg)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = colors.chipText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 온보딩 3페이지: 첫 캡처 유도
 */
@Composable
private fun OnboardingPage3(
    colors: com.example.kairos_mobile.ui.theme.KairosColors,
    inputText: String,
    isSubmitting: Boolean,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "첫 번째 생각을\n적어보세요",
            color = colors.text,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 40.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "무엇이든 좋습니다.\n할 일, 일정, 아이디어 — 그냥 적으세요.",
            color = colors.textSecondary,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 입력 영역
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = colors.text,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(colors.accent),
                singleLine = false,
                maxLines = 4,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.accentBg)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "첫 번째 생각을 적어보세요...",
                                color = colors.placeholder,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // 전송 버튼
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank()) colors.accent
                        else colors.accentBg
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        enabled = !isSubmitting && inputText.isNotBlank()
                    ) { onSubmit() },
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = if (colors.isDark) colors.background else Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "전송",
                        tint = if (inputText.isNotBlank()) {
                            if (colors.isDark) colors.background else Color.White
                        } else colors.textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * 하단 바: 페이지 인디케이터 + 다음 버튼
 */
@Composable
private fun OnboardingBottomBar(
    currentPage: Int,
    totalPages: Int,
    colors: com.example.kairos_mobile.ui.theme.KairosColors,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 페이지 인디케이터
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(totalPages) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentPage) 24.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (index == currentPage) colors.accent
                            else colors.border
                        )
                )
            }
        }

        // 다음/시작 버튼
        val buttonText = if (currentPage == totalPages - 1) "시작하기" else "다음"
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(colors.accent)
                .clickable { onNext() }
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buttonText,
                color = if (colors.isDark) colors.background else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
