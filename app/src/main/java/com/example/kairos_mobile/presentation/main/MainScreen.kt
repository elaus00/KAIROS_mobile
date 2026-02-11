package com.example.kairos_mobile.presentation.main

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    autoFocusCapture: Boolean = false,
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

    // 오프라인 상태 관찰
    val context = LocalContext.current
    var isOffline by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // 초기 상태 확인
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        isOffline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) != true

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isOffline = false }
            override fun onLost(network: Network) { isOffline = true }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        onDispose { connectivityManager.unregisterNetworkCallback(callback) }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 오프라인 배너
            AnimatedVisibility(
                visible = isOffline,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.warning.copy(alpha = 0.15f))
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "오프라인 상태입니다 · 연결되면 자동 동기화",
                        color = colors.warning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
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
                        autoFocusCapture = autoFocusCapture,
                        viewModel = captureViewModel
                    )

                    KairosTab.CALENDAR -> CalendarContent()
                }
            }
        }
    }
}
