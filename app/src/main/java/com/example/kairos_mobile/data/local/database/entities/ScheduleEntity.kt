package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Schedule Room Entity (PRD v4.0)
 * 일정 저장
 */
@Entity(
    tableName = "schedules",
    indices = [
        Index(value = ["date"]),
        Index(value = ["category"]),
        Index(value = ["google_calendar_id"])
    ]
)
data class ScheduleEntity(
    @PrimaryKey
    val id: String,

    // 일정 제목
    val title: String,

    // 시간 (HH:mm 형식)
    val time: String,

    // 날짜 (epoch day)
    val date: Long,

    // 장소 (선택)
    val location: String? = null,

    // 카테고리: WORK, PERSONAL
    val category: String,

    // Google Calendar 이벤트 ID (선택)
    @ColumnInfo(name = "google_calendar_id")
    val googleCalendarId: String? = null,

    // 원본 캡처 ID (캡처에서 생성된 경우)
    @ColumnInfo(name = "source_capture_id")
    val sourceCaptureId: String? = null,

    // 생성 시간 (epoch millis)
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    // 수정 시간 (epoch millis)
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
