package com.example.kairos_mobile.presentation.settings

import com.example.kairos_mobile.domain.model.ThemePreference

/**
 * Settings 화면 UI 상태
 * 다크모드 3옵션만 관리
 */
data class SettingsUiState(
    // 테마 설정 (LIGHT / DARK / SYSTEM)
    val themePreference: ThemePreference = ThemePreference.DARK,

    // Google Calendar 연동 여부
    val isCalendarEnabled: Boolean = false,

    // 일정 추가 모드 (auto / suggest)
    val calendarMode: String = "suggest",

    // 알림 설정 여부
    val isNotificationEnabled: Boolean = true,

    // 에러 메시지
    val errorMessage: String? = null,

    // 디버그: 이미지 캡처 제출 상태
    val debugSubmitting: Boolean = false,
    val debugResult: String? = null,

    // 캘린더 연동 요청 상태
    val calendarAuthLoading: Boolean = false,
    val calendarAuthMessage: String? = null
)
