package com.flit.app.presentation.calendar

import com.flit.app.domain.model.CalendarSyncStatus
import java.time.LocalDate
import java.time.YearMonth

/**
 * CalendarScreen UI 상태
 */
data class CalendarUiState(
    // 선택된 날짜 (null = 선택 없음, 다른 달 탐색 시)
    val selectedDate: LocalDate? = LocalDate.now(),

    // 현재 표시 중인 월
    val currentMonth: YearMonth = YearMonth.now(),

    // 월간 뷰 펼침 상태
    val isMonthExpanded: Boolean = false,

    // 선택된 날짜의 일정 목록 (표시용)
    val schedules: List<ScheduleDisplayItem> = emptyList(),

    // 미완료 할 일 목록
    val tasks: List<TodoDisplayItem> = emptyList(),

    // 완료된 할 일 목록
    val completedTasks: List<TodoDisplayItem> = emptyList(),

    // 완료 항목 표시 여부
    val showCompleted: Boolean = false,

    // 일정이 있는 날짜 목록 (dot 표시용)
    val datesWithSchedules: Set<LocalDate> = emptySet(),

    // 로딩 상태
    val isLoading: Boolean = false,

    // 연동 캘린더 이름 (배지/Snackbar 표시용)
    val targetCalendarName: String? = null,

    // 편집 중인 일정 (BottomSheet 표시)
    val editingSchedule: ScheduleDisplayItem? = null,

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
    val isAllDay: Boolean,
    /** 기기 캘린더 동기화 상태 */
    val calendarSyncStatus: CalendarSyncStatus = CalendarSyncStatus.NOT_LINKED
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
    val isCompleted: Boolean,
    /** 마감일 소스 (AI / USER) */
    val deadlineSource: String? = null,
    /** 캡처 원문 (AI 제목과 다를 때 확장 영역 표시) */
    val originalText: String? = null
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
    /** 캘린더 제안 승인 */
    data class ApproveSuggestion(val scheduleId: String) : CalendarEvent
    /** 캘린더 제안 거부 */
    data class RejectSuggestion(val scheduleId: String) : CalendarEvent
    /** 완료 항목 표시 토글 */
    data object ToggleShowCompleted : CalendarEvent
    /** 할 일 드래그 순서 변경 */
    data class ReorderTasks(val todoIds: List<String>) : CalendarEvent
    /** 할 일 마감일 업데이트 */
    data class UpdateTaskDeadline(val todoId: String, val deadlineMs: Long) : CalendarEvent
    /** 일정 편집 시작 */
    data class StartEditSchedule(val schedule: ScheduleDisplayItem) : CalendarEvent
    /** 일정 편집 저장 */
    data class EditSchedule(
        val scheduleId: String,
        val title: String,
        val startTime: Long?,
        val endTime: Long?,
        val location: String?,
        val isAllDay: Boolean
    ) : CalendarEvent
    /** 일정 편집 닫기 */
    data object DismissEditSchedule : CalendarEvent
    /** 일정 드래그 순서 변경 (메모리 전용) */
    data class ReorderSchedules(val scheduleIds: List<String>) : CalendarEvent
}

/**
 * Calendar 화면 단발성 이벤트
 */
sealed class CalendarUiEvent {
    data class DeleteSuccess(val captureId: String) : CalendarUiEvent()
    data object UndoSuccess : CalendarUiEvent()
    /** 캘린더 동기화 승인 완료 */
    data class SyncApproved(val calendarName: String? = null) : CalendarUiEvent()
    /** 캘린더 동기화 거부 완료 */
    data object SyncRejected : CalendarUiEvent()
    /** 상세 화면 네비게이션 */
    data class NavigateToDetail(val captureId: String) : CalendarUiEvent()
}
