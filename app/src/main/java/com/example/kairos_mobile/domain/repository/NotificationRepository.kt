package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.Notification
import kotlinx.coroutines.flow.Flow

/**
 * 알림 Repository 인터페이스
 */
interface NotificationRepository {

    /**
     * 모든 알림 조회 (최신순)
     */
    fun getNotifications(): Flow<List<Notification>>

    /**
     * 읽지 않은 알림만 조회
     */
    fun getUnreadNotifications(): Flow<List<Notification>>

    /**
     * 특정 알림 조회
     */
    suspend fun getNotificationById(id: String): Notification?

    /**
     * 알림 추가
     */
    suspend fun addNotification(notification: Notification)

    /**
     * 여러 알림 추가
     */
    suspend fun addNotifications(notifications: List<Notification>)

    /**
     * 알림을 읽음 처리
     */
    suspend fun markAsRead(notificationId: String)

    /**
     * 모든 알림 읽음 처리
     */
    suspend fun markAllAsRead()

    /**
     * 알림 삭제
     */
    suspend fun deleteNotification(notificationId: String)

    /**
     * 읽은 알림 모두 삭제
     */
    suspend fun deleteReadNotifications()

    /**
     * 읽지 않은 알림 개수 조회
     */
    fun getUnreadCount(): Flow<Int>
}
