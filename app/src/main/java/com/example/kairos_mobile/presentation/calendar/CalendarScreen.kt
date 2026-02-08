package com.example.kairos_mobile.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.presentation.calendar.components.CalendarCard
import com.example.kairos_mobile.presentation.calendar.components.ScheduleTimeline
import com.example.kairos_mobile.presentation.calendar.components.TaskList
import com.example.kairos_mobile.ui.theme.KairosTheme

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
                CalendarUiEvent.SyncApproved -> {
                    snackbarHostState.showSnackbar("캘린더에 추가되었습니다")
                }
                CalendarUiEvent.SyncRejected -> Unit
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        if (uiState.isMonthExpanded) {
            // 월간 확장 시: 캘린더 카드도 스크롤에 포함
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                CalendarCard(
                    selectedDate = uiState.selectedDate,
                    datesWithSchedules = uiState.datesWithSchedules,
                    isExpanded = true,
                    onDateSelected = { date ->
                        viewModel.onEvent(CalendarEvent.SelectDate(date))
                    },
                    onToggleExpand = {
                        viewModel.onEvent(CalendarEvent.ToggleMonthExpand)
                    },
                    onMonthChange = { yearMonth ->
                        viewModel.onEvent(CalendarEvent.ChangeMonth(yearMonth))
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colors.accent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))

                    ScheduleTimeline(
                        schedules = uiState.schedules,
                        onScheduleClick = { /* 상세 네비게이션은 Phase 1-10 */ },
                        onScheduleDelete = { captureId ->
                            viewModel.onEvent(CalendarEvent.DeleteSchedule(captureId))
                        },
                        onApproveSuggestion = { scheduleId ->
                            viewModel.onEvent(CalendarEvent.ApproveSuggestion(scheduleId))
                        },
                        onRejectSuggestion = { scheduleId ->
                            viewModel.onEvent(CalendarEvent.RejectSuggestion(scheduleId))
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TaskList(
                        tasks = uiState.tasks,
                        onTaskComplete = { taskId ->
                            viewModel.onEvent(CalendarEvent.ToggleTaskComplete(taskId))
                        },
                        onTaskDelete = { captureId ->
                            viewModel.onEvent(CalendarEvent.DeleteTask(captureId))
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            // 주간 뷰: 캘린더 카드 상단 고정
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                CalendarCard(
                    selectedDate = uiState.selectedDate,
                    datesWithSchedules = uiState.datesWithSchedules,
                    isExpanded = false,
                    onDateSelected = { date ->
                        viewModel.onEvent(CalendarEvent.SelectDate(date))
                    },
                    onToggleExpand = {
                        viewModel.onEvent(CalendarEvent.ToggleMonthExpand)
                    },
                    onMonthChange = { yearMonth ->
                        viewModel.onEvent(CalendarEvent.ChangeMonth(yearMonth))
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        ScheduleTimeline(
                            schedules = uiState.schedules,
                            onScheduleClick = { /* 상세 네비게이션은 Phase 1-10 */ },
                            onScheduleDelete = { captureId ->
                                viewModel.onEvent(CalendarEvent.DeleteSchedule(captureId))
                            },
                            onApproveSuggestion = { scheduleId ->
                                viewModel.onEvent(CalendarEvent.ApproveSuggestion(scheduleId))
                            },
                            onRejectSuggestion = { scheduleId ->
                                viewModel.onEvent(CalendarEvent.RejectSuggestion(scheduleId))
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        TaskList(
                            tasks = uiState.tasks,
                            onTaskComplete = { taskId ->
                                viewModel.onEvent(CalendarEvent.ToggleTaskComplete(taskId))
                            },
                            onTaskDelete = { captureId ->
                                viewModel.onEvent(CalendarEvent.DeleteTask(captureId))
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
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
