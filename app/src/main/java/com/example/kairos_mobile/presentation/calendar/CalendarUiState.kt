package com.example.kairos_mobile.presentation.calendar

import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.Todo
import java.time.LocalDate
import java.time.YearMonth

/**
 * CalendarScreen UI 상태 (PRD v4.0)
 */
data class CalendarUiState(
    // 선택된 날짜
    val selectedDate: LocalDate = LocalDate.now(),

    // 현재 표시 중인 월
    val currentMonth: YearMonth = YearMonth.now(),

    // 월간 뷰 펼침 상태
    val isMonthExpanded: Boolean = false,

    // 선택된 날짜의 일정 목록
    val schedules: List<Schedule> = emptyList(),

    // 선택된 날짜의 할 일 목록
    val tasks: List<Todo> = emptyList(),

    // 일정이 있는 날짜 목록 (dot 표시용)
    val datesWithSchedules: Set<LocalDate> = emptySet(),

    // 로딩 상태
    val isLoading: Boolean = false,

    // 에러 메시지
    val errorMessage: String? = null
)

/**
 * Calendar 화면 이벤트
 */
sealed interface CalendarEvent {
    data class SelectDate(val date: LocalDate) : CalendarEvent
    data class ChangeMonth(val yearMonth: YearMonth) : CalendarEvent
    data object ToggleMonthExpand : CalendarEvent
    data class ToggleTaskComplete(val taskId: String) : CalendarEvent
    data class DeleteTask(val taskId: String) : CalendarEvent
    data class ClickSchedule(val schedule: Schedule) : CalendarEvent
    data class ClickTask(val task: Todo) : CalendarEvent
}
