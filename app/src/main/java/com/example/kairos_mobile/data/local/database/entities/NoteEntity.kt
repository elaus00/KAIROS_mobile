package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 노트 Room Entity
 * Capture가 NOTES로 분류될 때 생성되는 파생 엔티티.
 */
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = CaptureEntity::class,
            parentColumns = ["id"],
            childColumns = ["capture_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["capture_id"], unique = true),
        Index(value = ["folder_id"])
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String,

    // FK → captures.id (UNIQUE)
    @ColumnInfo(name = "capture_id")
    val captureId: String,

    // FK → folders.id
    @ColumnInfo(name = "folder_id")
    val folderId: String? = null,

    // 생성 시각 (epoch ms)
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    // 최종 수정 시각
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
