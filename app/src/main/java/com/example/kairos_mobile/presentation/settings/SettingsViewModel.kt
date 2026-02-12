package com.example.kairos_mobile.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.CalendarException
import com.example.kairos_mobile.domain.model.LocalCalendar
import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.repository.AuthRepository
import com.example.kairos_mobile.domain.repository.CalendarRepository
import com.example.kairos_mobile.domain.repository.ImageRepository
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import com.example.kairos_mobile.domain.usecase.capture.SubmitCaptureUseCase
import com.example.kairos_mobile.domain.usecase.classification.GetPresetsUseCase
import com.example.kairos_mobile.domain.usecase.classification.SetCustomInstructionUseCase
import com.example.kairos_mobile.domain.usecase.classification.SetPresetUseCase
import com.example.kairos_mobile.domain.usecase.settings.GetCalendarSettingsUseCase
import com.example.kairos_mobile.domain.usecase.settings.SetCalendarSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Settings 화면 ViewModel
 * 테마/캘린더/알림/계정/구독 설정 관리
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val calendarRepository: CalendarRepository,
    private val getCalendarSettingsUseCase: GetCalendarSettingsUseCase,
    private val setCalendarSettingsUseCase: SetCalendarSettingsUseCase,
    private val submitCaptureUseCase: SubmitCaptureUseCase,
    private val imageRepository: ImageRepository,
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val getPresetsUseCase: GetPresetsUseCase,
    private val setPresetUseCase: SetPresetUseCase,
    private val setCustomInstructionUseCase: SetCustomInstructionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    companion object {
        private const val KEY_CAPTURE_FONT_SIZE = "capture_font_size"
    }

    init {
        loadPreferences()
        loadCalendarSettings()
        refreshCalendarPermissionState()
        loadAccountInfo()
        loadPresets()
        loadCaptureFontSize()
    }

    /** 설정 값 로드 */
    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferenceRepository.getThemePreference().collect { theme ->
                _uiState.update { it.copy(themePreference = theme) }
            }
        }
    }

    /** 캘린더 설정 로드 */
    private fun loadCalendarSettings() {
        viewModelScope.launch {
            val calendarEnabled = getCalendarSettingsUseCase.isCalendarEnabled()
            val calendarMode = getCalendarSettingsUseCase.getCalendarMode()
            val notificationEnabled = getCalendarSettingsUseCase.isNotificationEnabled()
            _uiState.update {
                it.copy(
                    isCalendarEnabled = calendarEnabled,
                    calendarMode = calendarMode,
                    isNotificationEnabled = notificationEnabled
                )
            }
        }
    }

    /** 계정 정보 로드 */
    private fun loadAccountInfo() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val tier = subscriptionRepository.getCachedTier()
            val features = subscriptionRepository.getCachedFeatures()
            _uiState.update {
                it.copy(user = user, subscriptionTier = tier, features = features)
            }
        }
    }

    /** 분류 프리셋 로드 */
    private fun loadPresets() {
        val presets = getPresetsUseCase()
        viewModelScope.launch {
            val currentPresetId = userPreferenceRepository.getString("classification_preset_id", "default")
            val currentInstruction = userPreferenceRepository.getString("classification_custom_instruction", "")
            _uiState.update {
                it.copy(
                    presets = presets,
                    selectedPresetId = currentPresetId,
                    customInstruction = currentInstruction
                )
            }
        }
    }

    /** 캡처 글씨 크기 로드 */
    private fun loadCaptureFontSize() {
        viewModelScope.launch {
            val size = userPreferenceRepository.getString(KEY_CAPTURE_FONT_SIZE, "MEDIUM")
            _uiState.update { it.copy(captureFontSize = size) }
        }
    }

    /** 캘린더 권한 상태 새로고침 */
    fun refreshCalendarPermissionState() {
        viewModelScope.launch {
            val granted = calendarRepository.isCalendarPermissionGranted()
            if (!granted) {
                if (_uiState.value.isCalendarEnabled) {
                    setCalendarSettingsUseCase.setCalendarEnabled(false)
                }
                _uiState.update {
                    it.copy(
                        isCalendarPermissionGranted = false,
                        isCalendarEnabled = false,
                        availableCalendars = emptyList(),
                        selectedCalendarId = null
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isCalendarPermissionGranted = true) }
            loadAvailableCalendarsInternal()
        }
    }

    /** 권한 요청 결과 처리 */
    fun onCalendarPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (!granted) {
                setCalendarSettingsUseCase.setCalendarEnabled(false)
                _uiState.update {
                    it.copy(
                        isCalendarPermissionGranted = false,
                        isCalendarEnabled = false,
                        availableCalendars = emptyList(),
                        selectedCalendarId = null,
                        calendarAuthMessage = "캘린더 권한이 거부되었습니다."
                    )
                }
                return@launch
            }

            setCalendarSettingsUseCase.setCalendarEnabled(true)
            _uiState.update {
                it.copy(
                    isCalendarPermissionGranted = true,
                    isCalendarEnabled = true,
                    calendarAuthMessage = "캘린더 권한이 허용되었습니다."
                )
            }
            loadAvailableCalendarsInternal()
        }
    }

    /** 캘린더 목록 재조회 */
    fun reloadAvailableCalendars() {
        viewModelScope.launch {
            loadAvailableCalendarsInternal()
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
                    it.copy(calendarAuthMessage = mapCalendarErrorMessage(throwable))
                }
            }
        }
    }

    /** 캡처 글씨 크기 변경 */
    fun setCaptureFontSize(size: String) {
        viewModelScope.launch {
            userPreferenceRepository.setString(KEY_CAPTURE_FONT_SIZE, size)
            _uiState.update { it.copy(captureFontSize = size) }
        }
    }

    /** 분류 프리셋 변경 */
    fun setPreset(presetId: String) {
        viewModelScope.launch {
            setPresetUseCase(presetId)
            _uiState.update { it.copy(selectedPresetId = presetId) }
        }
    }

    /** 분류 커스텀 인스트럭션 변경 */
    fun setCustomInstruction(instruction: String) {
        _uiState.update { it.copy(customInstruction = instruction) }
    }

    /** 분류 커스텀 인스트럭션 저장 */
    fun saveCustomInstruction() {
        viewModelScope.launch {
            setCustomInstructionUseCase(_uiState.value.customInstruction)
        }
    }

    /** 로그아웃 */
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _uiState.update {
                    it.copy(user = null)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    /** 테마 변경 (LIGHT / DARK / SYSTEM) */
    fun setTheme(theme: ThemePreference) {
        viewModelScope.launch {
            userPreferenceRepository.setThemePreference(theme)
            _uiState.update { it.copy(themePreference = theme) }
        }
    }

    /** 캘린더 연동 토글 */
    fun toggleCalendar(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !calendarRepository.isCalendarPermissionGranted()) {
                _uiState.update {
                    it.copy(
                        isCalendarEnabled = false,
                        calendarAuthMessage = "캘린더 권한이 필요합니다."
                    )
                }
                return@launch
            }

            setCalendarSettingsUseCase.setCalendarEnabled(enabled)
            _uiState.update {
                it.copy(
                    isCalendarEnabled = enabled,
                    isCalendarPermissionGranted = calendarRepository.isCalendarPermissionGranted()
                )
            }

            if (enabled) {
                loadAvailableCalendarsInternal()
            }
        }
    }

    /** 일정 추가 모드 변경 */
    fun setCalendarMode(mode: String) {
        viewModelScope.launch {
            setCalendarSettingsUseCase.setCalendarMode(mode)
            _uiState.update { it.copy(calendarMode = mode) }
        }
    }

    /** 알림 설정 토글 */
    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch {
            setCalendarSettingsUseCase.setNotificationEnabled(enabled)
            _uiState.update { it.copy(isNotificationEnabled = enabled) }
        }
    }

    /** 에러 메시지 닫기 */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissCalendarAuthMessage() {
        _uiState.update { it.copy(calendarAuthMessage = null) }
    }

    fun showCalendarAuthMessage(message: String) {
        _uiState.update { it.copy(calendarAuthMessage = message) }
    }

    /** 디버그: 이미지 URI로 캡처 제출 */
    fun debugSubmitImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(debugSubmitting = true, debugResult = null) }
            try {
                val savedUri = imageRepository.saveImage(uri)
                val capture = submitCaptureUseCase(
                    text = "[디버그] 이미지 캡처 테스트",
                    imageUri = savedUri
                )
                _uiState.update {
                    it.copy(
                        debugSubmitting = false,
                        debugResult = "캡처 생성됨: ${capture.id.take(8)}..."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        debugSubmitting = false,
                        debugResult = "실패: ${e.message}"
                    )
                }
            }
        }
    }

    /** 디버그 결과 메시지 닫기 */
    fun dismissDebugResult() {
        _uiState.update { it.copy(debugResult = null) }
    }

    private suspend fun loadAvailableCalendarsInternal() {
        runCatching {
            val calendars = calendarRepository.getAvailableCalendars()
            val selected = normalizeSelectedCalendar(calendars)
            calendars to selected
        }.onSuccess { (calendars, selectedCalendarId) ->
            _uiState.update {
                it.copy(
                    availableCalendars = calendars,
                    selectedCalendarId = selectedCalendarId,
                    isCalendarPermissionGranted = true
                )
            }
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(
                    availableCalendars = emptyList(),
                    selectedCalendarId = null,
                    calendarAuthMessage = mapCalendarErrorMessage(throwable)
                )
            }
        }
    }

    private suspend fun normalizeSelectedCalendar(calendars: List<LocalCalendar>): Long? {
        if (calendars.isEmpty()) {
            return null
        }

        val selected = calendarRepository.getTargetCalendarId()
        if (selected != null && calendars.any { it.id == selected }) {
            return selected
        }

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
