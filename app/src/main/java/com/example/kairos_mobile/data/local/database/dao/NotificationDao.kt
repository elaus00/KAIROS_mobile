package com.example.kairos_mobile.data.local.database.dao

import androidx.room.*
import com.example.kairos_mobile.data.local.database.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * 알림 Data Access Object
 */
@Dao
interface NotificationDao {

    /**
     * 모든 알림 조회 (최신순)
     */
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    /**
     * 읽지 않은 알림만 조회
     */
    @Query("SELECT * FROM notifications WHERE is_read = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(): Flow<List<NotificationEntity>>

    /**
     * 특정 알림 조회
     */
    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getNotificationById(id: String): NotificationEntity?

    /**
     * 알림 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    /**
     * 여러 알림 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    /**
     * 알림 읽음 상태 업데이트
     */
    @Query("UPDATE notifications SET is_read = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    /**
     * 모든 알림 읽음 처리
     */
    @Query("UPDATE notifications SET is_read = 1")
    suspend fun markAllAsRead()

    /**
     * 특정 알림 삭제
     */
    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    /**
     * 읽은 알림 모두 삭제
     */
    @Query("DELETE FROM notifications WHERE is_read = 1")
    suspend fun deleteReadNotifications()

    /**
     * 오래된 알림 삭제 (30일 이상)
     */
    @Query("DELETE FROM notifications WHERE timestamp < :timestamp")
    suspend fun deleteOldNotifications(timestamp: Long)

    /**
     * 읽지 않은 알림 개수 조회
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE is_read = 0")
    fun getUnreadCount(): Flow<Int>

    /**
     * 모든 알림 삭제 (테스트용)
     */
    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}
