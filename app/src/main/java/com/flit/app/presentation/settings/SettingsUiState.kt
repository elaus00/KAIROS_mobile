package com.flit.app.presentation.settings

import com.flit.app.domain.model.FontSizePreference
import com.flit.app.domain.model.SubscriptionFeatures
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.domain.model.ThemePreference
import com.flit.app.domain.model.User

/**
 * Settings 화면 UI 상태
 */
data class SettingsUiState(
    // 테마 설정 (LIGHT / DARK / SYSTEM)
    val themePreference: ThemePreference = ThemePreference.SYSTEM,

    // 캘린더 연동 여부
    val isCalendarEnabled: Boolean = false,
    val isCalendarPermissionGranted: Boolean = false,

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

    // 캡처 글씨 크기 (SMALL / MEDIUM / LARGE)
    val captureFontSize: String = FontSizePreference.MEDIUM.name,

    // 노트 보기 유형 (LIST / GRID / COMPACT)
    val noteViewType: String = "LIST"
)
