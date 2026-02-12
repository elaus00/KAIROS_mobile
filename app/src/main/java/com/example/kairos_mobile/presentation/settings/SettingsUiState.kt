package com.example.kairos_mobile.presentation.settings

import com.example.kairos_mobile.domain.model.ClassificationPreset
import com.example.kairos_mobile.domain.model.LocalCalendar
import com.example.kairos_mobile.domain.model.SubscriptionFeatures
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.model.ThemePreference
import com.example.kairos_mobile.domain.model.User

/**
 * Settings 화면 UI 상태
 */
data class SettingsUiState(
    // 테마 설정 (LIGHT / DARK / SYSTEM)
    val themePreference: ThemePreference = ThemePreference.DARK,

    // 캘린더 연동 여부
    val isCalendarEnabled: Boolean = false,
    val isCalendarPermissionGranted: Boolean = false,
    val availableCalendars: List<LocalCalendar> = emptyList(),
    val selectedCalendarId: Long? = null,

    // 일정 추가 모드 (auto / suggest)
    val calendarMode: String = "suggest",

    // 알림 설정 여부
    val isNotificationEnabled: Boolean = true,

    // 에러 메시지
    val errorMessage: String? = null,

    // 디버그: 이미지 캡처 제출 상태
    val debugSubmitting: Boolean = false,
    val debugResult: String? = null,

    val calendarAuthMessage: String? = null,

    // 계정 정보
    val user: User? = null,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val features: SubscriptionFeatures = SubscriptionFeatures(),

    // AI 분류 설정
    val presets: List<ClassificationPreset> = emptyList(),
    val selectedPresetId: String = "default",
    val customInstruction: String = "",

    // 캡처 글씨 크기 (SMALL / MEDIUM / LARGE)
    val captureFontSize: String = "MEDIUM"
)
