package com.example.kairos_mobile.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import com.example.kairos_mobile.presentation.auth.LoginScreen
import com.example.kairos_mobile.presentation.notes.reorganize.ReorganizeScreen
import com.example.kairos_mobile.presentation.settings.SettingsScreen
import com.example.kairos_mobile.presentation.settings.TermsOfServiceScreen
import com.example.kairos_mobile.presentation.settings.analytics.AnalyticsDashboardScreen
import com.example.kairos_mobile.presentation.subscription.SubscriptionScreen
import com.example.kairos_mobile.presentation.trash.TrashScreen

/**
 * Navigation 경로 정의
 * HOME 화면에서 3탭 스와이프 네비게이션 포함
 * SETTINGS는 독립 화면
 */
object NavRoutes {
    // 메인
    const val HOME = "home"

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
    const val LOGIN = "login"
    const val SUBSCRIPTION = "subscription"
    const val REORGANIZE = "reorganize"
    const val ANALYTICS = "analytics"

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
 *
 * 전환 애니메이션:
 * - 계층 이동(Detail): 부분 슬라이드(30%) + 페이드 (300ms, EaseOut)
 * - 모달 화면: 미세 수직 슬라이드(5%) + 페이드 (250ms)
 * - 홈/온보딩: 전환 없음 (앱 진입점)
 */
@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun KairosNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.HOME,
    autoFocusCapture: Boolean = false
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
        startDestination = startDestination
    ) {
        // 온보딩 화면 (ONBOARDING) — 첫 실행 시만 표시
        composable(
            route = NavRoutes.ONBOARDING,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // 메인 화면 (HOME) - 스와이프 네비게이션 포함
        composable(
            route = NavRoutes.HOME,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            MainScreen(
                initialTab = KairosTab.HOME,
                autoFocusCapture = autoFocusCapture,
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
                },
                onNavigateToReorganize = {
                    navController.navigate(NavRoutes.REORGANIZE)
                }
            )
        }

        // 설정 화면 (SETTINGS) - 모달 전환
        composable(
            route = NavRoutes.SETTINGS,
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 20 }
                ) + fadeIn(animationSpec = tween(250))
            },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it / 20 }
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
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
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN)
                },
                onNavigateToSubscription = {
                    navController.navigate(NavRoutes.SUBSCRIPTION)
                },
                onNavigateToAnalytics = {
                    navController.navigate(NavRoutes.ANALYTICS)
                }
            )
        }

        // 로그인 화면 (LOGIN) - 모달 전환
        composable(
            route = NavRoutes.LOGIN,
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 20 }
                ) + fadeIn(animationSpec = tween(250))
            },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it / 20 }
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLoginSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // 구독 관리 화면 (SUBSCRIPTION) - 모달 전환
        composable(
            route = NavRoutes.SUBSCRIPTION,
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 20 }
                ) + fadeIn(animationSpec = tween(250))
            },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it / 20 }
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            SubscriptionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // AI 노트 재구성 화면 (REORGANIZE) - 모달 전환
        composable(
            route = NavRoutes.REORGANIZE,
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 20 }
                ) + fadeIn(animationSpec = tween(250))
            },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it / 20 }
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            ReorganizeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 분석 대시보드 화면 (ANALYTICS) - 모달 전환
        composable(
            route = NavRoutes.ANALYTICS,
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 20 }
                ) + fadeIn(animationSpec = tween(250))
            },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it / 20 }
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            AnalyticsDashboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 캡처 상세 화면 (DETAIL) - 계층 이동 전환 (부분 슬라이드 + 페이드)
        composable(
            route = NavRoutes.DETAIL,
            arguments = listOf(
                navArgument("captureId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { it / 3 }
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { -it / 3 }
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { -it / 3 }
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { it / 3 }
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            CaptureDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                }
            )
        }

        // 노트 상세 화면 (NOTE_DETAIL) - 계층 이동 전환 (부분 슬라이드 + 페이드)
        composable(
            route = NavRoutes.NOTE_DETAIL,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { it / 3 }
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { -it / 3 }
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { -it / 3 }
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { it / 3 }
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            NoteDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 검색 화면 (SEARCH) - 모달 전환
        composable(
            route = NavRoutes.SEARCH,
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 20 }
                ) + fadeIn(animationSpec = tween(250))
            },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it / 20 }
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCaptureClick = navigateToDetail,
                onNavigate = { navController.popBackStack() }
            )
        }

        // 전체 기록 화면 (HISTORY) - 모달 전환
        composable(
            route = NavRoutes.HISTORY,
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 20 }
                ) + fadeIn(animationSpec = tween(250))
            },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it / 20 }
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCaptureClick = navigateToDetail
            )
        }

        // 휴지통 화면 (TRASH) - 모달 전환
        composable(
            route = NavRoutes.TRASH,
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 20 }
                ) + fadeIn(animationSpec = tween(250))
            },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it / 20 }
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            TrashScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 개인정보 처리방침 화면 (PRIVACY_POLICY) - 모달 전환
        composable(
            route = NavRoutes.PRIVACY_POLICY,
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 20 }
                ) + fadeIn(animationSpec = tween(250))
            },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it / 20 }
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            PrivacyPolicyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 이용약관 화면 (TERMS_OF_SERVICE) - 모달 전환
        composable(
            route = NavRoutes.TERMS_OF_SERVICE,
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 20 }
                ) + fadeIn(animationSpec = tween(250))
            },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it / 20 }
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            TermsOfServiceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
