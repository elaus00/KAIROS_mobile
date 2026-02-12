package com.example.kairos_mobile.domain.usecase.settings

import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캘린더 설정 변경 UseCase
 */
@Singleton
class SetCalendarSettingsUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) {
    /**
     * 기기 캘린더 연동 설정
     */
    suspend fun setCalendarEnabled(enabled: Boolean) {
        userPreferenceRepository.setString(CalendarSettingsKeys.KEY_CALENDAR_ENABLED, enabled.toString())
    }

    /**
     * 일정 추가 모드 설정 (auto / suggest)
     */
    suspend fun setCalendarMode(mode: String) {
        val normalizedMode = when (mode.lowercase()) {
            CalendarSettingsKeys.MODE_AUTO -> CalendarSettingsKeys.MODE_AUTO
            else -> CalendarSettingsKeys.MODE_SUGGEST
        }
        userPreferenceRepository.setString(CalendarSettingsKeys.KEY_CALENDAR_MODE, normalizedMode)
    }

    /**
     * 알림 설정
     */
    suspend fun setNotificationEnabled(enabled: Boolean) {
        userPreferenceRepository.setString(CalendarSettingsKeys.KEY_NOTIFICATION_ENABLED, enabled.toString())
    }
}
