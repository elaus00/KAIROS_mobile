package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 분석 이벤트 Entity
 * 주요 액션에 대한 분석 이벤트를 로컬에 저장 후 배치 전송
 */
@Entity(
    tableName = "analytics_events",
    indices = [
        Index(value = ["is_synced"]),
        Index(value = ["timestamp"])
    ]
)
data class AnalyticsEventEntity(
    @PrimaryKey
    val id: String,

    // 이벤트 유형 (capture_created, classification_completed 등)
    @ColumnInfo(name = "event_type")
    val eventType: String,

    // 이벤트 데이터 (JSON 문자열)
    @ColumnInfo(name = "event_data")
    val eventData: String? = null,

    // 이벤트 발생 시각
    val timestamp: Long,

    // 서버 동기화 여부
    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val isSynced: Boolean = false
)
