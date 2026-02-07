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
     * Google Calendar 연동 설정
     */
    suspend fun setCalendarEnabled(enabled: Boolean) {
        userPreferenceRepository.setString("calendar_enabled", enabled.toString())
    }

    /**
     * 일정 추가 모드 설정 (auto / suggest)
     */
    suspend fun setCalendarMode(mode: String) {
        userPreferenceRepository.setString("calendar_mode", mode)
    }

    /**
     * 알림 설정
     */
    suspend fun setNotificationEnabled(enabled: Boolean) {
        userPreferenceRepository.setString("notification_enabled", enabled.toString())
    }
}
