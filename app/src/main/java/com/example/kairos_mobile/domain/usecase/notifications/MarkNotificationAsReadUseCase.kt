package com.example.kairos_mobile.domain.usecase.notifications

import com.example.kairos_mobile.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 읽음 처리 Use Case
 *
 * 비즈니스 로직:
 * - 특정 알림을 읽음 처리
 * - 모든 알림을 읽음 처리
 */
@Singleton
class MarkNotificationAsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * 특정 알림 읽음 처리
     *
     * @param notificationId 알림 ID
     */
    suspend operator fun invoke(notificationId: String) {
        if (notificationId.isBlank()) {
            throw IllegalArgumentException("Notification ID cannot be empty")
        }

        repository.markAsRead(notificationId)
    }

    /**
     * 모든 알림 읽음 처리
     */
    suspend fun markAllAsRead() {
        repository.markAllAsRead()
    }
}
