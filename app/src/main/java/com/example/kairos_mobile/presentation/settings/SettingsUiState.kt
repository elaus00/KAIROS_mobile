package com.example.kairos_mobile.presentation.settings

import com.example.kairos_mobile.domain.model.ThemePreference

/**
 * 노트 보기 방식
 */
enum class ViewMode {
    DETAIL,  // 자세히
    GRID,    // 그리드
    LIST     // 리스트
}

/**
 * Settings 화면 UI 상태
 * AI 기능 및 테마 설정
 */
data class SettingsUiState(
    // ========== AI 기능 설정 ==========
    val autoClassifyEnabled: Boolean = true,
    val smartScheduleEnabled: Boolean = true,

    // ========== 테마 설정 ==========
    val themePreference: ThemePreference = ThemePreference.DARK,

    // ========== 화면 설정 ==========
    val viewMode: ViewMode = ViewMode.LIST,
    val showOverlayOnLaunch: Boolean = true,

    // ========== 연동 설정 ==========
    val googleCalendarConnected: Boolean = true,
    val obsidianConnected: Boolean = false,

    // ========== UI 이벤트 ==========
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * Settings 화면 이벤트
 */
sealed class SettingsEvent {
    /**
     * 에러 메시지 표시
     */
    data class ShowError(val message: String) : SettingsEvent()

    /**
     * 성공 메시지 표시
     */
    data class ShowSuccess(val message: String) : SettingsEvent()
}
