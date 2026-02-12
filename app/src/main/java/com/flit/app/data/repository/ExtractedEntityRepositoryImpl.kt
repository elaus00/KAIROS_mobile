package com.flit.app.data.repository

import com.flit.app.data.local.database.dao.ExtractedEntityDao
import com.flit.app.data.mapper.EntityMapper
import com.flit.app.domain.model.ExtractedEntity
import com.flit.app.domain.repository.ExtractedEntityRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 추출 엔티티 Repository 구현체
 */
@Singleton
class ExtractedEntityRepositoryImpl @Inject constructor(
    private val extractedEntityDao: ExtractedEntityDao,
    private val entityMapper: EntityMapper
) : ExtractedEntityRepository {

    override suspend fun replaceForCapture(captureId: String, entities: List<ExtractedEntity>) {
        extractedEntityDao.deleteAllForCapture(captureId)
        if (entities.isEmpty()) return

        extractedEntityDao.insertAll(
            entities.map { entity ->
                entityMapper.toEntity(entity.copy(captureId = captureId))
            }
        )
    }
}

