package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.ExtractedEntityDao
import com.example.kairos_mobile.data.mapper.EntityMapper
import com.example.kairos_mobile.domain.model.ExtractedEntity
import com.example.kairos_mobile.domain.repository.ExtractedEntityRepository
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

