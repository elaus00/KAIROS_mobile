package com.flit.app.data.mapper

import com.flit.app.data.local.database.entities.NoteEntity
import com.flit.app.domain.model.Note
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
            updatedAt = entity.updatedAt,
            body = entity.body
        )
    }

    fun toEntity(note: Note): NoteEntity {
        return NoteEntity(
            id = note.id,
            captureId = note.captureId,
            folderId = note.folderId,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            body = note.body
        )
    }
}
