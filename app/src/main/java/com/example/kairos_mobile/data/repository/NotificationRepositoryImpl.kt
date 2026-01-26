package com.example.kairos_mobile.data.repository

import android.util.Log
import com.example.kairos_mobile.data.local.database.dao.NotificationDao
import com.example.kairos_mobile.data.local.database.entities.NotificationEntity
import com.example.kairos_mobile.domain.model.Notification
import com.example.kairos_mobile.domain.model.NotificationType
import com.example.kairos_mobile.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 Repository 구현체
 */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : NotificationRepository {

    companion object {
        private const val TAG = "NotificationRepository"
        private const val MAX_NOTIFICATION_AGE_DAYS = 30
    }

    /**
     * 모든 알림 조회 (최신순)
     */
    override fun getNotifications(): Flow<List<Notification>> {
        return dao.getAllNotifications()
            .map { entities -> entities.map { entityToDomain(it) } }
    }

    /**
     * 읽지 않은 알림만 조회
     */
    override fun getUnreadNotifications(): Flow<List<Notification>> {
        return dao.getUnreadNotifications()
            .map { entities -> entities.map { entityToDomain(it) } }
    }

    /**
     * 특정 알림 조회
     */
    override suspend fun getNotificationById(id: String): Notification? = withContext(dispatcher) {
        try {
            val entity = dao.getNotificationById(id)
            entity?.let { entityToDomain(it) }
        } catch (e: Exception) {
            Log.e(TAG, "getNotificationById failed", e)
            null
        }
    }

    /**
     * 알림 추가
     */
    override suspend fun addNotification(notification: Notification) = withContext(dispatcher) {
        try {
            dao.insertNotification(domainToEntity(notification))
            Log.d(TAG, "Notification added: ${notification.id}")

            // 오래된 알림 자동 정리 (30일 이상)
            cleanupOldNotifications()
        } catch (e: Exception) {
            Log.e(TAG, "addNotification failed", e)
        }
    }

    /**
     * 여러 알림 추가
     */
    override suspend fun addNotifications(notifications: List<Notification>) = withContext(dispatcher) {
        try {
            val entities = notifications.map { domainToEntity(it) }
            dao.insertNotifications(entities)
            Log.d(TAG, "${notifications.size} notifications added")

            // 오래된 알림 자동 정리
            cleanupOldNotifications()
        } catch (e: Exception) {
            Log.e(TAG, "addNotifications failed", e)
        }
    }

    /**
     * 알림을 읽음 처리
     */
    override suspend fun markAsRead(notificationId: String) = withContext(dispatcher) {
        try {
            dao.markAsRead(notificationId)
            Log.d(TAG, "Notification marked as read: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "markAsRead failed", e)
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    override suspend fun markAllAsRead() = withContext(dispatcher) {
        try {
            dao.markAllAsRead()
            Log.d(TAG, "All notifications marked as read")
        } catch (e: Exception) {
            Log.e(TAG, "markAllAsRead failed", e)
        }
    }

    /**
     * 알림 삭제
     */
    override suspend fun deleteNotification(notificationId: String) = withContext(dispatcher) {
        try {
            dao.deleteNotification(notificationId)
            Log.d(TAG, "Notification deleted: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "deleteNotification failed", e)
        }
    }

    /**
     * 읽은 알림 모두 삭제
     */
    override suspend fun deleteReadNotifications() = withContext(dispatcher) {
        try {
            dao.deleteReadNotifications()
            Log.d(TAG, "Read notifications deleted")
        } catch (e: Exception) {
            Log.e(TAG, "deleteReadNotifications failed", e)
        }
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    override fun getUnreadCount(): Flow<Int> {
        return dao.getUnreadCount()
    }

    /**
     * 오래된 알림 자동 정리 (30일 이상)
     */
    private suspend fun cleanupOldNotifications() {
        try {
            val cutoffTime = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -MAX_NOTIFICATION_AGE_DAYS)
            }.timeInMillis

            dao.deleteOldNotifications(cutoffTime)
            Log.d(TAG, "Old notifications cleaned up (older than $MAX_NOTIFICATION_AGE_DAYS days)")
        } catch (e: Exception) {
            Log.e(TAG, "cleanupOldNotifications failed", e)
        }
    }

    /**
     * Entity를 Domain 모델로 변환
     */
    private fun entityToDomain(entity: NotificationEntity): Notification {
        return Notification(
            id = entity.id,
            title = entity.title,
            message = entity.message,
            timestamp = entity.timestamp,
            isRead = entity.isRead,
            relatedCaptureId = entity.relatedCaptureId,
            type = try {
                NotificationType.valueOf(entity.type)
            } catch (e: Exception) {
                NotificationType.INFO
            }
        )
    }

    /**
     * Domain 모델을 Entity로 변환
     */
    private fun domainToEntity(notification: Notification): NotificationEntity {
        return NotificationEntity(
            id = notification.id,
            title = notification.title,
            message = notification.message,
            timestamp = notification.timestamp,
            isRead = notification.isRead,
            relatedCaptureId = notification.relatedCaptureId,
            type = notification.type.name
        )
    }
}
