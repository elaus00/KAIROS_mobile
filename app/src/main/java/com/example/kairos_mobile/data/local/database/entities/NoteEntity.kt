package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Note Room Entity (PRD v4.0)
 * 노트 저장
 */
@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["folder"]),
        Index(value = ["created_at"]),
        Index(value = ["updated_at"])
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String,

    // 노트 제목
    val title: String,

    // 노트 내용
    val content: String,

    // 폴더: INBOX, IDEAS, REFERENCES
    val folder: String,

    // 태그 (JSON 배열 문자열)
    val tags: String? = null,

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
