package com.example.kairos_mobile.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.repository.PreferencesRepository
import com.example.kairos_mobile.domain.usecase.settings.GetThemePreferenceUseCase
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
    private val preferencesRepository: PreferencesRepository
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
            preferencesRepository.getAutoSummarizeEnabled().collect { enabled ->
                _uiState.update { it.copy(autoSummarizeEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            preferencesRepository.getSmartTagsEnabled().collect { enabled ->
                _uiState.update { it.copy(smartTagsEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            getThemePreferenceUseCase().collect { theme ->
                _uiState.update { it.copy(themePreference = theme) }
            }
        }
    }

    // ========== AI 기능 설정 ==========

    /**
     * 자동 요약 활성화 토글
     */
    fun toggleAutoSummarize(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoSummarizeEnabled(enabled)
            _uiState.update { it.copy(autoSummarizeEnabled = enabled) }
        }
    }

    /**
     * 스마트 태그 제안 활성화 토글
     */
    fun toggleSmartTags(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSmartTagsEnabled(enabled)
            _uiState.update { it.copy(smartTagsEnabled = enabled) }
        }
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
