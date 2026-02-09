package com.example.kairos_mobile.presentation.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.presentation.calendar.CalendarContent
import com.example.kairos_mobile.presentation.capture.CaptureContent
import com.example.kairos_mobile.presentation.capture.CaptureViewModel
import com.example.kairos_mobile.presentation.components.common.KairosBottomNav
import com.example.kairos_mobile.presentation.components.common.KairosTab
import com.example.kairos_mobile.presentation.notes.NotesContent
import com.example.kairos_mobile.ui.theme.KairosTheme
import kotlinx.coroutines.launch

/**
 * MainScreen
 * HorizontalPager를 사용한 스와이프 네비게이션
 * NOTES(0) ← HOME(1) → CALENDAR(2) 3개 탭
 * HOME 탭이 가운데, 바로 캡처 입력 가능
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    initialTab: KairosTab = KairosTab.HOME,
    onNavigateToCapture: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToNoteDetail: (String) -> Unit = {},
    onNavigateToTrash: () -> Unit = {},
    onNavigateToReorganize: () -> Unit = {},
    captureViewModel: CaptureViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val colors = KairosTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }

    // Pager 상태 (HOME이 가운데 = index 1)
    val pagerState = rememberPagerState(
        initialPage = KairosTab.entries.indexOf(initialTab),
        pageCount = { KairosTab.entries.size }
    )

    // 현재 선택된 탭
    val currentTab by remember {
        derivedStateOf { KairosTab.entries[pagerState.currentPage] }
    }

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
        containerColor = colors.background
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background),
            beyondViewportPageCount = 1
        ) { page ->
            when (KairosTab.entries[page]) {
                KairosTab.NOTES -> NotesContent(
                    onSearchClick = onNavigateToSearch,
                    onNoteClick = { noteId -> onNavigateToNoteDetail(noteId) },
                    onTrashClick = onNavigateToTrash,
                    onReorganizeClick = onNavigateToReorganize
                )

                KairosTab.HOME -> CaptureContent(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToHistory = onNavigateToHistory,
                    snackbarHostState = snackbarHostState,
                    viewModel = captureViewModel
                )

                KairosTab.CALENDAR -> CalendarContent()
            }
        }
    }
}
