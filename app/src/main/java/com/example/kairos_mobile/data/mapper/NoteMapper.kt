package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.NoteEntity
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.NoteFolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant

/**
 * Note Entity <-> Domain 변환 매퍼 (PRD v4.0)
 */
object NoteMapper {

    private val gson = Gson()

    /**
     * Entity -> Domain 변환
     */
    fun toDomain(entity: NoteEntity): Note {
        return Note(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            folder = try {
                NoteFolder.valueOf(entity.folder)
            } catch (e: Exception) {
                NoteFolder.INBOX
            },
            tags = parseTags(entity.tags),
            sourceCaptureId = entity.sourceCaptureId,
            createdAt = Instant.ofEpochMilli(entity.createdAt),
            updatedAt = Instant.ofEpochMilli(entity.updatedAt)
        )
    }

    /**
     * Domain -> Entity 변환
     */
    fun toEntity(domain: Note): NoteEntity {
        return NoteEntity(
            id = domain.id,
            title = domain.title,
            content = domain.content,
            folder = domain.folder.name,
            tags = serializeTags(domain.tags),
            sourceCaptureId = domain.sourceCaptureId,
            createdAt = domain.createdAt.toEpochMilli(),
            updatedAt = domain.updatedAt.toEpochMilli()
        )
    }

    /**
     * Entity 리스트 -> Domain 리스트 변환
     */
    fun toDomainList(entities: List<NoteEntity>): List<Note> {
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
