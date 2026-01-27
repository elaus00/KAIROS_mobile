package com.example.kairos_mobile.presentation.insight

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kairos_mobile.domain.model.InsightType
import com.example.kairos_mobile.navigation.NavRoutes
import com.example.kairos_mobile.presentation.components.common.GlassBottomNavigation
import com.example.kairos_mobile.presentation.components.common.GlassHeader
import com.example.kairos_mobile.presentation.components.common.NavigationTab
import com.example.kairos_mobile.presentation.components.insight.OfflineIndicator
import com.example.kairos_mobile.presentation.components.insight.SuccessFeedback
import com.example.kairos_mobile.presentation.components.insight.GlassInsightCard
import com.example.kairos_mobile.ui.components.AnimatedGlassBackgroundThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * KAIROS Magic Inbox - Glassmorphism 메인 화면
 *
 * Phase 2: 멀티모달 캡처 지원
 * - TEXT, IMAGE, VOICE, WEB_CLIP 모드
 */
@Composable
fun InsightScreen(
    sharedText: String? = null,       // M07: 공유된 텍스트 (URL 포함)
    sharedImageUri: Uri? = null,      // M07: 공유된 이미지 URI
    onNavigate: (String) -> Unit = {},
    isDarkTheme: Boolean = false,     // 테마 설정
    viewModel: InsightViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 테마에 따른 색상 설정
    val snackbarBgColor = if (isDarkTheme) GlassCard else AiryGlassCard
    val snackbarContentColor = if (isDarkTheme) TextPrimary else AiryTextPrimary

    // 이미지 선택기 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    // M07: 공유 인텐트 처리
    LaunchedEffect(sharedText, sharedImageUri) {
        when {
            sharedText != null -> viewModel.onSharedTextReceived(sharedText)
            sharedImageUri != null -> viewModel.onSharedImageReceived(sharedImageUri)
        }
    }

    // 에러 메시지 스낵바 표시
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.onErrorDismissed()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 테마 인식 애니메이션 배경
        AnimatedGlassBackgroundThemed(isDarkTheme = isDarkTheme)

        // 메인 콘텐츠
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { data ->
                        Snackbar(
                            snackbarData = data,
                            shape = RoundedCornerShape(12.dp),
                            containerColor = snackbarBgColor,
                            contentColor = snackbarContentColor
                        )
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    // 헤더
                    GlassHeader(
                        onNotificationClick = {
                            onNavigate(NavRoutes.NOTIFICATIONS)
                        },
                        isDarkTheme = isDarkTheme
                    )

                    // 메인 콘텐츠 영역
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 100.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 메인 인사이트 카드 (Phase 3: QuickTypeButtons 통합)
                        GlassInsightCard(
                            text = uiState.inputText,
                            onTextChange = viewModel::onTextChanged,
                            onSubmit = viewModel::onSubmit,
                            onModeSelected = { mode ->
                                viewModel.onInsightModeChanged(mode)
                                // IMAGE 모드 선택 시 갤러리 열기
                                if (mode == InsightMode.IMAGE) {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            },
                            suggestedQuickTypes = uiState.suggestedQuickTypes,
                            onQuickTypeSelected = viewModel::onQuickTypeSelected,
                            enabled = !uiState.isLoading,
                            isLoading = uiState.isLoading,
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        )
                    }
                }

                // 성공 피드백
                if (uiState.showSuccessFeedback) {
                    SuccessFeedback(
                        isOffline = uiState.isOffline,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // 하단 네비게이션
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                ) {
                    GlassBottomNavigation(
                        selectedTab = NavigationTab.INSIGHT,
                        onTabSelected = { tab ->
                            val route = when (tab) {
                                NavigationTab.INSIGHT -> NavRoutes.INSIGHT
                                NavigationTab.SEARCH -> NavRoutes.SEARCH
                                NavigationTab.ARCHIVE -> NavRoutes.ARCHIVE
                                NavigationTab.SETTINGS -> NavRoutes.SETTINGS
                            }
                            if (route != NavRoutes.INSIGHT) {
                                onNavigate(route)
                            }
                        },
                        isDarkTheme = isDarkTheme
                    )
                }

                // 오프라인 인디케이터
                if (uiState.pendingCount > 0) {
                    OfflineIndicator(
                        count = uiState.pendingCount,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    )
                }
            }
        }
    }
}
