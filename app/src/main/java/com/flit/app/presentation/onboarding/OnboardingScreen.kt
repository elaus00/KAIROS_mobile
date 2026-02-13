package com.flit.app.presentation.onboarding

import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flit.app.ui.theme.FlitTheme
import kotlinx.coroutines.flow.Flow
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
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val readGranted = result[Manifest.permission.READ_CALENDAR] == true
        val writeGranted = result[Manifest.permission.WRITE_CALENDAR] == true
        viewModel.onCalendarPermissionResult(readGranted && writeGranted)
    }

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OnboardingEvent.NavigateToHome -> onComplete()
            }
        }
    }

    OnboardingContent(
        uiState = uiState,
        onSkip = viewModel::skip,
        onUpdateInput = viewModel::updateInput,
        onCompleteOnboarding = viewModel::completeOnboarding,
        onCalendarConnect = {
            calendarPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
                )
            )
        }
    )
}

/**
 * 온보딩 화면 Content
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingContent(
    uiState: OnboardingUiState,
    onSkip: () -> Unit,
    onUpdateInput: (String) -> Unit,
    onCompleteOnboarding: () -> Unit,
    onCalendarConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 4 }
    )

    Column(
        modifier = modifier
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
            TextButton(
                onClick = onSkip,
                modifier = Modifier.heightIn(min = 48.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "건너뛰기",
                    color = colors.textMuted,
                    fontSize = 14.sp
                )
            }
        }

        // 페이저
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OnboardingPage1(colors = colors)
                1 -> OnboardingPage2(colors = colors)
                2 -> OnboardingPageCalendar(
                    colors = colors,
                    isConnected = uiState.isCalendarPermissionGranted,
                    errorMessage = uiState.calendarConnectionError,
                    onConnect = onCalendarConnect
                )
                3 -> OnboardingPage3(
                    colors = colors,
                    inputText = uiState.inputText,
                    isSubmitting = uiState.isSubmitting,
                    onInputChange = onUpdateInput,
                    onSubmit = onCompleteOnboarding
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
                        onCompleteOnboarding()
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
private fun OnboardingPage1(colors: com.flit.app.ui.theme.FlitColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 아이콘/일러스트 영역
        Text(
            text = "Flit.",
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
private fun OnboardingPage2(colors: com.flit.app.ui.theme.FlitColors) {
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
 * 온보딩 캘린더 권한 연결 페이지
 */
@Composable
private fun OnboardingPageCalendar(
    colors: com.flit.app.ui.theme.FlitColors,
    isConnected: Boolean,
    errorMessage: String?,
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
            text = "캘린더 연동",
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

        // 에러 메시지 표시
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = colors.danger,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

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
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (errorMessage != null) "다시 시도" else "캘린더 권한 허용",
                    color = colors.background,
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
    colors: com.flit.app.ui.theme.FlitColors
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
    colors: com.flit.app.ui.theme.FlitColors,
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
            FilledIconButton(
                onClick = onSubmit,
                enabled = !isSubmitting && inputText.isNotBlank(),
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = colors.accent,
                    disabledContainerColor = colors.accentBg
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colors.background
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "전송",
                        tint = if (inputText.isNotBlank()) {
                            colors.background
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
    colors: com.flit.app.ui.theme.FlitColors,
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
        Button(
            onClick = onNext,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
        ) {
            Text(
                text = buttonText,
                color = colors.background,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingContentPreview() {
    FlitTheme {
        OnboardingContent(
            uiState = OnboardingUiState(),
            onSkip = {},
            onUpdateInput = {},
            onCompleteOnboarding = {},
            onCalendarConnect = {}
        )
    }
}
