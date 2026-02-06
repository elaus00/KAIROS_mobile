package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 태그 Room Entity
 */
@Entity(
    tableName = "tags",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class TagEntity(
    @PrimaryKey
    val id: String,

    // 태그명 (UNIQUE)
    val name: String,

    // 생성 시각 (epoch ms)
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
