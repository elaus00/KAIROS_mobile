package com.example.kairos_mobile.navigation

import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kairos_mobile.presentation.archive.ArchiveScreen
import com.example.kairos_mobile.presentation.insight.InsightScreen
import com.example.kairos_mobile.presentation.notifications.NotificationsScreen
import com.example.kairos_mobile.presentation.search.SearchScreen
import com.example.kairos_mobile.presentation.settings.SettingsScreen

/**
 * Navigation 경로 정의
 */
object NavRoutes {
    const val INSIGHT = "insight"
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
    sharedImageUri: Uri? = null
) {
    // 공통 탭 네비게이션 함수
    val navigateToTab: (String) -> Unit = { route ->
        if (route == NavRoutes.INSIGHT) {
            // INSIGHT로 이동할 때는 popBackStack으로 시작 화면으로 돌아감
            navController.popBackStack(NavRoutes.INSIGHT, inclusive = false)
        } else {
            navController.navigate(route) {
                // 시작 화면까지 pop하여 백스택 정리
                popUpTo(NavRoutes.INSIGHT) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.INSIGHT,
        // 페이지 전환 애니메이션 제거
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // 인사이트 화면
        composable(NavRoutes.INSIGHT) {
            InsightScreen(
                sharedText = sharedText,
                sharedImageUri = sharedImageUri,
                onNavigate = navigateToTab
            )
        }

        // 검색 화면
        composable(NavRoutes.SEARCH) {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onInsightClick = { _ ->
                    // TODO: 인사이트 상세 화면으로 이동
                },
                onNavigate = navigateToTab
            )
        }

        // 히스토리 화면
        composable(NavRoutes.ARCHIVE) {
            ArchiveScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onInsightClick = { _ ->
                    // TODO: 인사이트 상세 화면으로 이동
                },
                onNavigate = navigateToTab
            )
        }

        // 알림 화면
        composable(NavRoutes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNotificationClick = { insightId ->
                    // TODO: 인사이트 상세 화면으로 이동 (현재는 뒤로가기)
                    if (insightId != null) {
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
                onNavigate = navigateToTab
            )
        }
    }
}
