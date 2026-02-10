package com.example.kairos_mobile.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.CalendarApiException
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
        loadAccountInfo()
        loadPresets()
        loadCaptureFontSize()
    }

    /**
     * 설정 값 로드
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferenceRepository.getThemePreference().collect { theme ->
                _uiState.update { it.copy(themePreference = theme) }
            }
        }
    }

    /**
     * 캘린더 설정 로드
     */
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

    /**
     * 계정 정보 로드
     */
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

    /**
     * 분류 프리셋 로드
     */
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

    /**
     * 캡처 글씨 크기 로드
     */
    private fun loadCaptureFontSize() {
        viewModelScope.launch {
            val size = userPreferenceRepository.getString(KEY_CAPTURE_FONT_SIZE, "MEDIUM")
            _uiState.update { it.copy(captureFontSize = size) }
        }
    }

    /**
     * 캡처 글씨 크기 변경
     */
    fun setCaptureFontSize(size: String) {
        viewModelScope.launch {
            userPreferenceRepository.setString(KEY_CAPTURE_FONT_SIZE, size)
            _uiState.update { it.copy(captureFontSize = size) }
        }
    }

    /**
     * 분류 프리셋 변경
     */
    fun setPreset(presetId: String) {
        viewModelScope.launch {
            setPresetUseCase(presetId)
            _uiState.update { it.copy(selectedPresetId = presetId) }
        }
    }

    /**
     * 분류 커스텀 인스트럭션 변경
     */
    fun setCustomInstruction(instruction: String) {
        _uiState.update { it.copy(customInstruction = instruction) }
    }

    /**
     * 분류 커스텀 인스트럭션 저장
     */
    fun saveCustomInstruction() {
        viewModelScope.launch {
            setCustomInstructionUseCase(_uiState.value.customInstruction)
        }
    }

    /**
     * 로그아웃
     */
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

    /**
     * 테마 변경 (LIGHT / DARK / SYSTEM)
     */
    fun setTheme(theme: ThemePreference) {
        viewModelScope.launch {
            userPreferenceRepository.setThemePreference(theme)
            _uiState.update { it.copy(themePreference = theme) }
        }
    }

    /**
     * Google Calendar 연동 토글
     */
    fun toggleCalendar(enabled: Boolean) {
        viewModelScope.launch {
            setCalendarSettingsUseCase.setCalendarEnabled(enabled)
            _uiState.update { it.copy(isCalendarEnabled = enabled) }
        }
    }

    /**
     * 일정 추가 모드 변경
     */
    fun setCalendarMode(mode: String) {
        viewModelScope.launch {
            setCalendarSettingsUseCase.setCalendarMode(mode)
            _uiState.update { it.copy(calendarMode = mode) }
        }
    }

    /**
     * 알림 설정 토글
     */
    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch {
            setCalendarSettingsUseCase.setNotificationEnabled(enabled)
            _uiState.update { it.copy(isNotificationEnabled = enabled) }
        }
    }

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissCalendarAuthMessage() {
        _uiState.update { it.copy(calendarAuthMessage = null) }
    }

    fun showCalendarAuthMessage(message: String) {
        _uiState.update { it.copy(calendarAuthMessage = message) }
    }

    fun exchangeCalendarCode(code: String, redirectUri: String) {
        if (code.isBlank() || redirectUri.isBlank()) {
            _uiState.update { it.copy(calendarAuthMessage = "code/redirect_uri를 입력해주세요.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(calendarAuthLoading = true, calendarAuthMessage = null) }
            runCatching {
                calendarRepository.exchangeCalendarToken(code.trim(), redirectUri.trim())
            }.onSuccess { connected ->
                if (connected) {
                    setCalendarSettingsUseCase.setCalendarEnabled(true)
                }
                _uiState.update {
                    it.copy(
                        calendarAuthLoading = false,
                        isCalendarEnabled = connected || it.isCalendarEnabled,
                        calendarAuthMessage = if (connected) "Google Calendar 연결 성공" else "연결 실패"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        calendarAuthLoading = false,
                        calendarAuthMessage = mapCalendarErrorMessage(throwable)
                    )
                }
            }
        }
    }

    fun saveCalendarToken(accessToken: String, refreshToken: String?, expiresIn: String?) {
        if (accessToken.isBlank()) {
            _uiState.update { it.copy(calendarAuthMessage = "access_token을 입력해주세요.") }
            return
        }
        // expires_in(초)를 expires_at(ISO 8601)로 변환
        val expiresAt = expiresIn?.trim()?.takeIf { it.isNotEmpty() }?.toLongOrNull()?.let {
            java.time.Instant.now().plusSeconds(it).toString()
        }
        viewModelScope.launch {
            _uiState.update { it.copy(calendarAuthLoading = true, calendarAuthMessage = null) }
            runCatching {
                calendarRepository.saveCalendarToken(
                    accessToken = accessToken.trim(),
                    refreshToken = refreshToken?.trim()?.ifEmpty { null },
                    expiresAt = expiresAt
                )
            }.onSuccess { connected ->
                if (connected) {
                    setCalendarSettingsUseCase.setCalendarEnabled(true)
                }
                _uiState.update {
                    it.copy(
                        calendarAuthLoading = false,
                        isCalendarEnabled = connected || it.isCalendarEnabled,
                        calendarAuthMessage = if (connected) "토큰 저장 성공" else "토큰 저장 실패"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        calendarAuthLoading = false,
                        calendarAuthMessage = mapCalendarErrorMessage(throwable)
                    )
                }
            }
        }
    }

    fun fetchCalendarEventsPreview() {
        viewModelScope.launch {
            _uiState.update { it.copy(calendarAuthLoading = true, calendarAuthMessage = null) }
            val today = java.time.LocalDate.now()
            runCatching {
                calendarRepository.getCalendarEvents(
                    startDate = today.minusDays(7),
                    endDate = today.plusDays(30)
                )
            }.onSuccess { events ->
                _uiState.update {
                    it.copy(
                        calendarAuthLoading = false,
                        calendarAuthMessage = "캘린더 이벤트 ${events.size}건 조회됨"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        calendarAuthLoading = false,
                        calendarAuthMessage = mapCalendarErrorMessage(throwable)
                    )
                }
            }
        }
    }

    /**
     * 디버그: 이미지 URI로 캡처 제출
     */
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

    /**
     * 디버그 결과 메시지 닫기
     */
    fun dismissDebugResult() {
        _uiState.update { it.copy(debugResult = null) }
    }

    private fun mapCalendarErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is CalendarApiException.GoogleAuthRequired -> "Google 계정 연결이 필요합니다."
            is CalendarApiException.GoogleTokenExpired -> "Google 토큰이 만료되었습니다. 다시 연결해주세요."
            is CalendarApiException.GoogleApiError -> "Google Calendar API 오류가 발생했습니다."
            is CalendarApiException.Unknown -> throwable.message ?: "캘린더 요청 실패"
            else -> throwable.message ?: "요청 실패"
        }
    }
}
