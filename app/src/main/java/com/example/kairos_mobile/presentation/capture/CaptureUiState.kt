package com.example.kairos_mobile.presentation.capture

/**
 * 캡처 화면 UI 상태
 */
data class CaptureUiState(
    /** 입력 텍스트 */
    val inputText: String = "",
    /** 현재 글자 수 */
    val characterCount: Int = 0,
    /** 최대 글자 수 */
    val maxCharacterCount: Int = 5000,
    /** 미확인 AI 분류 수 (벨 뱃지) */
    val unconfirmedCount: Int = 0,
    /** 제출 중 여부 */
    val isSubmitting: Boolean = false,
    /** AI Status Sheet 표시 여부 */
    val showStatusSheet: Boolean = false,
    /** 에러 메시지 */
    val errorMessage: String? = null
)

/**
 * 캡처 화면 이벤트
 */
sealed class CaptureEvent {
    /** 캡처 제출 성공 */
    data object SubmitSuccess : CaptureEvent()
}
