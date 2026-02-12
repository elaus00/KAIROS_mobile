package com.flit.app.presentation.settings.calendar

import com.flit.app.domain.model.FontSizePreference
import com.flit.app.domain.model.LocalCalendar

/**
 * 캘린더 설정 세부 화면 UI 상태
 */
data class CalendarSettingsUiState(
    val availableCalendars: List<LocalCalendar> = emptyList(),
    val selectedCalendarId: Long? = null,
    val captureFontSize: String = FontSizePreference.MEDIUM.name,
    /** 자동 추가 활성화 여부 (true=auto, false=suggest) */
    val isAutoAddEnabled: Boolean = false,
    val isNotificationEnabled: Boolean = true,
    val errorMessage: String? = null
)
