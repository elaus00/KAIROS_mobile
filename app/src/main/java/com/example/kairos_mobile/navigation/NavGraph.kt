package com.example.kairos_mobile.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kairos_mobile.presentation.components.common.KairosTab
import com.example.kairos_mobile.presentation.detail.CaptureDetailScreen
import com.example.kairos_mobile.presentation.history.HistoryScreen
import com.example.kairos_mobile.presentation.main.MainScreen
import com.example.kairos_mobile.presentation.onboarding.OnboardingScreen
import com.example.kairos_mobile.presentation.search.SearchScreen
import com.example.kairos_mobile.presentation.settings.PrivacyPolicyScreen
import com.example.kairos_mobile.presentation.notes.detail.NoteDetailScreen
import com.example.kairos_mobile.presentation.settings.SettingsScreen
import com.example.kairos_mobile.presentation.settings.TermsOfServiceScreen
import com.example.kairos_mobile.presentation.trash.TrashScreen

/**
 * Navigation 경로 정의
 * NOTES ← HOME → CALENDAR (3탭)
 * SETTINGS는 독립 화면
 */
object NavRoutes {
    // 메인 탭
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val NOTES = "notes"

    // 독립 화면
    const val SETTINGS = "settings"
    const val ONBOARDING = "onboarding"

    // 보조 화면
    const val DETAIL = "detail/{captureId}"
    const val NOTE_DETAIL = "note_detail/{noteId}"
    const val SEARCH = "search"
    const val HISTORY = "history"
    const val TRASH = "trash"
    const val PRIVACY_POLICY = "privacy-policy"
    const val TERMS_OF_SERVICE = "terms-of-service"

    /**
     * CaptureDetailScreen 라우트 생성
     */
    fun detail(captureId: String): String = "detail/$captureId"

    /**
     * NoteDetailScreen 라우트 생성
     */
    fun noteDetail(noteId: String): String = "note_detail/$noteId"
}

/**
 * KAIROS Navigation Graph
 * 3개 탭: NOTES ← HOME → CALENDAR
 * SETTINGS는 독립 화면
 */
@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun KairosNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.HOME
) {
    // 캡처 상세 화면으로 네비게이션
    val navigateToDetail: (String) -> Unit = { captureId ->
        navController.navigate(NavRoutes.detail(captureId))
    }

    // 노트 상세 화면으로 네비게이션
    val navigateToNoteDetail: (String) -> Unit = { noteId ->
        navController.navigate(NavRoutes.noteDetail(noteId))
    }

    // 검색 화면으로 네비게이션
    val navigateToSearch: () -> Unit = {
        navController.navigate(NavRoutes.SEARCH)
    }

    NavHost(
        navController = navController,
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        startDestination = startDestination,
        // 페이지 전환 애니메이션 제거
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // 온보딩 화면 (ONBOARDING) — 첫 실행 시만 표시
        composable(NavRoutes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // 메인 화면 (HOME) - 스와이프 네비게이션 포함
        composable(NavRoutes.HOME) {
            MainScreen(
                initialTab = KairosTab.HOME,
                onNavigateToCapture = navigateToDetail,
                onNavigateToSearch = navigateToSearch,
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                },
                onNavigateToHistory = {
                    navController.navigate(NavRoutes.HISTORY)
                },
                onNavigateToNoteDetail = navigateToNoteDetail,
                onNavigateToTrash = {
                    navController.navigate(NavRoutes.TRASH)
                }
            )
        }

        // 설정 화면 (SETTINGS) - 독립 화면
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPrivacyPolicy = {
                    navController.navigate(NavRoutes.PRIVACY_POLICY)
                },
                onNavigateToTermsOfService = {
                    navController.navigate(NavRoutes.TERMS_OF_SERVICE)
                },
                onNavigateToTrash = {
                    navController.navigate(NavRoutes.TRASH)
                }
            )
        }

        // 캡처 상세 화면 (DETAIL)
        composable(
            route = NavRoutes.DETAIL,
            arguments = listOf(
                navArgument("captureId") { type = NavType.StringType }
            )
        ) {
            CaptureDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 노트 상세 화면 (NOTE_DETAIL)
        composable(
            route = NavRoutes.NOTE_DETAIL,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType }
            )
        ) {
            NoteDetailScreen(
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
                onCaptureClick = navigateToDetail,
                onNavigate = { navController.popBackStack() }
            )
        }

        // 전체 기록 화면 (HISTORY)
        composable(NavRoutes.HISTORY) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCaptureClick = navigateToDetail
            )
        }

        // 휴지통 화면 (TRASH)
        composable(NavRoutes.TRASH) {
            TrashScreen(
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

        // 이용약관 화면 (TERMS_OF_SERVICE)
        composable(NavRoutes.TERMS_OF_SERVICE) {
            TermsOfServiceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
