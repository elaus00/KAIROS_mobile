package com.example.kairos_mobile.data.repository

import androidx.room.withTransaction
import com.example.kairos_mobile.data.local.database.KairosDatabase
import com.example.kairos_mobile.data.local.database.dao.CaptureDao
import com.example.kairos_mobile.data.local.database.dao.CaptureSearchDao
import com.example.kairos_mobile.data.local.database.entities.CaptureSearchFts
import com.example.kairos_mobile.data.mapper.CaptureMapper
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.ConfidenceLevel
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.repository.CaptureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캡처 Repository 구현체
 */
@Singleton
class CaptureRepositoryImpl @Inject constructor(
    private val database: KairosDatabase,
    private val captureDao: CaptureDao,
    private val captureSearchDao: CaptureSearchDao,
    private val captureMapper: CaptureMapper
) : CaptureRepository {

    override suspend fun saveCapture(capture: Capture): Capture {
        val entity = captureMapper.toEntity(capture)
        captureDao.insert(entity)
        // FTS 인덱스 추가
        captureSearchDao.insert(
            CaptureSearchFts(
                captureId = capture.id,
                titleText = capture.aiTitle ?: "",
                originalText = capture.originalText,
                tagText = "",
                entityText = ""
            )
        )
        return capture
    }

    override suspend fun getCaptureById(id: String): Capture? {
        return captureDao.getById(id)?.let { captureMapper.toDomain(it) }
    }

    override suspend fun updateClassification(
        captureId: String,
        classifiedType: ClassifiedType,
        noteSubType: NoteSubType?,
        aiTitle: String,
        confidence: ConfidenceLevel
    ) {
        database.withTransaction {
            val now = System.currentTimeMillis()
            captureDao.updateClassification(
                id = captureId,
                classifiedType = classifiedType.name,
                noteSubType = noteSubType?.name,
                completedAt = now,
                updatedAt = now
            )
            captureDao.updateAiTitle(captureId, aiTitle, now)
            captureDao.updateConfidence(captureId, confidence.name, now)
            // FTS 인덱스 업데이트 (제목 변경)
            captureSearchDao.deleteForUpdate(captureId)
            val capture = captureDao.getById(captureId)
            if (capture != null) {
                captureSearchDao.insert(
                    CaptureSearchFts(
                        captureId = captureId,
                        titleText = aiTitle,
                        originalText = capture.originalText,
                        tagText = "",
                        entityText = ""
                    )
                )
            }
        }
    }

    override suspend fun updateClassifiedType(
        captureId: String,
        classifiedType: ClassifiedType,
        noteSubType: NoteSubType?
    ) {
        val now = System.currentTimeMillis()
        captureDao.updateClassification(
            id = captureId,
            classifiedType = classifiedType.name,
            noteSubType = noteSubType?.name,
            completedAt = now,
            updatedAt = now
        )
    }

    override suspend fun confirmClassification(captureId: String) {
        captureDao.confirmClassification(captureId, System.currentTimeMillis())
    }

    override suspend fun confirmAllClassifications() {
        captureDao.confirmAllClassifications(System.currentTimeMillis())
    }

    override suspend fun softDelete(captureId: String) {
        captureDao.softDelete(captureId, System.currentTimeMillis())
    }

    override suspend fun undoSoftDelete(captureId: String) {
        captureDao.undoSoftDelete(captureId, System.currentTimeMillis())
    }

    override suspend fun hardDelete(captureId: String) {
        captureSearchDao.deleteByCaptureId(captureId)
        captureDao.hardDelete(captureId)
    }

    override fun getUnconfirmedClassifications(): Flow<List<Capture>> {
        return captureDao.getUnconfirmedCaptures()
            .map { entities -> entities.map { captureMapper.toDomain(it) } }
    }

    override fun getUnconfirmedCount(): Flow<Int> {
        return captureDao.getUnconfirmedCount()
    }

    override fun getAllCaptures(offset: Int, limit: Int): Flow<List<Capture>> {
        return captureDao.getActiveCapturesPaged(limit, offset)
            .map { entities -> entities.map { captureMapper.toDomain(it) } }
    }

    override suspend fun getTempCaptures(): List<Capture> {
        return captureDao.getTempCaptures().map { captureMapper.toDomain(it) }
    }

    override fun searchCaptures(query: String): Flow<List<Capture>> {
        return captureSearchDao.search(sanitizeFtsQuery(query))
            .map { entities -> entities.map { captureMapper.toDomain(it) } }
    }

    override suspend fun moveToTrash(captureId: String) {
        captureDao.moveToTrash(captureId, System.currentTimeMillis())
    }

    override suspend fun restoreFromTrash(captureId: String) {
        captureDao.restoreFromTrash(captureId, System.currentTimeMillis())
    }

    override fun getTrashedItems(): Flow<List<Capture>> {
        return captureDao.getTrashedItems()
            .map { entities -> entities.map { captureMapper.toDomain(it) } }
    }

    override suspend fun getTrashedOverdue(thresholdMs: Long): List<Capture> {
        return captureDao.getTrashedOverdue(thresholdMs)
            .map { captureMapper.toDomain(it) }
    }

    override fun getChildCaptures(parentId: String): Flow<List<Capture>> {
        return captureDao.getByParentCaptureId(parentId)
            .map { entities -> entities.map { captureMapper.toDomain(it) } }
    }

    override suspend fun getFilteredCaptures(
        type: ClassifiedType?,
        startDate: Long?,
        endDate: Long?,
        limit: Int,
        offset: Int
    ): List<Capture> {
        return captureDao.getFilteredCapturesPaged(
            type = type?.name,
            startDate = startDate,
            endDate = endDate,
            limit = limit,
            offset = offset
        ).map { captureMapper.toDomain(it) }
    }

    override suspend fun searchCapturesFiltered(
        query: String,
        type: ClassifiedType?,
        startDate: Long?,
        endDate: Long?,
        limit: Int
    ): List<Capture> {
        return captureSearchDao.searchFiltered(
            query = sanitizeFtsQuery(query),
            type = type?.name,
            startDate = startDate,
            endDate = endDate,
            limit = limit
        ).map { captureMapper.toDomain(it) }
    }

    private fun sanitizeFtsQuery(query: String): String {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return "\"\""
        }
        val escaped = trimmed.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
