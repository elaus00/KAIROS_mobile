package com.example.kairos_mobile.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.repository.ImageRepository
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import com.example.kairos_mobile.domain.usecase.capture.SubmitCaptureUseCase
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
 * 다크모드 3옵션 설정만 관리
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val getCalendarSettingsUseCase: GetCalendarSettingsUseCase,
    private val setCalendarSettingsUseCase: SetCalendarSettingsUseCase,
    private val submitCaptureUseCase: SubmitCaptureUseCase,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
        loadCalendarSettings()
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
}
