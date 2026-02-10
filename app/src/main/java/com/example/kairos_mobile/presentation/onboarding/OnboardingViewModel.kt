package com.example.kairos_mobile.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import com.example.kairos_mobile.domain.usecase.capture.SubmitCaptureUseCase
import com.example.kairos_mobile.domain.usecase.settings.CalendarSettingsKeys
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
 * 온보딩 UI 상태
 */
data class OnboardingUiState(
    val inputText: String = "",
    val isSubmitting: Boolean = false,
    /** Google Calendar 연결 상태 (Mock) */
    val isGoogleConnected: Boolean = false,
    /** Google Calendar 연결 실패 에러 메시지 */
    val googleConnectionError: String? = null
)

/**
 * 온보딩 이벤트
 */
sealed class OnboardingEvent {
    /** 온보딩 완료 → 홈으로 이동 */
    data object NavigateToHome : OnboardingEvent()
}

/**
 * 온보딩 ViewModel
 * 3화면 온보딩 흐름 관리 + 첫 캡처 제출 + 완료 상태 저장
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val submitCaptureUseCase: SubmitCaptureUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()

    init {
        loadGoogleConnectionState()
    }

    private fun loadGoogleConnectionState() {
        viewModelScope.launch {
            val connected = userPreferenceRepository.getString(
                CalendarSettingsKeys.KEY_CALENDAR_ENABLED,
                "false"
            ) == "true"
            _uiState.update { it.copy(isGoogleConnected = connected) }
        }
    }

    /**
     * 입력 텍스트 업데이트 (3번째 화면 입력창)
     */
    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    /**
     * 온보딩 완료 처리
     * 입력된 텍스트가 있으면 캡처 저장 후 완료, 없으면 바로 완료
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val text = _uiState.value.inputText
            if (text.isNotBlank()) {
                try {
                    submitCaptureUseCase(text)
                } catch (_: Exception) {
                    // 첫 캡처 실패해도 온보딩은 완료 처리
                }
            }
            userPreferenceRepository.setOnboardingCompleted()
            _events.emit(OnboardingEvent.NavigateToHome)
            _uiState.update { it.copy(isSubmitting = false) }
        }
    }

    /**
     * 스킵 (텍스트 입력 없이 완료)
     */
    fun skip() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            userPreferenceRepository.setOnboardingCompleted()
            _events.emit(OnboardingEvent.NavigateToHome)
            _uiState.update { it.copy(isSubmitting = false) }
        }
    }

    /**
     * Google Calendar 연결 (Mock — 즉시 연결됨 상태로 전환)
     */
    fun connectGoogle() {
        viewModelScope.launch {
            try {
                // 에러 상태 초기화
                _uiState.update { it.copy(googleConnectionError = null) }

                userPreferenceRepository.setString(CalendarSettingsKeys.KEY_CALENDAR_ENABLED, "true")
                _uiState.update { it.copy(isGoogleConnected = true) }
            } catch (e: Exception) {
                // 실패 시 에러 메시지 설정 (현재는 발생하지 않음)
                _uiState.update { it.copy(googleConnectionError = "연결에 실패했습니다. 다시 시도해주세요.") }
            }
        }
    }
}
