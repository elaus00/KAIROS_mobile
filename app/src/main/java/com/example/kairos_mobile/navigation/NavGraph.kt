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
import com.example.kairos_mobile.presentation.capture.QuickCaptureOverlay
import com.example.kairos_mobile.presentation.components.common.KairosTab
import com.example.kairos_mobile.presentation.main.MainScreen
import com.example.kairos_mobile.presentation.notes.edit.NoteEditScreen
import com.example.kairos_mobile.presentation.notifications.NotificationsScreen
import com.example.kairos_mobile.presentation.result.ResultScreen
import com.example.kairos_mobile.presentation.search.SearchScreen
import com.example.kairos_mobile.presentation.settings.PrivacyPolicyScreen
import com.example.kairos_mobile.presentation.settings.ProfileScreen

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
    const val RESULT = "result/{captureId}"
    const val NOTE_EDIT = "notes/{noteId}"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val PRIVACY_POLICY = "privacy-policy"
    const val QUICK_CAPTURE = "quick-capture"

    /**
     * ResultScreen 라우트 생성
     */
    fun result(captureId: String): String = "result/$captureId"

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
    startDestination: String = NavRoutes.HOME,
    sharedText: String? = null,
    sharedImageUri: Uri? = null
) {
    // ResultScreen으로 네비게이션
    val navigateToResult: (String) -> Unit = { captureId ->
        navController.navigate(NavRoutes.result(captureId))
    }

    // NoteEditScreen으로 네비게이션
    val navigateToNoteEdit: (String) -> Unit = { noteId ->
        navController.navigate(NavRoutes.noteEdit(noteId))
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        // 페이지 전환 애니메이션 제거
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // 메인 화면 (HOME) - 스와이프 네비게이션 포함
        composable(NavRoutes.HOME) {
            MainScreen(
                initialTab = KairosTab.HOME,
                onNavigateToCapture = navigateToResult,
                onNavigateToNoteEdit = navigateToNoteEdit,
                onNavigateToProfile = {
                    navController.navigate(NavRoutes.PROFILE)
                },
                onNavigateToPrivacyPolicy = {
                    navController.navigate(NavRoutes.PRIVACY_POLICY)
                },
                onOpenCamera = {
                    // TODO: 카메라 열기 구현
                }
            )
        }

        // 결과 화면 (RESULT)
        composable(
            route = NavRoutes.RESULT,
            arguments = listOf(
                navArgument("captureId") { type = NavType.StringType }
            )
        ) {
            ResultScreen(
                onNavigateBack = {
                    navController.popBackStack()
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
            NoteEditScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 검색 화면 (SEARCH)
        composable(NavRoutes.SEARCH) {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCaptureClick = navigateToResult,
                onNavigate = { navController.popBackStack() }
            )
        }

        // 알림 화면 (NOTIFICATIONS)
        composable(NavRoutes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNotificationClick = { captureId ->
                    if (captureId != null) {
                        navigateToResult(captureId)
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        // 프로필 화면 (PROFILE)
        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 개인정보 처리방침 화면 (PRIVACY_POLICY)
        composable(NavRoutes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // QuickCapture 오버레이 화면 (앱 시작 시)
        composable(NavRoutes.QUICK_CAPTURE) {
            QuickCaptureOverlay(
                onDismiss = {
                    // HOME으로 이동 (백스택 정리)
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.QUICK_CAPTURE) { inclusive = true }
                    }
                },
                onOpenCamera = {
                    // TODO: 카메라 열기 구현
                },
                initialText = sharedText
            )
        }
    }
}
