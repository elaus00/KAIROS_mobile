package com.example.kairos_mobile.navigation

import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kairos_mobile.presentation.archive.ArchiveScreen
import com.example.kairos_mobile.presentation.calendar.CalendarScreen
import com.example.kairos_mobile.presentation.home.HomeScreen
import com.example.kairos_mobile.presentation.insight.InsightScreen
import com.example.kairos_mobile.presentation.notifications.NotificationsScreen
import com.example.kairos_mobile.presentation.result.ResultScreen
import com.example.kairos_mobile.presentation.search.SearchScreen
import com.example.kairos_mobile.presentation.settings.SettingsScreen

/**
 * Navigation 경로 정의 (PRD v4.0)
 * HOME / CALENDAR / NOTES / SETTINGS
 */
object NavRoutes {
    // PRD v4.0 메인 탭
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val NOTES = "notes"
    const val SETTINGS = "settings"

    // 보조 화면
    const val RESULT = "result/{insightId}"
    const val NOTE_EDIT = "notes/{noteId}"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"

    // 레거시 호환성 (기존 코드 지원)
    @Deprecated("Use HOME instead")
    const val CAPTURE = "home"
    @Deprecated("Use CALENDAR instead")
    const val TODO = "calendar"
    @Deprecated("Use NOTES instead")
    const val HISTORY = "notes"

    /**
     * ResultScreen 라우트 생성
     */
    fun result(insightId: String): String = "result/$insightId"

    /**
     * NoteEditScreen 라우트 생성
     */
    fun noteEdit(noteId: String): String = "notes/$noteId"
}

/**
 * KAIROS Navigation Graph (PRD v4.0)
 * 4개 탭: HOME / CALENDAR / NOTES / SETTINGS
 */
@Composable
fun KairosNavGraph(
    navController: NavHostController = rememberNavController(),
    sharedText: String? = null,
    sharedImageUri: Uri? = null
) {
    // 공통 탭 네비게이션 함수
    val navigateToTab: (String) -> Unit = { route ->
        if (route == NavRoutes.HOME) {
            // HOME으로 이동할 때는 popBackStack으로 시작 화면으로 돌아감
            navController.popBackStack(NavRoutes.HOME, inclusive = false)
        } else {
            navController.navigate(route) {
                // 시작 화면까지 pop하여 백스택 정리
                popUpTo(NavRoutes.HOME) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // ResultScreen으로 네비게이션
    val navigateToResult: (String) -> Unit = { insightId ->
        navController.navigate(NavRoutes.result(insightId))
    }

    // NoteEditScreen으로 네비게이션
    val navigateToNoteEdit: (String) -> Unit = { noteId ->
        navController.navigate(NavRoutes.noteEdit(noteId))
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        // 페이지 전환 애니메이션 제거
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // 홈 화면 (HOME) - PRD v4.0
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigate = navigateToTab,
                onNavigateToCapture = navigateToResult,
                onOpenCamera = {
                    // TODO: 카메라 열기 구현
                }
            )
        }

        // 결과 화면 (RESULT)
        composable(
            route = NavRoutes.RESULT,
            arguments = listOf(
                navArgument("insightId") { type = NavType.StringType }
            )
        ) {
            ResultScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 캘린더 화면 (CALENDAR) - PRD v4.0
        composable(NavRoutes.CALENDAR) {
            CalendarScreen(
                onNavigate = navigateToTab,
                onScheduleClick = { /* TODO: 일정 상세 화면 */ },
                onTaskClick = { /* TODO: 할 일 상세 화면 */ }
            )
        }

        // 노트 화면 (NOTES) - PRD v4.0
        // TODO: NotesScreen 구현 후 교체
        composable(NavRoutes.NOTES) {
            // 임시로 ArchiveScreen 사용
            ArchiveScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onInsightClick = navigateToResult,
                onNavigate = navigateToTab,
                onSearchClick = {
                    navController.navigate(NavRoutes.SEARCH)
                }
            )
        }

        // 노트 편집 화면 (NOTE_EDIT)
        composable(
            route = NavRoutes.NOTE_EDIT,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType }
            )
        ) {
            // TODO: NoteEditScreen 구현 후 교체
            // 임시로 뒤로가기
            navController.popBackStack()
        }

        // 검색 화면 (SEARCH)
        composable(NavRoutes.SEARCH) {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onInsightClick = navigateToResult,
                onNavigate = navigateToTab
            )
        }

        // 알림 화면 (NOTIFICATIONS)
        composable(NavRoutes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNotificationClick = { insightId ->
                    if (insightId != null) {
                        navigateToResult(insightId)
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        // 설정 화면 (SETTINGS)
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
