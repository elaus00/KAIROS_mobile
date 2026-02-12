package com.flit.app.presentation.settings.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.model.CalendarException
import com.flit.app.domain.model.FontSizePreference
import com.flit.app.domain.model.LocalCalendar
import com.flit.app.domain.repository.CalendarRepository
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.settings.CalendarSettingsKeys
import com.flit.app.domain.usecase.settings.GetCalendarSettingsUseCase
import com.flit.app.domain.usecase.settings.PreferenceKeys
import com.flit.app.domain.usecase.settings.SetCalendarSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 캘린더 설정 세부 화면 ViewModel
 */
@HiltViewModel
class CalendarSettingsViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val getCalendarSettingsUseCase: GetCalendarSettingsUseCase,
    private val setCalendarSettingsUseCase: SetCalendarSettingsUseCase,
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarSettingsUiState())
    val uiState: StateFlow<CalendarSettingsUiState> = _uiState.asStateFlow()

    init {
        loadCalendarSettings()
        loadAvailableCalendars()
        loadCaptureFontSize()
    }

    /** 캘린더 설정 로드 */
    private fun loadCalendarSettings() {
        viewModelScope.launch {
            val mode = getCalendarSettingsUseCase.getCalendarMode()
            val notificationEnabled = getCalendarSettingsUseCase.isNotificationEnabled()
            _uiState.update {
                it.copy(
                    isAutoAddEnabled = mode == CalendarSettingsKeys.MODE_AUTO,
                    isNotificationEnabled = notificationEnabled
                )
            }
        }
    }

    /** 사용 가능한 캘린더 목록 로드 */
    private fun loadAvailableCalendars() {
        viewModelScope.launch {
            runCatching {
                val calendars = calendarRepository.getAvailableCalendars()
                val selected = normalizeSelectedCalendar(calendars)
                calendars to selected
            }.onSuccess { (calendars, selectedId) ->
                _uiState.update {
                    it.copy(
                        availableCalendars = calendars,
                        selectedCalendarId = selectedId
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = mapCalendarErrorMessage(throwable))
                }
            }
        }
    }

    /** 대상 캘린더 선택 */
    fun setTargetCalendar(calendarId: Long) {
        viewModelScope.launch {
            runCatching {
                calendarRepository.setTargetCalendarId(calendarId)
            }.onSuccess {
                _uiState.update { it.copy(selectedCalendarId = calendarId) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = mapCalendarErrorMessage(throwable))
                }
            }
        }
    }

    /** 자동 추가 토글 (on=auto, off=suggest) */
    fun toggleAutoAdd(enabled: Boolean) {
        val mode = if (enabled) CalendarSettingsKeys.MODE_AUTO else CalendarSettingsKeys.MODE_SUGGEST
        viewModelScope.launch {
            setCalendarSettingsUseCase.setCalendarMode(mode)
            _uiState.update { it.copy(isAutoAddEnabled = enabled) }
        }
    }

    /** 알림 토글 */
    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch {
            setCalendarSettingsUseCase.setNotificationEnabled(enabled)
            _uiState.update { it.copy(isNotificationEnabled = enabled) }
        }
    }

    /** 캘린더 목록 새로고침 */
    fun reloadAvailableCalendars() {
        loadAvailableCalendars()
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun loadCaptureFontSize() {
        viewModelScope.launch {
            val size = userPreferenceRepository.getString(
                PreferenceKeys.KEY_CAPTURE_FONT_SIZE,
                FontSizePreference.MEDIUM.name
            )
            _uiState.update { it.copy(captureFontSize = size) }
        }
    }

    private suspend fun normalizeSelectedCalendar(calendars: List<LocalCalendar>): Long? {
        if (calendars.isEmpty()) return null
        val selected = calendarRepository.getTargetCalendarId()
        if (selected != null && calendars.any { it.id == selected }) return selected
        val fallback = calendars.firstOrNull { it.isPrimary } ?: calendars.first()
        calendarRepository.setTargetCalendarId(fallback.id)
        return fallback.id
    }

    private fun mapCalendarErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is CalendarException.PermissionDenied -> "캘린더 권한이 필요합니다."
            is CalendarException.NoCalendarSelected -> "연동할 캘린더를 선택해주세요."
            is CalendarException.InsertFailed -> throwable.message ?: "캘린더 일정 추가에 실패했습니다."
            is CalendarException.Unknown -> throwable.message ?: "캘린더 요청 실패"
            else -> throwable.message ?: "요청 실패"
        }
    }
}
