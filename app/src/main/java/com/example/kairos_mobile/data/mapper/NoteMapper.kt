package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.NoteEntity
import com.example.kairos_mobile.domain.model.Note
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Note Entity ↔ Domain 변환 Mapper
 */
@Singleton
class NoteMapper @Inject constructor() {

    fun toDomain(entity: NoteEntity): Note {
        return Note(
            id = entity.id,
            captureId = entity.captureId,
            folderId = entity.folderId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(note: Note): NoteEntity {
        return NoteEntity(
            id = note.id,
            captureId = note.captureId,
            folderId = note.folderId,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt
        )
    }
}
