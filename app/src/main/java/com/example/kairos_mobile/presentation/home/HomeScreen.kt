package com.example.kairos_mobile.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.presentation.capture.QuickCapturePopup
import com.example.kairos_mobile.presentation.capture.QuickCaptureViewModel
import com.example.kairos_mobile.presentation.components.common.KairosBottomNav
import com.example.kairos_mobile.presentation.components.common.KairosTab
import com.example.kairos_mobile.presentation.components.common.SectionHeaderKorean
import com.example.kairos_mobile.presentation.home.components.AIRecommendationCard
import com.example.kairos_mobile.presentation.home.components.CaptureGrid
import com.example.kairos_mobile.presentation.home.components.CaptureGridEmpty
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * Home 화면 (PRD v4.0)
 * - 앱 제목
 * - AI 추천 일정 카드
 * - 최근 캡처 그리드
 * - FAB (QuickCapture 팝업 열기)
 * - 하단 네비게이션
 */
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onNavigateToCapture: (String) -> Unit,
    onOpenCamera: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    quickCaptureViewModel: QuickCaptureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }

    // QuickCapture 팝업 표시 상태
    var showQuickCapturePopup by remember { mutableStateOf(false) }

    // 에러 메시지 표시
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(HomeEvent.DismissError)
        }
    }

    // 제출 성공 알림
    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            snackbarHostState.showSnackbar("캡처가 저장되었습니다")
            viewModel.onEvent(HomeEvent.ClearSubmitSuccess)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                KairosBottomNav(
                    selectedTab = KairosTab.HOME,
                    onTabSelected = { tab ->
                        onNavigate(tab.route)
                    }
                )
            },
            floatingActionButton = {
                // FAB - QuickCapture 팝업 열기
                FloatingActionButton(
                    onClick = { showQuickCapturePopup = true },
                    shape = CircleShape,
                    containerColor = colors.accent,
                    contentColor = if (colors.isDark) colors.background else colors.card,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "빠른 캡처",
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            containerColor = colors.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(colors.background)
            ) {
                // 앱 제목
                Text(
                    text = "Kairos",
                    color = colors.text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 16.dp,
                        bottom = 16.dp
                    )
                )

                // AI 추천 일정 카드
                AIRecommendationCard(
                    schedule = uiState.nextSchedule,
                    todayScheduleCount = uiState.todayScheduleCount,
                    onClick = { onNavigate(KairosTab.CALENDAR.route) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 최근 캡처 섹션
                SectionHeaderKorean(title = "최근 캡처")

                if (uiState.recentCaptures.isEmpty() && !uiState.isLoadingCaptures) {
                    CaptureGridEmpty()
                } else {
                    CaptureGrid(
                        captures = uiState.recentCaptures,
                        onCaptureClick = onNavigateToCapture,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // QuickCapture 팝업
        QuickCapturePopup(
            visible = showQuickCapturePopup,
            onDismiss = { showQuickCapturePopup = false },
            onOpenCamera = onOpenCamera,
            viewModel = quickCaptureViewModel
        )
    }
}

/**
 * Home 화면 내용 (Scaffold 없이)
 * MainScreen의 HorizontalPager에서 사용
 */
@Composable
fun HomeContent(
    onNavigateToCapture: (String) -> Unit,
    onNavigateToCalendar: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors

    // 에러 메시지 표시
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(HomeEvent.DismissError)
        }
    }

    // 제출 성공 알림
    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            snackbarHostState.showSnackbar("캡처가 저장되었습니다")
            viewModel.onEvent(HomeEvent.ClearSubmitSuccess)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // 앱 제목
        Text(
            text = "Kairos",
            color = colors.text,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                start = 16.dp,
                top = 16.dp,
                bottom = 16.dp
            )
        )

        // AI 추천 일정 카드
        AIRecommendationCard(
            schedule = uiState.nextSchedule,
            todayScheduleCount = uiState.todayScheduleCount,
            onClick = onNavigateToCalendar,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 최근 캡처 섹션
        SectionHeaderKorean(title = "최근 캡처")

        if (uiState.recentCaptures.isEmpty() && !uiState.isLoadingCaptures) {
            CaptureGridEmpty()
        } else {
            CaptureGrid(
                captures = uiState.recentCaptures,
                onCaptureClick = onNavigateToCapture,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
