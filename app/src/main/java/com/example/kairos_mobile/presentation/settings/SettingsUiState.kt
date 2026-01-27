package com.example.kairos_mobile.presentation.settings

import com.example.kairos_mobile.domain.model.ThemePreference

/**
 * Settings 화면 UI 상태
 * Phase 3: 외부 서비스 연동 및 AI 기능 설정
 */
data class SettingsUiState(
    // ========== M11: Google Calendar 연동 상태 ==========
    val googleCalendarConnected: Boolean = false,
    val googleLastSyncTime: Long? = null,
    val googleSyncedCount: Int = 0,
    val isGoogleLoading: Boolean = false,

    // ========== M12: Todoist 연동 상태 ==========
    val todoistConnected: Boolean = false,
    val todoistLastSyncTime: Long? = null,
    val todoistSyncedCount: Int = 0,
    val isTodoistLoading: Boolean = false,

    // ========== AI 기능 설정 ==========
    val autoSummarizeEnabled: Boolean = true,
    val smartTagsEnabled: Boolean = true,

    // ========== 테마 설정 ==========
    val themePreference: ThemePreference = ThemePreference.DARK,

    // ========== UI 이벤트 ==========
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // OAuth URL (Chrome Custom Tab에서 열 URL)
    val pendingOAuthUrl: String? = null
)

/**
 * Settings 화면 이벤트
 */
sealed class SettingsEvent {
    /**
     * OAuth 플로우 시작 (Chrome Custom Tab 열기)
     */
    data class OpenOAuthUrl(val url: String) : SettingsEvent()

    /**
     * 에러 메시지 표시
     */
    data class ShowError(val message: String) : SettingsEvent()

    /**
     * 성공 메시지 표시
     */
    data class ShowSuccess(val message: String) : SettingsEvent()
}
