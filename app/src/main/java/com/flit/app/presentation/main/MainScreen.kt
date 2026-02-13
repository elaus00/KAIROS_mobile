package com.flit.app.presentation.main

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.flit.app.presentation.calendar.CalendarContent
import com.flit.app.presentation.capture.CaptureContent
import com.flit.app.presentation.capture.CaptureViewModel
import com.flit.app.presentation.components.common.FlitBottomNav
import com.flit.app.presentation.components.common.FlitTab
import com.flit.app.presentation.notes.NotesContent
import com.flit.app.ui.theme.FlitTheme
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
    initialTab: FlitTab = FlitTab.HOME,
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
    val colors = FlitTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }

    // 오프라인 상태 관찰
    val context = LocalContext.current
    var isOffline by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // 초기 상태 확인
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        isOffline = activeNetwork == null ||
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) != true ||
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED).not()

        // 기본 네트워크만 추적하여 다른 네트워크 전환 시 오프라인 오탐 방지
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isOffline = false }
            override fun onLost(network: Network) { isOffline = true }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        onDispose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    // Pager 상태 (HOME이 가운데 = index 1)
    val pagerState = rememberPagerState(
        initialPage = FlitTab.entries.indexOf(initialTab),
        pageCount = { FlitTab.entries.size }
    )

    // 현재 선택된 탭
    val currentTab by remember {
        derivedStateOf { FlitTab.entries[pagerState.currentPage] }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            FlitBottomNav(
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    scope.launch {
                        pagerState.animateScrollToPage(FlitTab.entries.indexOf(tab))
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.warning.copy(alpha = 0.15f))
                        .padding(vertical = 4.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "오프라인 상태입니다 · 연결되면 자동 동기화",
                        color = colors.warning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            // TODO: ViewModel의 수동 동기화 함수 호출
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "새로고침",
                            tint = colors.warning,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background),
                beyondViewportPageCount = 1
            ) { page ->
                when (FlitTab.entries[page]) {
                    FlitTab.NOTES -> NotesContent(
                        onSearchClick = onNavigateToSearch,
                        onNoteClick = { noteId -> onNavigateToNoteDetail(noteId) },
                        onTrashClick = onNavigateToTrash,
                        onReorganizeClick = onNavigateToReorganize
                    )

                    FlitTab.HOME -> CaptureContent(
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToHistory = onNavigateToHistory,
                        snackbarHostState = snackbarHostState,
                        autoFocusCapture = autoFocusCapture,
                        viewModel = captureViewModel
                    )

                    FlitTab.CALENDAR -> CalendarContent()
                }
            }
        }
    }
}
