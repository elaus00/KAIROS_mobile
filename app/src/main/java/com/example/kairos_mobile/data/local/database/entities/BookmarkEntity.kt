package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bookmark Room Entity (PRD v4.0)
 * 북마크 저장
 */
@Entity(
    tableName = "bookmarks",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["url"], unique = true)
    ]
)
data class BookmarkEntity(
    @PrimaryKey
    val id: String,

    // 북마크 제목
    val title: String,

    // URL
    val url: String,

    // AI 요약 (선택)
    val summary: String? = null,

    // 태그 (JSON 배열 문자열)
    val tags: String? = null,

    // 파비콘 URL (선택)
    @ColumnInfo(name = "favicon_url")
    val faviconUrl: String? = null,

    // 원본 캡처 ID (캡처에서 생성된 경우)
    @ColumnInfo(name = "source_capture_id")
    val sourceCaptureId: String? = null,

    // 생성 시간 (epoch millis)
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
