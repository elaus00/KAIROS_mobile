package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.CaptureEntity
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.ConfidenceLevel
import com.example.kairos_mobile.domain.model.NoteSubType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Capture Entity ↔ Domain 변환 Mapper
 */
@Singleton
class CaptureMapper @Inject constructor() {

    /**
     * CaptureEntity → Capture (Domain)
     */
    fun toDomain(entity: CaptureEntity): Capture {
        return Capture(
            id = entity.id,
            originalText = entity.originalText,
            aiTitle = entity.aiTitle,
            classifiedType = parseClassifiedType(entity.classifiedType),
            noteSubType = entity.noteSubType?.let { parseNoteSubType(it) },
            confidence = entity.confidence?.let { parseConfidenceLevel(it) },
            source = parseCaptureSource(entity.source),
            isConfirmed = entity.isConfirmed,
            confirmedAt = entity.confirmedAt,
            isDeleted = entity.isDeleted,
            deletedAt = entity.deletedAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            classificationCompletedAt = entity.classificationCompletedAt,
            isTrashed = entity.isTrashed,
            trashedAt = entity.trashedAt,
            imageUri = entity.imageUri,
            parentCaptureId = entity.parentCaptureId
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

    private fun parseConfidenceLevel(value: String): ConfidenceLevel {
        return try {
            ConfidenceLevel.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ConfidenceLevel.MEDIUM
        }
    }

    private fun parseCaptureSource(value: String): CaptureSource {
        return try {
            CaptureSource.valueOf(value)
        } catch (e: IllegalArgumentException) {
            CaptureSource.APP
        }
    }

    /**
     * Capture (Domain) → CaptureEntity
     */
    fun toEntity(capture: Capture): CaptureEntity {
        return CaptureEntity(
            id = capture.id,
            originalText = capture.originalText,
            aiTitle = capture.aiTitle,
            classifiedType = capture.classifiedType.name,
            noteSubType = capture.noteSubType?.name,
            confidence = capture.confidence?.name,
            source = capture.source.name,
            isConfirmed = capture.isConfirmed,
            confirmedAt = capture.confirmedAt,
            isDeleted = capture.isDeleted,
            deletedAt = capture.deletedAt,
            createdAt = capture.createdAt,
            updatedAt = capture.updatedAt,
            classificationCompletedAt = capture.classificationCompletedAt,
            isTrashed = capture.isTrashed,
            trashedAt = capture.trashedAt,
            imageUri = capture.imageUri,
            parentCaptureId = capture.parentCaptureId
        )
    }
}
