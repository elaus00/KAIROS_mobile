package com.example.kairos_mobile.domain.usecase.settings

import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캘린더 설정 조회 UseCase
 */
@Singleton
class GetCalendarSettingsUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) {
    /**
     * Google Calendar 연동 여부
     */
    suspend fun isCalendarEnabled(): Boolean {
        return userPreferenceRepository.getString(CalendarSettingsKeys.KEY_CALENDAR_ENABLED, "false") == "true"
    }

    /**
     * 일정 추가 모드 (auto / suggest)
     */
    suspend fun getCalendarMode(): String {
        return when (
            userPreferenceRepository.getString(
                CalendarSettingsKeys.KEY_CALENDAR_MODE,
                CalendarSettingsKeys.MODE_SUGGEST
            ).lowercase()
        ) {
            CalendarSettingsKeys.MODE_AUTO -> CalendarSettingsKeys.MODE_AUTO
            else -> CalendarSettingsKeys.MODE_SUGGEST
        }
    }

    /**
     * 알림 설정 여부
     */
    suspend fun isNotificationEnabled(): Boolean {
        return userPreferenceRepository.getString(CalendarSettingsKeys.KEY_NOTIFICATION_ENABLED, "true") == "true"
    }
}
