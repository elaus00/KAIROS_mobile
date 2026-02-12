package com.flit.app.domain.usecase.settings

/**
 * Preference 키 상수 정의
 * 여러 곳에서 사용되는 키 문자열을 중앙 집중 관리
 */
object PreferenceKeys {
    /** 캡처 화면 글씨 크기 설정 키 */
    const val KEY_CAPTURE_FONT_SIZE = "capture_font_size"

    /** AI 분류 프리셋 ID 설정 키 */
    const val KEY_CLASSIFICATION_PRESET_ID = "classification_preset_id"

    /** AI 분류 커스텀 인스트럭션 설정 키 */
    const val KEY_CLASSIFICATION_CUSTOM_INSTRUCTION = "classification_custom_instruction"
}
