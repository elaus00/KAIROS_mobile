package com.example.kairos_mobile.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.repository.CalendarRepository
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
    /** 캘린더 권한 허용 상태 */
    val isCalendarPermissionGranted: Boolean = false,
    /** 캘린더 연동 에러 메시지 */
    val calendarConnectionError: String? = null
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
    private val submitCaptureUseCase: SubmitCaptureUseCase,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()

    init {
        loadCalendarPermissionState()
    }

    private fun loadCalendarPermissionState() {
        viewModelScope.launch {
            val granted = calendarRepository.isCalendarPermissionGranted()
            _uiState.update { it.copy(isCalendarPermissionGranted = granted) }
            if (!granted) {
                userPreferenceRepository.setString(CalendarSettingsKeys.KEY_CALENDAR_ENABLED, "false")
            }
        }
    }

    /** 입력 텍스트 업데이트 (3번째 화면 입력창) */
    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    /**
     * 권한 요청 결과 처리
     * 허용 시 캘린더 연동 활성화 + 기본 캘린더 선택
     */
    fun onCalendarPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (!granted) {
                userPreferenceRepository.setString(CalendarSettingsKeys.KEY_CALENDAR_ENABLED, "false")
                _uiState.update {
                    it.copy(
                        isCalendarPermissionGranted = false,
                        calendarConnectionError = "권한이 없어도 계속 사용할 수 있습니다. 나중에 설정에서 켜세요."
                    )
                }
                return@launch
            }

            runCatching {
                val calendars = calendarRepository.getAvailableCalendars()
                val target = calendars.firstOrNull { it.isPrimary } ?: calendars.firstOrNull()
                target?.let { calendarRepository.setTargetCalendarId(it.id) }
                userPreferenceRepository.setString(CalendarSettingsKeys.KEY_CALENDAR_ENABLED, "true")
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isCalendarPermissionGranted = true,
                        calendarConnectionError = null
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isCalendarPermissionGranted = false,
                        calendarConnectionError = "캘린더 연동에 실패했습니다. 설정에서 다시 시도해주세요."
                    )
                }
            }
        }
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

    /** 스킵 (텍스트 입력 없이 완료) */
    fun skip() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            userPreferenceRepository.setOnboardingCompleted()
            _events.emit(OnboardingEvent.NavigateToHome)
            _uiState.update { it.copy(isSubmitting = false) }
        }
    }
}
