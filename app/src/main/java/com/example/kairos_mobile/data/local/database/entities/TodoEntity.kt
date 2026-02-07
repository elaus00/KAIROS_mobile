package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 할 일 Room Entity
 * Capture가 TODO로 분류될 때 생성되는 파생 엔티티.
 */
@Entity(
    tableName = "todos",
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
        Index(value = ["is_completed"]),
        Index(value = ["deadline"]),
        Index(value = ["sort_order"])
    ]
)
data class TodoEntity(
    @PrimaryKey
    val id: String,

    // FK → captures.id (UNIQUE)
    @ColumnInfo(name = "capture_id")
    val captureId: String,

    // 마감 일시 (epoch ms)
    val deadline: Long? = null,

    // 완료 여부
    @ColumnInfo(name = "is_completed", defaultValue = "0")
    val isCompleted: Boolean = false,

    // 완료 처리 시각
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    // 리스트 내 정렬 순서
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,

    // 생성 시각 (epoch ms)
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    // 마감일 소스 (AI, USER)
    @ColumnInfo(name = "deadline_source")
    val deadlineSource: String? = null,

    // 정렬 소스 (DEFAULT, AI, USER)
    @ColumnInfo(name = "sort_source", defaultValue = "DEFAULT")
    val sortSource: String = "DEFAULT",

    // 최종 수정 시각
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
