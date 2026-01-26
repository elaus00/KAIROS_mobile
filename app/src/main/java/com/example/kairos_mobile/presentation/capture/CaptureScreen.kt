package com.example.kairos_mobile.presentation.capture

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.navigation.NavRoutes
import com.example.kairos_mobile.presentation.components.*
import com.example.kairos_mobile.ui.components.AnimatedGlassBackground

/**
 * KAIROS Magic Inbox - Glassmorphism 메인 화면
 *
 * Phase 2: 멀티모달 캡처 지원
 * - TEXT, IMAGE, VOICE, WEB_CLIP 모드
 */
@Composable
fun CaptureScreen(
    sharedText: String? = null,       // M07: 공유된 텍스트 (URL 포함)
    sharedImageUri: Uri? = null,      // M07: 공유된 이미지 URI
    onNavigate: (String) -> Unit = {},
    viewModel: CaptureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
        // 애니메이션 배경 (Blobs)
        AnimatedGlassBackground()

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
                            containerColor = com.example.kairos_mobile.ui.theme.GlassCard,
                            contentColor = com.example.kairos_mobile.ui.theme.TextPrimary
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
                        onProfileClick = {
                            onNavigate(NavRoutes.NOTIFICATIONS)
                        }
                    )

                    // 메인 콘텐츠 영역
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 100.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 메인 캡처 카드 (Phase 3: QuickTypeButtons 통합)
                        GlassCaptureCard(
                            text = uiState.inputText,
                            onTextChange = viewModel::onTextChanged,
                            onSubmit = viewModel::onSubmit,
                            onModeSelected = viewModel::onCaptureModeChanged,
                            suggestedQuickTypes = uiState.suggestedQuickTypes,
                            onQuickTypeSelected = viewModel::onQuickTypeSelected,
                            enabled = !uiState.isLoading,
                            isLoading = uiState.isLoading,
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
                        selectedTab = NavigationTab.CAPTURE,
                        onTabSelected = { tab ->
                            val route = when (tab) {
                                NavigationTab.CAPTURE -> NavRoutes.CAPTURE
                                NavigationTab.SEARCH -> NavRoutes.SEARCH
                                NavigationTab.ARCHIVE -> NavRoutes.ARCHIVE
                                NavigationTab.SETTINGS -> NavRoutes.SETTINGS
                            }
                            if (route != NavRoutes.CAPTURE) {
                                onNavigate(route)
                            }
                        }
                    )
                }

                // 오프라인 인디케이터
                if (uiState.pendingCount > 0) {
                    OfflineIndicator(
                        count = uiState.pendingCount,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    )
                }
            }
        }
    }
}
