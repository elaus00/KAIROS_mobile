package com.example.kairos_mobile.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.presentation.calendar.components.MonthView
import com.example.kairos_mobile.presentation.calendar.components.ScheduleTimeline
import com.example.kairos_mobile.presentation.calendar.components.TaskList
import com.example.kairos_mobile.presentation.components.common.KairosBottomNav
import com.example.kairos_mobile.presentation.components.common.KairosTab
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * CalendarScreen (PRD v4.0)
 * 일정 + 할 일 통합 화면
 */
@Composable
fun CalendarScreen(
    onNavigate: (String) -> Unit,
    onScheduleClick: (Schedule) -> Unit = {},
    onTaskClick: (Todo) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors

    // 날짜 포맷터
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("M월 d일")
    }

    Scaffold(
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
            // 상단 날짜 표시
            DateHeader(
                dateText = "${uiState.selectedDate.format(dateFormatter)} ${
                    uiState.selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)
                }",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // 월간/주간 뷰
            MonthView(
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
                }
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
                    Spacer(modifier = Modifier.height(16.dp))

                    // 일정 타임라인
                    ScheduleTimeline(
                        schedules = uiState.schedules,
                        onScheduleClick = { schedule ->
                            viewModel.onEvent(CalendarEvent.ClickSchedule(schedule))
                            onScheduleClick(schedule)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 할 일 목록
                    TaskList(
                        tasks = uiState.tasks,
                        onTaskClick = { task ->
                            viewModel.onEvent(CalendarEvent.ClickTask(task))
                            onTaskClick(task)
                        },
                        onTaskComplete = { taskId ->
                            viewModel.onEvent(CalendarEvent.ToggleTaskComplete(taskId))
                        },
                        onTaskDelete = { taskId ->
                            viewModel.onEvent(CalendarEvent.DeleteTask(taskId))
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
 * 날짜 헤더
 */
@Composable
private fun DateHeader(
    dateText: String,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Text(
        text = dateText,
        color = colors.text,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}
