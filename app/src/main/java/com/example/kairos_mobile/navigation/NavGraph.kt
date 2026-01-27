package com.example.kairos_mobile.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kairos_mobile.presentation.archive.ArchiveScreen
import com.example.kairos_mobile.presentation.capture.CaptureScreen
import com.example.kairos_mobile.presentation.notifications.NotificationsScreen
import com.example.kairos_mobile.presentation.search.SearchScreen
import com.example.kairos_mobile.presentation.settings.SettingsScreen

/**
 * Navigation 경로 정의
 */
object NavRoutes {
    const val CAPTURE = "capture"
    const val SEARCH = "search"
    const val ARCHIVE = "archive"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
}

/**
 * KAIROS Navigation Graph
 */
@Composable
fun KairosNavGraph(
    navController: NavHostController = rememberNavController(),
    sharedText: String? = null,
    sharedImageUri: Uri? = null,
    oAuthCallback: OAuthCallback? = null
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.CAPTURE
    ) {
        // 캡처 화면
        composable(NavRoutes.CAPTURE) {
            CaptureScreen(
                sharedText = sharedText,
                sharedImageUri = sharedImageUri,
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        // 검색 화면
        composable(NavRoutes.SEARCH) {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCaptureClick = { captureId ->
                    // TODO: 캡처 상세 화면으로 이동
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(NavRoutes.CAPTURE) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // 히스토리 화면
        composable(NavRoutes.ARCHIVE) {
            ArchiveScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCaptureClick = { captureId ->
                    // TODO: 캡처 상세 화면으로 이동
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(NavRoutes.CAPTURE) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // 알림 화면
        composable(NavRoutes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNotificationClick = { captureId ->
                    // TODO: 캡처 상세 화면으로 이동 (현재는 뒤로가기)
                    if (captureId != null) {
                        // 나중에 상세 화면 구현 시 네비게이션 추가
                    }
                    navController.popBackStack()
                }
            )
        }

        // 설정 화면
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(NavRoutes.CAPTURE) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * OAuth 콜백 정보
 * Deep Link로 전달된 OAuth 인증 결과
 */
data class OAuthCallback(
    val provider: String,  // "google" or "todoist"
    val code: String,
    val state: String?
)
