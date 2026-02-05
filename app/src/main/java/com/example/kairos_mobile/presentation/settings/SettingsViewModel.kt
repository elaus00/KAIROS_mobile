package com.example.kairos_mobile.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.usecase.settings.GetShowOverlayOnLaunchUseCase
import com.example.kairos_mobile.domain.usecase.settings.GetThemePreferenceUseCase
import com.example.kairos_mobile.domain.usecase.settings.SetShowOverlayOnLaunchUseCase
import com.example.kairos_mobile.domain.usecase.settings.SetThemePreferenceUseCase
import com.example.kairos_mobile.domain.model.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Settings 화면 ViewModel
 * AI 기능 및 테마 설정 관리
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getThemePreferenceUseCase: GetThemePreferenceUseCase,
    private val setThemePreferenceUseCase: SetThemePreferenceUseCase,
    private val getShowOverlayOnLaunchUseCase: GetShowOverlayOnLaunchUseCase,
    private val setShowOverlayOnLaunchUseCase: SetShowOverlayOnLaunchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        loadPreferences()
    }

    /**
     * 설정 값 로드
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            getThemePreferenceUseCase().collect { theme ->
                _uiState.update { it.copy(themePreference = theme) }
            }
        }
        viewModelScope.launch {
            getShowOverlayOnLaunchUseCase().collect { enabled ->
                _uiState.update { it.copy(showOverlayOnLaunch = enabled) }
            }
        }
    }

    // ========== AI 기능 설정 ==========

    /**
     * 자동 분류 활성화 토글
     */
    fun toggleAutoClassify(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(autoClassifyEnabled = enabled) }
        }
    }

    /**
     * 스마트 배치 활성화 토글
     */
    fun toggleSmartSchedule(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(smartScheduleEnabled = enabled) }
        }
    }

    // ========== 화면 설정 ==========

    /**
     * 노트 보기 방식 변경
     */
    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    /**
     * 앱 시작 시 빠른 메모 표시 토글
     */
    fun toggleShowOverlayOnLaunch(enabled: Boolean) {
        viewModelScope.launch {
            setShowOverlayOnLaunchUseCase(enabled)
            _uiState.update { it.copy(showOverlayOnLaunch = enabled) }
        }
    }

    // ========== 연동 설정 ==========

    /**
     * Google Calendar 연동 토글
     */
    fun toggleGoogleCalendar() {
        _uiState.update { it.copy(googleCalendarConnected = !it.googleCalendarConnected) }
    }

    /**
     * Obsidian 연동 토글
     */
    fun toggleObsidian() {
        _uiState.update { it.copy(obsidianConnected = !it.obsidianConnected) }
    }

    // ========== 테마 설정 ==========

    /**
     * 테마 변경
     */
    fun setTheme(theme: ThemePreference) {
        viewModelScope.launch {
            setThemePreferenceUseCase(theme)
            _uiState.update { it.copy(themePreference = theme) }
        }
    }

    /**
     * 다크 모드 토글
     */
    fun toggleDarkMode(enabled: Boolean) {
        val theme = if (enabled) ThemePreference.DARK else ThemePreference.LIGHT
        setTheme(theme)
    }

    // ========== UI 이벤트 처리 ==========

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 성공 메시지 닫기
     */
    fun onSuccessDismissed() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
