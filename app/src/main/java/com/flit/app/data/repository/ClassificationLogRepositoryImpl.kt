package com.flit.app.data.repository

import com.flit.app.data.local.database.dao.ClassificationLogDao
import com.flit.app.data.mapper.ClassificationLogMapper
import com.flit.app.domain.model.ClassificationLog
import com.flit.app.domain.repository.ClassificationLogRepository
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

    override suspend fun getModificationPatterns(): List<Triple<String, String, Int>> {
        val all = classificationLogDao.getAll()
        return all.groupBy { "${it.originalType}_${it.newType}" }
            .map { (key, logs) ->
                val parts = key.split("_", limit = 2)
                Triple(parts[0], parts[1], logs.size)
            }
            .sortedByDescending { it.third }
    }
}
