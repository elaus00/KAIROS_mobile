package com.example.kairos_mobile.data.repository

import androidx.work.WorkManager
import com.example.kairos_mobile.data.local.database.dao.SyncQueueDao
import com.example.kairos_mobile.data.mapper.SyncQueueMapper
import com.example.kairos_mobile.data.worker.ClassifyCaptureWorker
import com.example.kairos_mobile.domain.model.SyncQueueItem
import com.example.kairos_mobile.domain.model.SyncQueueStatus
import com.example.kairos_mobile.domain.repository.SyncQueueRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 동기화 큐 Repository 구현체
 */
@Singleton
class SyncQueueRepositoryImpl @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val syncQueueMapper: SyncQueueMapper,
    private val workManager: WorkManager
) : SyncQueueRepository {

    override suspend fun enqueue(item: SyncQueueItem) {
        syncQueueDao.insert(syncQueueMapper.toEntity(item))
    }

    override fun triggerProcessing() {
        runCatching { ClassifyCaptureWorker.enqueue(workManager) }
    }

    override suspend fun getPendingItems(): List<SyncQueueItem> {
        return syncQueueDao.getPendingItems(System.currentTimeMillis())
            .map { syncQueueMapper.toDomain(it) }
    }

    override suspend fun updateStatus(itemId: String, status: SyncQueueStatus) {
        syncQueueDao.updateStatus(itemId, status.name)
    }

    override suspend fun incrementRetry(itemId: String, nextRetryAt: Long) {
        syncQueueDao.incrementRetry(itemId, nextRetryAt)
    }

    override suspend fun deleteCompleted() {
        syncQueueDao.deleteCompleted()
    }

    override suspend fun resetProcessingToPending() {
        syncQueueDao.resetProcessingToPending()
    }
}
