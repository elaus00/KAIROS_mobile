package com.example.kairos_mobile.domain.usecase

import com.example.kairos_mobile.domain.model.Notification
import com.example.kairos_mobile.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 조회 Use Case
 *
 * 비즈니스 로직:
 * - 모든 알림 또는 읽지 않은 알림 조회
 * - 읽지 않은 알림 개수 조회
 */
@Singleton
class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * 모든 알림 조회 (최신순)
     */
    operator fun invoke(): Flow<List<Notification>> {
        return repository.getNotifications()
    }

    /**
     * 읽지 않은 알림만 조회
     */
    fun getUnreadNotifications(): Flow<List<Notification>> {
        return repository.getUnreadNotifications()
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    fun getUnreadCount(): Flow<Int> {
        return repository.getUnreadCount()
    }
}
