package com.example.kairos_mobile.domain.model

import java.time.Instant

/**
 * 노트 도메인 모델 (PRD v4.0)
 */
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val folder: NoteFolder = NoteFolder.INBOX,
    val tags: List<String> = emptyList(),
    val sourceCaptureId: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant = createdAt
) {
    /**
     * 노트 미리보기 (최대 100자)
     */
    fun getPreview(maxLength: Int = 100): String {
        return if (content.length <= maxLength) {
            content
        } else {
            content.take(maxLength) + "..."
        }
    }

    /**
     * 태그 문자열 (예: "#work #important")
     */
    fun getTagsString(): String {
        return tags.joinToString(" ") { "#$it" }
    }

    /**
     * 특정 태그가 있는지 확인
     */
    fun hasTag(tag: String): Boolean {
        return tags.any { it.equals(tag, ignoreCase = true) }
    }

    /**
     * 검색어와 매칭되는지 확인 (제목, 내용, 태그)
     */
    fun matchesQuery(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return title.lowercase().contains(lowerQuery) ||
                content.lowercase().contains(lowerQuery) ||
                tags.any { it.lowercase().contains(lowerQuery) }
    }
}

/**
 * 노트 폴더
 */
enum class NoteFolder {
    INBOX,      // 인박스 (기본)
    IDEAS,      // 아이디어
    REFERENCES; // 레퍼런스

    fun getDisplayName(): String {
        return when (this) {
            INBOX -> "인박스"
            IDEAS -> "아이디어"
            REFERENCES -> "레퍼런스"
        }
    }

    fun getEnglishName(): String {
        return when (this) {
            INBOX -> "Inbox"
            IDEAS -> "Ideas"
            REFERENCES -> "References"
        }
    }
}
