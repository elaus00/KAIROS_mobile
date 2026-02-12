package com.flit.app.presentation.capture

/**
 * 캡처 화면 UI 상태
 */
data class CaptureUiState(
    /** 입력 텍스트 */
    val inputText: String = "",
    /** 최대 글자 수 */
    val maxCharacterCount: Int = 5000,
    /** 미확인 AI 분류 수 (벨 뱃지) */
    val unconfirmedCount: Int = 0,
    /** 제출 중 여부 */
    val isSubmitting: Boolean = false,
    /** AI Status Sheet 표시 여부 */
    val showStatusSheet: Boolean = false,
    /** 첨부 이미지 URI */
    val imageUri: String? = null,
    /** 에러 메시지 */
    val errorMessage: String? = null,
    /** 캡처 입력 글씨 크기 (sp) */
    val fontSize: Int = 20,
    /** 캡처 입력 줄 높이 (sp) */
    val lineHeight: Int = 34
)

/**
 * 캡처 화면 이벤트
 */
sealed class CaptureEvent {
    /** 캡처 제출 성공 */
    data object SubmitSuccess : CaptureEvent()
}
