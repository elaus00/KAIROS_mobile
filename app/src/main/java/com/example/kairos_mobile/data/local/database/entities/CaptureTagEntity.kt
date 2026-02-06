package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 캡처-태그 연결 Room Entity (M:N 관계)
 */
@Entity(
    tableName = "capture_tags",
    primaryKeys = ["capture_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = CaptureEntity::class,
            parentColumns = ["id"],
            childColumns = ["capture_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["capture_id"]),
        Index(value = ["tag_id"])
    ]
)
data class CaptureTagEntity(
    @ColumnInfo(name = "capture_id")
    val captureId: String,

    @ColumnInfo(name = "tag_id")
    val tagId: String
)
