package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.AnalyticsEventDao
import com.example.kairos_mobile.data.mapper.AnalyticsEventMapper
import com.example.kairos_mobile.domain.model.AnalyticsEvent
import com.example.kairos_mobile.domain.repository.AnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 분석 이벤트 Repository 구현체
 */
@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsEventDao: AnalyticsEventDao,
    private val mapper: AnalyticsEventMapper
) : AnalyticsRepository {

    override suspend fun insert(event: AnalyticsEvent) {
        analyticsEventDao.insert(mapper.toEntity(event))
    }

    override suspend fun getUnsynced(limit: Int): List<AnalyticsEvent> {
        return analyticsEventDao.getUnsynced(limit).map { mapper.toDomain(it) }
    }

    override suspend fun markSynced(ids: List<String>) {
        analyticsEventDao.markSynced(ids)
    }

    override suspend fun deleteOld(threshold: Long) {
        analyticsEventDao.deleteOld(threshold)
    }
}
