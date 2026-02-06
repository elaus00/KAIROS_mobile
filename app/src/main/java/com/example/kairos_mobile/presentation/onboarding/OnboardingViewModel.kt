package com.example.kairos_mobile.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import com.example.kairos_mobile.domain.usecase.capture.SubmitCaptureUseCase
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
    val isSubmitting: Boolean = false
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
            val text = _uiState.value.inputText
            if (text.isNotBlank()) {
                _uiState.update { it.copy(isSubmitting = true) }
                try {
                    submitCaptureUseCase(text)
                } catch (_: Exception) {
                    // 첫 캡처 실패해도 온보딩은 완료 처리
                }
            }
            userPreferenceRepository.setOnboardingCompleted()
            _events.emit(OnboardingEvent.NavigateToHome)
        }
    }

    /**
     * 스킵 (텍스트 입력 없이 완료)
     */
    fun skip() {
        viewModelScope.launch {
            userPreferenceRepository.setOnboardingCompleted()
            _events.emit(OnboardingEvent.NavigateToHome)
        }
    }
}
