package com.flit.app.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 폴더 Room Entity
 * 노트의 분류 컨테이너.
 */
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey
    val id: String,

    // 폴더명
    val name: String,

    // 폴더 유형: INBOX, IDEAS, BOOKMARKS, AI_GROUP, USER
    val type: String,

    // 폴더 정렬 순서
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,

    // 생성 시각 (epoch ms)
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
