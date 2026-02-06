package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.SyncQueueEntity
import com.example.kairos_mobile.domain.model.SyncAction
import com.example.kairos_mobile.domain.model.SyncQueueItem
import com.example.kairos_mobile.domain.model.SyncQueueStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SyncQueue Entity ↔ Domain 변환 Mapper
 */
@Singleton
class SyncQueueMapper @Inject constructor() {

    fun toDomain(entity: SyncQueueEntity): SyncQueueItem {
        return SyncQueueItem(
            id = entity.id,
            action = parseSyncAction(entity.action),
            payload = entity.payload,
            retryCount = entity.retryCount,
            maxRetries = entity.maxRetries,
            status = parseSyncQueueStatus(entity.status),
            createdAt = entity.createdAt,
            nextRetryAt = entity.nextRetryAt
        )
    }

    private fun parseSyncAction(value: String): SyncAction {
        return try {
            SyncAction.valueOf(value)
        } catch (e: IllegalArgumentException) {
            SyncAction.CLASSIFY
        }
    }

    private fun parseSyncQueueStatus(value: String): SyncQueueStatus {
        return try {
            SyncQueueStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            SyncQueueStatus.PENDING
        }
    }

    fun toEntity(item: SyncQueueItem): SyncQueueEntity {
        return SyncQueueEntity(
            id = item.id,
            action = item.action.name,
            payload = item.payload,
            retryCount = item.retryCount,
            maxRetries = item.maxRetries,
            status = item.status.name,
            createdAt = item.createdAt,
            nextRetryAt = item.nextRetryAt
        )
    }
}
