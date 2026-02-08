package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.CaptureTagDao
import com.example.kairos_mobile.data.local.database.dao.TagDao
import com.example.kairos_mobile.data.local.database.entities.CaptureTagEntity
import com.example.kairos_mobile.data.local.database.entities.TagEntity
import com.example.kairos_mobile.data.mapper.TagMapper
import com.example.kairos_mobile.domain.model.Tag
import com.example.kairos_mobile.domain.repository.TagRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 태그 Repository 구현체
 */
@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao,
    private val captureTagDao: CaptureTagDao,
    private val tagMapper: TagMapper
) : TagRepository {

    override suspend fun getOrCreate(name: String): Tag {
        // 이름으로 기존 태그 조회
        val existing = tagDao.getByName(name)
        if (existing != null) {
            return tagMapper.toDomain(existing)
        }

        // 새로 생성
        val newTag = Tag(
            id = UUID.randomUUID().toString(),
            name = name,
            createdAt = System.currentTimeMillis()
        )
        tagDao.insert(tagMapper.toEntity(newTag))
        return newTag
    }

    override suspend fun linkTagToCapture(captureId: String, tagId: String) {
        captureTagDao.insert(
            CaptureTagEntity(captureId = captureId, tagId = tagId)
        )
    }

    override suspend fun deleteTagsByCaptureId(captureId: String) {
        captureTagDao.deleteAllForCapture(captureId)
    }
}
