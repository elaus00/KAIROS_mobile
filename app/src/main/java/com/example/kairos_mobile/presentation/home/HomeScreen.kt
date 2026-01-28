package com.example.kairos_mobile.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.kairos_mobile.presentation.components.common.KairosBottomNav
import com.example.kairos_mobile.presentation.components.common.KairosTab
import com.example.kairos_mobile.presentation.components.common.SectionHeader
import com.example.kairos_mobile.presentation.home.components.AIRecommendationCard
import com.example.kairos_mobile.presentation.home.components.CaptureGrid
import com.example.kairos_mobile.presentation.home.components.CaptureGridEmpty
import com.example.kairos_mobile.presentation.home.components.CaptureInputArea
import com.example.kairos_mobile.ui.theme.KairosTheme
import kotlinx.coroutines.launch

/**
 * Home 화면 (PRD v4.0)
 * - 앱 제목
 * - AI 추천 일정 카드
 * - 최근 캡처 그리드
 * - 하단 입력창
 * - 하단 네비게이션
 */
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onNavigateToCapture: (String) -> Unit,
    onOpenCamera: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Column {
                // 입력 영역
                CaptureInputArea(
                    inputText = uiState.inputText,
                    characterCount = uiState.characterCount,
                    maxCharacterCount = uiState.maxCharacterCount,
                    suggestedType = uiState.suggestedType,
                    isClassifying = uiState.isClassifying,
                    isSubmitting = uiState.isSubmitting,
                    onInputChange = { viewModel.onEvent(HomeEvent.UpdateInput(it)) },
                    onFocusChange = { viewModel.onEvent(HomeEvent.SetInputFocused(it)) },
                    onSubmit = { viewModel.onEvent(HomeEvent.Submit) },
                    onCameraClick = onOpenCamera
                )

                // 하단 네비게이션
                KairosBottomNav(
                    selectedTab = KairosTab.HOME,
                    onTabSelected = { tab ->
                        onNavigate(tab.route)
                    }
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
            SectionHeader(title = "Recent Captures")

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
}
