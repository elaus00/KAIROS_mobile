package com.example.kairos_mobile.presentation.calendar

import java.time.LocalDate
import java.time.YearMonth

/**
 * CalendarScreen UI 상태
 */
data class CalendarUiState(
    // 선택된 날짜
    val selectedDate: LocalDate = LocalDate.now(),

    // 현재 표시 중인 월
    val currentMonth: YearMonth = YearMonth.now(),

    // 월간 뷰 펼침 상태
    val isMonthExpanded: Boolean = false,

    // 선택된 날짜의 일정 목록 (표시용)
    val schedules: List<ScheduleDisplayItem> = emptyList(),

    // 할 일 목록 (전체 활성)
    val tasks: List<TodoDisplayItem> = emptyList(),

    // 일정이 있는 날짜 목록 (dot 표시용)
    val datesWithSchedules: Set<LocalDate> = emptySet(),

    // 로딩 상태
    val isLoading: Boolean = false,

    // 에러 메시지
    val errorMessage: String? = null
)

/**
 * 일정 표시용 모델 (Schedule + Capture aiTitle)
 */
data class ScheduleDisplayItem(
    val scheduleId: String,
    val captureId: String,
    /** AI 생성 제목 (Capture.aiTitle) */
    val title: String,
    /** 시작 시간 (epoch ms) */
    val startTime: Long?,
    /** 종료 시간 (epoch ms) */
    val endTime: Long?,
    /** 장소 */
    val location: String?,
    /** 종일 이벤트 여부 */
    val isAllDay: Boolean
)

/**
 * 할 일 표시용 모델 (Todo + Capture aiTitle)
 */
data class TodoDisplayItem(
    val todoId: String,
    val captureId: String,
    /** AI 생성 제목 (Capture.aiTitle) */
    val title: String,
    /** 마감 시각 (epoch ms) */
    val deadline: Long?,
    /** 완료 여부 */
    val isCompleted: Boolean
)

/**
 * Calendar 화면 이벤트
 */
sealed interface CalendarEvent {
    data class SelectDate(val date: LocalDate) : CalendarEvent
    data class ChangeMonth(val yearMonth: YearMonth) : CalendarEvent
    data object ToggleMonthExpand : CalendarEvent
    data class ToggleTaskComplete(val taskId: String) : CalendarEvent
    data class DeleteTask(val captureId: String) : CalendarEvent
    data class DeleteSchedule(val captureId: String) : CalendarEvent
}

/**
 * Calendar 화면 단발성 이벤트
 */
sealed class CalendarUiEvent {
    data class DeleteSuccess(val captureId: String) : CalendarUiEvent()
    data object UndoSuccess : CalendarUiEvent()
}
