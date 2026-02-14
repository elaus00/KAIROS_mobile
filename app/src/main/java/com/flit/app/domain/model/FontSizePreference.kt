package com.flit.app.domain.model

/**
 * 글씨 크기 설정
 * - captureFontSize / captureLineHeight: 캡처 입력 화면용
 * - bodyFontSize / bodyLineHeight: 상세 화면 본문용
 */
enum class FontSizePreference(
    val captureFontSize: Int,
    val captureLineHeight: Int,
    val bodyFontSize: Int,
    val bodyLineHeight: Int,
    val appFontScale: Float
) {
    SMALL(
        captureFontSize = 16,
        captureLineHeight = 28,
        bodyFontSize = 13,
        bodyLineHeight = 19,
        appFontScale = 13f / 15f
    ),
    MEDIUM(
        captureFontSize = 20,
        captureLineHeight = 34,
        bodyFontSize = 15,
        bodyLineHeight = 22,
        appFontScale = 1.0f
    ),
    LARGE(
        captureFontSize = 24,
        captureLineHeight = 40,
        bodyFontSize = 18,
        bodyLineHeight = 26,
        appFontScale = 18f / 15f
    );

    companion object {
        fun fromString(value: String): FontSizePreference {
            return entries.find { it.name == value.uppercase() } ?: MEDIUM
        }
    }
}
