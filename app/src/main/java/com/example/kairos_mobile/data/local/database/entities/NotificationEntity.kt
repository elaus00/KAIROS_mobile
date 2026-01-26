package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 알림 Room Entity
 * 앱 내 알림들을 저장
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    /**
     * 알림 고유 ID
     */
    @PrimaryKey
    val id: String,

    /**
     * 알림 제목
     */
    val title: String,

    /**
     * 알림 메시지 내용
     */
    val message: String,

    /**
     * 알림 생성 시간 (Unix timestamp milliseconds)
     */
    val timestamp: Long,

    /**
     * 읽음 여부
     */
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    /**
     * 연관된 캡처 ID
     */
    @ColumnInfo(name = "related_capture_id")
    val relatedCaptureId: String? = null,

    /**
     * 알림 타입 (NotificationType.name)
     */
    @ColumnInfo(name = "type")
    val type: String = "INFO"
)
