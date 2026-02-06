package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.ExtractedEntityEntity
import com.example.kairos_mobile.domain.model.EntityType
import com.example.kairos_mobile.domain.model.ExtractedEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ExtractedEntity Entity ↔ Domain 변환 Mapper
 */
@Singleton
class EntityMapper @Inject constructor() {

    fun toDomain(entity: ExtractedEntityEntity): ExtractedEntity {
        return ExtractedEntity(
            id = entity.id,
            captureId = entity.captureId,
            type = parseEntityType(entity.type),
            value = entity.value,
            normalizedValue = entity.normalizedValue
        )
    }

    private fun parseEntityType(value: String): EntityType {
        return try {
            EntityType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            EntityType.DATE
        }
    }

    fun toEntity(extractedEntity: ExtractedEntity): ExtractedEntityEntity {
        return ExtractedEntityEntity(
            id = extractedEntity.id,
            captureId = extractedEntity.captureId,
            type = extractedEntity.type.name,
            value = extractedEntity.value,
            normalizedValue = extractedEntity.normalizedValue
        )
    }
}
