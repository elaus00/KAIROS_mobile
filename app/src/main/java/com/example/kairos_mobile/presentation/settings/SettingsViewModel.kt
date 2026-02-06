package com.example.kairos_mobile.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.usecase.settings.GetThemePreferenceUseCase
import com.example.kairos_mobile.domain.usecase.settings.SetThemePreferenceUseCase
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
    private val getThemePreferenceUseCase: GetThemePreferenceUseCase,
    private val setThemePreferenceUseCase: SetThemePreferenceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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
    }

    /**
     * 테마 변경 (LIGHT / DARK / SYSTEM)
     */
    fun setTheme(theme: ThemePreference) {
        viewModelScope.launch {
            setThemePreferenceUseCase(theme)
            _uiState.update { it.copy(themePreference = theme) }
        }
    }

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
