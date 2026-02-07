package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.AnalyticsEventEntity
import com.example.kairos_mobile.domain.model.AnalyticsEvent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AnalyticsEvent Entity ↔ Domain 변환 Mapper
 */
@Singleton
class AnalyticsEventMapper @Inject constructor() {

    fun toDomain(entity: AnalyticsEventEntity): AnalyticsEvent {
        return AnalyticsEvent(
            id = entity.id,
            eventType = entity.eventType,
            eventData = entity.eventData,
            timestamp = entity.timestamp,
            isSynced = entity.isSynced
        )
    }

    fun toEntity(event: AnalyticsEvent): AnalyticsEventEntity {
        return AnalyticsEventEntity(
            id = event.id,
            eventType = event.eventType,
            eventData = event.eventData,
            timestamp = event.timestamp,
            isSynced = event.isSynced
        )
    }
}
