package com.flit.app.data.mapper

import com.flit.app.data.local.database.entities.FolderEntity
import com.flit.app.domain.model.Folder
import com.flit.app.domain.model.FolderType
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
