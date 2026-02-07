package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.ClassificationLogDao
import com.example.kairos_mobile.data.mapper.ClassificationLogMapper
import com.example.kairos_mobile.domain.model.ClassificationLog
import com.example.kairos_mobile.domain.repository.ClassificationLogRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 분류 수정 로그 Repository 구현체
 */
@Singleton
class ClassificationLogRepositoryImpl @Inject constructor(
    private val classificationLogDao: ClassificationLogDao,
    private val mapper: ClassificationLogMapper
) : ClassificationLogRepository {

    override suspend fun insert(log: ClassificationLog) {
        classificationLogDao.insert(mapper.toEntity(log))
    }

    override suspend fun getByCaptureId(captureId: String): List<ClassificationLog> {
        return classificationLogDao.getByCaptureId(captureId).map { mapper.toDomain(it) }
    }

    override suspend fun getRecentLogs(limit: Int): List<ClassificationLog> {
        return classificationLogDao.getRecentLogs(limit).map { mapper.toDomain(it) }
    }
}
