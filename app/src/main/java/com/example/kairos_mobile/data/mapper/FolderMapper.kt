package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.FolderEntity
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.FolderType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Folder Entity ↔ Domain 변환 Mapper
 */
@Singleton
class FolderMapper @Inject constructor() {

    fun toDomain(entity: FolderEntity): Folder {
        return Folder(
            id = entity.id,
            name = entity.name,
            type = parseFolderType(entity.type),
            sortOrder = entity.sortOrder,
            createdAt = entity.createdAt
        )
    }

    private fun parseFolderType(value: String): FolderType {
        return try {
            FolderType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            FolderType.USER
        }
    }

    fun toEntity(folder: Folder): FolderEntity {
        return FolderEntity(
            id = folder.id,
            name = folder.name,
            type = folder.type.name,
            sortOrder = folder.sortOrder,
            createdAt = folder.createdAt
        )
    }
}
