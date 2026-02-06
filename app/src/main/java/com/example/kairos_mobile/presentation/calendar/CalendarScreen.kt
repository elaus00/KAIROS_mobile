package com.example.kairos_mobile.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.presentation.calendar.components.CalendarCard
import com.example.kairos_mobile.presentation.calendar.components.ScheduleTimeline
import com.example.kairos_mobile.presentation.calendar.components.TaskList
import com.example.kairos_mobile.presentation.components.common.KairosBottomNav
import com.example.kairos_mobile.presentation.components.common.KairosTab
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * CalendarScreen
 * 일정 + 할 일 통합 화면
 */
@Composable
fun CalendarScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CalendarUiEvent.DeleteSuccess -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "삭제되었습니다",
                        actionLabel = "실행 취소",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete(event.captureId)
                    }
                }
                CalendarUiEvent.UndoSuccess -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            KairosBottomNav(
                selectedTab = KairosTab.CALENDAR,
                onTabSelected = { tab ->
                    onNavigate(tab.route)
                }
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            // 화면 타이틀
            Text(
                text = "Calendar",
                color = colors.text,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            // 캘린더 카드 (날짜 헤더 + 주간/월간 뷰 포함)
            CalendarCard(
                selectedDate = uiState.selectedDate,
                datesWithSchedules = uiState.datesWithSchedules,
                isExpanded = uiState.isMonthExpanded,
                onDateSelected = { date ->
                    viewModel.onEvent(CalendarEvent.SelectDate(date))
                },
                onToggleExpand = {
                    viewModel.onEvent(CalendarEvent.ToggleMonthExpand)
                },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            // 로딩 상태
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colors.accent,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                // 스크롤 가능한 컨텐츠
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // 일정 타임라인
                    ScheduleTimeline(
                        schedules = uiState.schedules,
                        onScheduleClick = { /* 상세 네비게이션은 Phase 1-10 */ },
                        onScheduleDelete = { captureId ->
                            viewModel.onEvent(CalendarEvent.DeleteSchedule(captureId))
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 할 일 목록
                    TaskList(
                        tasks = uiState.tasks,
                        onTaskComplete = { taskId ->
                            viewModel.onEvent(CalendarEvent.ToggleTaskComplete(taskId))
                        },
                        onTaskDelete = { captureId ->
                            viewModel.onEvent(CalendarEvent.DeleteTask(captureId))
                        }
                    )

                    // 하단 여백
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Calendar 화면 내용 (Scaffold 없이)
 * MainScreen의 HorizontalPager에서 사용
 */
@Composable
fun CalendarContent(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CalendarUiEvent.DeleteSuccess -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "삭제되었습니다",
                        actionLabel = "실행 취소",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete(event.captureId)
                    }
                }
                CalendarUiEvent.UndoSuccess -> Unit
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 화면 타이틀
            Text(
                text = "Calendar",
                color = colors.text,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            // 캘린더 카드 (날짜 헤더 + 주간/월간 뷰 포함)
            CalendarCard(
                selectedDate = uiState.selectedDate,
                datesWithSchedules = uiState.datesWithSchedules,
                isExpanded = uiState.isMonthExpanded,
                onDateSelected = { date ->
                    viewModel.onEvent(CalendarEvent.SelectDate(date))
                },
                onToggleExpand = {
                    viewModel.onEvent(CalendarEvent.ToggleMonthExpand)
                },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            // 로딩 상태
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colors.accent,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                // 스크롤 가능한 컨텐츠
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // 일정 타임라인
                    ScheduleTimeline(
                        schedules = uiState.schedules,
                        onScheduleClick = { /* 상세 네비게이션은 Phase 1-10 */ },
                        onScheduleDelete = { captureId ->
                            viewModel.onEvent(CalendarEvent.DeleteSchedule(captureId))
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 할 일 목록
                    TaskList(
                        tasks = uiState.tasks,
                        onTaskComplete = { taskId ->
                            viewModel.onEvent(CalendarEvent.ToggleTaskComplete(taskId))
                        },
                        onTaskDelete = { captureId ->
                            viewModel.onEvent(CalendarEvent.DeleteTask(captureId))
                        }
                    )

                    // 하단 여백
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
