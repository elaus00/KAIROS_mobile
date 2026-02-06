package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

/**
 * 캡처 전문 검색 FTS4 가상 테이블
 * contentEntity를 사용하지 않는 독립 FTS 테이블.
 * 캡처 생성/수정 시 수동으로 동기화해야 한다.
 */
@Fts4
@Entity(tableName = "capture_search")
data class CaptureSearchFts(
    // 원본 Capture 참조 ID
    @ColumnInfo(name = "capture_id")
    val captureId: String,

    // AI 제목
    @ColumnInfo(name = "title_text")
    val titleText: String? = null,

    // 원문
    @ColumnInfo(name = "original_text")
    val originalText: String,

    // 태그 합산 텍스트
    @ColumnInfo(name = "tag_text")
    val tagText: String? = null,

    // 엔티티 합산 텍스트
    @ColumnInfo(name = "entity_text")
    val entityText: String? = null
)
