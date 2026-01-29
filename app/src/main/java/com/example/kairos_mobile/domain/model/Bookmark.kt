package com.example.kairos_mobile.domain.model

import java.time.Instant

/**
 * 북마크 도메인 모델 (PRD v4.0)
 */
data class Bookmark(
    val id: String,
    val title: String,
    val url: String,
    val summary: String? = null,
    val tags: List<String> = emptyList(),
    val faviconUrl: String? = null,
    val sourceCaptureId: String? = null,
    val createdAt: Instant
) {
    /**
     * 도메인 추출 (예: "example.com")
     */
    fun getDomain(): String {
        return try {
            val uri = java.net.URI(url)
            uri.host?.removePrefix("www.") ?: url
        } catch (e: Exception) {
            url
        }
    }

    /**
     * 요약 미리보기 (최대 150자)
     */
    fun getSummaryPreview(maxLength: Int = 150): String? {
        return summary?.let {
            if (it.length <= maxLength) it else it.take(maxLength) + "..."
        }
    }

    /**
     * 태그 문자열 (예: "#article #tech")
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
     * 검색어와 매칭되는지 확인 (제목, URL, 요약, 태그)
     */
    fun matchesQuery(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return title.lowercase().contains(lowerQuery) ||
                url.lowercase().contains(lowerQuery) ||
                summary?.lowercase()?.contains(lowerQuery) == true ||
                tags.any { it.lowercase().contains(lowerQuery) }
    }
}
