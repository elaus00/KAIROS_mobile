package com.example.kairos_mobile.domain.usecase

import com.example.kairos_mobile.domain.model.Notification
import com.example.kairos_mobile.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 추가 Use Case
 *
 * 비즈니스 로직:
 * - 새 알림 생성 및 저장
 * - 입력 검증
 */
@Singleton
class AddNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * 알림 추가
     *
     * @param notification 추가할 알림
     */
    suspend operator fun invoke(notification: Notification) {
        if (notification.title.isBlank()) {
            throw IllegalArgumentException("Notification title cannot be empty")
        }

        repository.addNotification(notification)
    }
}
