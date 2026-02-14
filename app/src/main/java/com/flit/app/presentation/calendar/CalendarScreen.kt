package com.flit.app.presentation.calendar

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flit.app.presentation.calendar.components.CalendarCard
import com.flit.app.presentation.calendar.components.ScheduleEditSheet
import com.flit.app.presentation.calendar.components.ScheduleTimeline
import com.flit.app.presentation.calendar.components.TaskList
import com.flit.app.presentation.components.common.FlitSnackbar
import com.flit.app.ui.theme.FlitTheme

/**
 * Calendar 화면 (ViewModel 소유)
 * MainScreen의 HorizontalPager에서 사용
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarContent(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 단발성 이벤트 수집 (ViewModel 의존)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CalendarUiEvent.DeleteSuccess -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "삭제했어요",
                        actionLabel = "실행 취소",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete(event.captureId)
                    }
                }
                CalendarUiEvent.UndoSuccess -> Unit
                is CalendarUiEvent.SyncApproved -> {
                    val message = if (event.calendarName != null) {
                        "'${event.calendarName}' 캘린더에 추가했어요"
                    } else {
                        "기기 캘린더에 추가했어요"
                    }
                    snackbarHostState.showSnackbar(message)
                }
                CalendarUiEvent.SyncRejected -> Unit
                is CalendarUiEvent.NavigateToDetail -> {
                    // 네비게이션은 MainScreen에서 연결
                }
            }
        }
    }

    CalendarContentBody(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onNavigateToDetail = { captureId -> viewModel.navigateToDetail(captureId) },
        modifier = modifier
    )
}

/**
 * Calendar 순수 UI (ViewModel 참조 없음)
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarContentBody(
    uiState: CalendarUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (CalendarEvent) -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    // 마감일 편집 DatePicker 상태: (todoId, currentMs)
    var deadlineEditState by remember { mutableStateOf<Pair<String, Long>?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 스크롤 콘텐츠
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 헤더 (스크롤과 함께 이동)
                Text(
                    text = "일정 및 할 일",
                    color = colors.text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

            CalendarCard(
                selectedDate = uiState.selectedDate,
                currentMonth = uiState.currentMonth,
                datesWithSchedules = uiState.datesWithSchedules,
                isExpanded = uiState.isMonthExpanded,
                onDateSelected = { date -> onEvent(CalendarEvent.SelectDate(date)) },
                onToggleExpand = { onEvent(CalendarEvent.ToggleMonthExpand) },
                onMonthChange = { yearMonth -> onEvent(CalendarEvent.ChangeMonth(yearMonth)) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = colors.accent,
                            modifier = Modifier.size(40.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "로드 중...",
                            color = colors.textMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))

                ScheduleTimeline(
                    schedules = uiState.schedules,
                    targetCalendarName = uiState.targetCalendarName,
                    onScheduleClick = { schedule ->
                        onEvent(CalendarEvent.StartEditSchedule(schedule))
                    },
                    onScheduleDelete = { captureId ->
                        onEvent(CalendarEvent.DeleteSchedule(captureId))
                    },
                    onReorder = { scheduleIds ->
                        onEvent(CalendarEvent.ReorderSchedules(scheduleIds))
                    },
                    onApproveSuggestion = { scheduleId ->
                        onEvent(CalendarEvent.ApproveSuggestion(scheduleId))
                    },
                    onRejectSuggestion = { scheduleId ->
                        onEvent(CalendarEvent.RejectSuggestion(scheduleId))
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                TaskList(
                    tasks = uiState.tasks,
                    completedTasks = uiState.completedTasks,
                    showCompleted = uiState.showCompleted,
                    onTaskComplete = { taskId ->
                        onEvent(CalendarEvent.ToggleTaskComplete(taskId))
                    },
                    onTaskDelete = { captureId ->
                        onEvent(CalendarEvent.DeleteTask(captureId))
                    },
                    onReorder = { todoIds ->
                        onEvent(CalendarEvent.ReorderTasks(todoIds))
                    },
                    onToggleShowCompleted = {
                        onEvent(CalendarEvent.ToggleShowCompleted)
                    },
                    onTaskClick = { captureId ->
                        onNavigateToDetail(captureId)
                    },
                    onDeadlineEdit = { todoId, currentDeadlineMs ->
                        deadlineEditState = todoId to currentDeadlineMs
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
            }
        }

        // 커스텀 Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            snackbar = { data -> FlitSnackbar(snackbarData = data) }
        )
    }

    // 일정 편집 BottomSheet
    uiState.editingSchedule?.let { schedule ->
        ScheduleEditSheet(
            schedule = schedule,
            onSave = { editEvent -> onEvent(editEvent) },
            onDismiss = { onEvent(CalendarEvent.DismissEditSchedule) }
        )
    }

    // 마감일 편집 DatePicker
    deadlineEditState?.let { (todoId, currentMs) ->
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentMs
        )
        DatePickerDialog(
            onDismissRequest = { deadlineEditState = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedMs ->
                        onEvent(CalendarEvent.UpdateTaskDeadline(todoId, selectedMs))
                    }
                    deadlineEditState = null
                }) { Text("확인", color = colors.accent) }
            },
            dismissButton = {
                TextButton(onClick = { deadlineEditState = null }) {
                    Text("취소", color = colors.textSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CalendarContentBodyPreview() {
    FlitTheme {
        CalendarContentBody(uiState = CalendarUiState())
    }
}
