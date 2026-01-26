package com.example.kairos_mobile.domain.model

/**
 * 테마 설정
 *
 * 사용자가 선택한 앱 테마
 */
enum class ThemePreference {
    /**
     * 다크 모드 (기본값)
     */
    DARK,

    /**
     * 라이트 모드
     */
    LIGHT;

    /**
     * 사용자 친화적인 이름 반환
     */
    fun getDisplayName(): String {
        return when (this) {
            DARK -> "다크 모드"
            LIGHT -> "라이트 모드"
        }
    }
}
