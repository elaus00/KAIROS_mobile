package com.example.kairos_mobile.presentation.home

import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.model.InsightType
import com.example.kairos_mobile.domain.model.Schedule

/**
 * Home 화면 UI 상태 (PRD v4.0)
 */
data class HomeUiState(
    // 캡처 입력
    val inputText: String = "",
    val isInputFocused: Boolean = false,
    val characterCount: Int = 0,
    val maxCharacterCount: Int = 500,

    // AI 분류
    val suggestedType: InsightType? = null,
    val isClassifying: Boolean = false,
    val classificationConfidence: Float = 0f,

    // 최근 캡처 그리드
    val recentCaptures: List<Insight> = emptyList(),
    val isLoadingCaptures: Boolean = false,

    // AI 추천 일정
    val nextSchedule: Schedule? = null,
    val todayScheduleCount: Int = 0,

    // 처리 상태
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Home 화면 이벤트
 */
sealed class HomeEvent {
    // 입력 관련
    data class UpdateInput(val text: String) : HomeEvent()
    data class SetInputFocused(val focused: Boolean) : HomeEvent()
    data object ClearInput : HomeEvent()

    // 제출 관련
    data object Submit : HomeEvent()
    data class SubmitWithType(val type: InsightType) : HomeEvent()

    // 캡처 관련
    data object OpenCamera : HomeEvent()
    data object OpenVoiceInput : HomeEvent()

    // 네비게이션
    data class NavigateToCapture(val captureId: String) : HomeEvent()
    data object NavigateToCalendar : HomeEvent()

    // 오류 처리
    data object DismissError : HomeEvent()
    data object ClearSubmitSuccess : HomeEvent()
}
