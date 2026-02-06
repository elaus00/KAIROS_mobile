package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.TagEntity
import com.example.kairos_mobile.domain.model.Tag
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tag Entity ↔ Domain 변환 Mapper
 */
@Singleton
class TagMapper @Inject constructor() {

    fun toDomain(entity: TagEntity): Tag {
        return Tag(
            id = entity.id,
            name = entity.name,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(tag: Tag): TagEntity {
        return TagEntity(
            id = tag.id,
            name = tag.name,
            createdAt = tag.createdAt
        )
    }
}
