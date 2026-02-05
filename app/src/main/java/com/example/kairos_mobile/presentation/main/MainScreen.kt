package com.example.kairos_mobile.presentation.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.domain.model.Bookmark
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.presentation.calendar.CalendarContent
import com.example.kairos_mobile.presentation.capture.QuickCapturePopup
import com.example.kairos_mobile.presentation.capture.QuickCaptureViewModel
import com.example.kairos_mobile.presentation.components.common.KairosBottomNav
import com.example.kairos_mobile.presentation.components.common.KairosTab
import com.example.kairos_mobile.presentation.home.HomeContent
import com.example.kairos_mobile.presentation.notes.NotesContent
import com.example.kairos_mobile.presentation.settings.SettingsContent
import com.example.kairos_mobile.ui.theme.KairosTheme
import kotlinx.coroutines.launch

/**
 * MainScreen (PRD v4.0)
 * HorizontalPager를 사용한 스와이프 네비게이션
 * HOME / CALENDAR / NOTES / SETTINGS 4개 탭
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    initialTab: KairosTab = KairosTab.HOME,
    onNavigateToCapture: (String) -> Unit,
    onNavigateToNoteEdit: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onOpenCamera: () -> Unit,
    quickCaptureViewModel: QuickCaptureViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val colors = KairosTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }

    // Pager 상태
    val pagerState = rememberPagerState(
        initialPage = KairosTab.entries.indexOf(initialTab),
        pageCount = { KairosTab.entries.size }
    )

    // 현재 선택된 탭
    val currentTab by remember {
        derivedStateOf { KairosTab.entries[pagerState.currentPage] }
    }

    // QuickCapture 팝업 표시 상태
    var showQuickCapturePopup by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                KairosBottomNav(
                    selectedTab = currentTab,
                    onTabSelected = { tab ->
                        scope.launch {
                            pagerState.animateScrollToPage(KairosTab.entries.indexOf(tab))
                        }
                    }
                )
            },
            floatingActionButton = {
                // FAB - HOME 탭에서만 표시
                if (currentTab == KairosTab.HOME) {
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
                }
            },
            containerColor = colors.background
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(colors.background),
                beyondViewportPageCount = 1 // 인접 페이지 미리 로드
            ) { page ->
                when (KairosTab.entries[page]) {
                    KairosTab.HOME -> HomeContent(
                        onNavigateToCapture = onNavigateToCapture,
                        onNavigateToCalendar = {
                            scope.launch {
                                pagerState.animateScrollToPage(KairosTab.CALENDAR.ordinal)
                            }
                        },
                        snackbarHostState = snackbarHostState
                    )

                    KairosTab.CALENDAR -> CalendarContent(
                        onScheduleClick = { /* TODO: 일정 상세 */ },
                        onTaskClick = { /* TODO: 할 일 상세 */ }
                    )

                    KairosTab.NOTES -> NotesContent(
                        onNoteClick = { note -> onNavigateToNoteEdit(note.id) },
                        onBookmarkClick = { /* TODO: 북마크 웹뷰 */ }
                    )

                    KairosTab.SETTINGS -> SettingsContent(
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                        snackbarHostState = snackbarHostState
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
