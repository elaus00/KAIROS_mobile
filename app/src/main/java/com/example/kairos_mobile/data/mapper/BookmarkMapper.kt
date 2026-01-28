package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.BookmarkEntity
import com.example.kairos_mobile.domain.model.Bookmark
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant

/**
 * Bookmark Entity <-> Domain 변환 매퍼 (PRD v4.0)
 */
object BookmarkMapper {

    private val gson = Gson()

    /**
     * Entity -> Domain 변환
     */
    fun toDomain(entity: BookmarkEntity): Bookmark {
        return Bookmark(
            id = entity.id,
            title = entity.title,
            url = entity.url,
            summary = entity.summary,
            tags = parseTags(entity.tags),
            faviconUrl = entity.faviconUrl,
            sourceInsightId = entity.sourceInsightId,
            createdAt = Instant.ofEpochMilli(entity.createdAt)
        )
    }

    /**
     * Domain -> Entity 변환
     */
    fun toEntity(domain: Bookmark): BookmarkEntity {
        return BookmarkEntity(
            id = domain.id,
            title = domain.title,
            url = domain.url,
            summary = domain.summary,
            tags = serializeTags(domain.tags),
            faviconUrl = domain.faviconUrl,
            sourceInsightId = domain.sourceInsightId,
            createdAt = domain.createdAt.toEpochMilli()
        )
    }

    /**
     * Entity 리스트 -> Domain 리스트 변환
     */
    fun toDomainList(entities: List<BookmarkEntity>): List<Bookmark> {
        return entities.map { toDomain(it) }
    }

    /**
     * JSON 문자열에서 태그 리스트 파싱
     */
    private fun parseTags(tagsJson: String?): List<String> {
        if (tagsJson.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(tagsJson, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 태그 리스트를 JSON 문자열로 직렬화
     */
    private fun serializeTags(tags: List<String>): String? {
        if (tags.isEmpty()) return null
        return gson.toJson(tags)
    }
}
