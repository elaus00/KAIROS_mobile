package com.example.kairos_mobile.presentation.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.presentation.calendar.components.CalendarCard
import com.example.kairos_mobile.presentation.calendar.components.ScheduleTimeline
import com.example.kairos_mobile.presentation.calendar.components.TaskList
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * Calendar 화면 내용 (Scaffold 없이)
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
    val colors = KairosTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTask by remember { mutableStateOf<TodoDisplayItem?>(null) }

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
                is CalendarUiEvent.SyncApproved -> {
                    val message = if (event.calendarName != null) {
                        "'${event.calendarName}' 캘린더에 추가되었습니다"
                    } else {
                        "기기 캘린더에 추가되었습니다"
                    }
                    snackbarHostState.showSnackbar(message)
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
        // 월간/주간 모두 캘린더 카드가 콘텐츠와 함께 스크롤
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

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
                    completedTasks = uiState.completedTasks,
                    showCompleted = uiState.showCompleted,
                    onTaskComplete = { taskId ->
                        viewModel.onEvent(CalendarEvent.ToggleTaskComplete(taskId))
                    },
                    onTaskDelete = { captureId ->
                        viewModel.onEvent(CalendarEvent.DeleteTask(captureId))
                    },
                    onReorder = { todoIds ->
                        viewModel.onEvent(CalendarEvent.ReorderTasks(todoIds))
                    },
                    onToggleShowCompleted = {
                        viewModel.onEvent(CalendarEvent.ToggleShowCompleted)
                    },
                    onTaskAction = { task -> selectedTask = task }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // 할일 액션 시트
    selectedTask?.let { task ->
        ModalBottomSheet(
            onDismissRequest = { selectedTask = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = colors.background
        ) {
            TaskActionSheet(
                task = task,
                onDelete = {
                    viewModel.onEvent(CalendarEvent.DeleteTask(task.captureId))
                    selectedTask = null
                },
                onDismiss = { selectedTask = null }
            )
        }
    }
}

/**
 * 할일 액션 시트 내용
 */
@Composable
private fun TaskActionSheet(
    task: TodoDisplayItem,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KairosTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // 할일 제목
        Text(
            text = task.title,
            color = colors.text,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )

        // 마감일 정보
        task.deadline?.let { deadlineMs ->
            Spacer(modifier = Modifier.height(6.dp))
            val dateTime = java.time.Instant.ofEpochMilli(deadlineMs)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
            val formatter = java.time.format.DateTimeFormatter.ofPattern("M/d HH:mm")
            val isOverdue = deadlineMs < System.currentTimeMillis()
            Text(
                text = "${dateTime.format(formatter)} 마감",
                color = if (isOverdue) colors.danger else colors.textSecondary,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = colors.divider)
        Spacer(modifier = Modifier.height(8.dp))

        // 삭제 버튼
        TextButton(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "삭제",
                color = colors.danger,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
