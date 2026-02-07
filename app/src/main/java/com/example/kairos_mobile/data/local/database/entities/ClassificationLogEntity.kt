package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 분류 수정 로그 Entity
 * 사용자가 AI 분류를 수동 변경할 때 기록
 */
@Entity(
    tableName = "classification_logs",
    foreignKeys = [
        ForeignKey(
            entity = CaptureEntity::class,
            parentColumns = ["id"],
            childColumns = ["capture_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["capture_id"]),
        Index(value = ["modified_at"])
    ]
)
data class ClassificationLogEntity(
    @PrimaryKey
    val id: String,

    // FK → captures.id
    @ColumnInfo(name = "capture_id")
    val captureId: String,

    // 변경 전 분류 유형
    @ColumnInfo(name = "original_type")
    val originalType: String,

    // 변경 전 서브 유형
    @ColumnInfo(name = "original_sub_type")
    val originalSubType: String? = null,

    // 변경 후 분류 유형
    @ColumnInfo(name = "new_type")
    val newType: String,

    // 변경 후 서브 유형
    @ColumnInfo(name = "new_sub_type")
    val newSubType: String? = null,

    // AI 분류 후 수정까지 걸린 시간 (ms)
    @ColumnInfo(name = "time_since_classification_ms")
    val timeSinceClassificationMs: Long? = null,

    // 수정 시각
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long
)
