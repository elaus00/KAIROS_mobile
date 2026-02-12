package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 일정 Room Entity
 * Capture가 SCHEDULE로 분류될 때 생성되는 파생 엔티티.
 */
@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = CaptureEntity::class,
            parentColumns = ["id"],
            childColumns = ["capture_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["capture_id"], unique = true),
        Index(value = ["start_time"]),
        Index(value = ["calendar_sync_status"])
    ]
)
data class ScheduleEntity(
    @PrimaryKey
    val id: String,

    // FK → captures.id (UNIQUE)
    @ColumnInfo(name = "capture_id")
    val captureId: String,

    // 시작 일시 (epoch ms)
    @ColumnInfo(name = "start_time")
    val startTime: Long? = null,

    // 종료 일시 (epoch ms)
    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,

    // 장소
    val location: String? = null,

    // 종일 이벤트 여부
    @ColumnInfo(name = "is_all_day", defaultValue = "0")
    val isAllDay: Boolean = false,

    // AI 분류 신뢰도: HIGH, MEDIUM, LOW
    val confidence: String,

    // 기기 캘린더 동기화 상태
    @ColumnInfo(name = "calendar_sync_status", defaultValue = "NOT_LINKED")
    val calendarSyncStatus: String = "NOT_LINKED",

    // 캘린더 이벤트 ID (DB 컬럼은 기존 google_event_id 유지)
    @ColumnInfo(name = "google_event_id")
    val calendarEventId: String? = null,

    // 생성 시각 (epoch ms)
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    // 최종 수정 시각
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
