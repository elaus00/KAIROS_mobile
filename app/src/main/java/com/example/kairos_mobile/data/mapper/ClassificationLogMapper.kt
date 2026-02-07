package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.ClassificationLogEntity
import com.example.kairos_mobile.domain.model.ClassificationLog
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.NoteSubType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ClassificationLog Entity ↔ Domain 변환 Mapper
 */
@Singleton
class ClassificationLogMapper @Inject constructor() {

    fun toDomain(entity: ClassificationLogEntity): ClassificationLog {
        return ClassificationLog(
            id = entity.id,
            captureId = entity.captureId,
            originalType = parseClassifiedType(entity.originalType),
            originalSubType = entity.originalSubType?.let { parseNoteSubType(it) },
            newType = parseClassifiedType(entity.newType),
            newSubType = entity.newSubType?.let { parseNoteSubType(it) },
            timeSinceClassificationMs = entity.timeSinceClassificationMs,
            modifiedAt = entity.modifiedAt
        )
    }

    fun toEntity(log: ClassificationLog): ClassificationLogEntity {
        return ClassificationLogEntity(
            id = log.id,
            captureId = log.captureId,
            originalType = log.originalType.name,
            originalSubType = log.originalSubType?.name,
            newType = log.newType.name,
            newSubType = log.newSubType?.name,
            timeSinceClassificationMs = log.timeSinceClassificationMs,
            modifiedAt = log.modifiedAt
        )
    }

    private fun parseClassifiedType(value: String): ClassifiedType {
        return try {
            ClassifiedType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ClassifiedType.TEMP
        }
    }

    private fun parseNoteSubType(value: String): NoteSubType {
        return try {
            NoteSubType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            NoteSubType.INBOX
        }
    }
}
