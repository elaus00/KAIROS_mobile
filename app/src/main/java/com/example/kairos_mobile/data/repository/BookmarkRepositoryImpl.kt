package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.BookmarkDao
import com.example.kairos_mobile.data.mapper.BookmarkMapper
import com.example.kairos_mobile.domain.model.Bookmark
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bookmark Repository 구현체 (PRD v4.0)
 */
@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {

    override fun getAllBookmarks(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarks().map { entities ->
            BookmarkMapper.toDomainList(entities)
        }
    }

    override fun searchBookmarks(query: String): Flow<List<Bookmark>> {
        return bookmarkDao.searchBookmarks(query).map { entities ->
            BookmarkMapper.toDomainList(entities)
        }
    }

    override fun getBookmarksByTag(tag: String): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksByTag(tag).map { entities ->
            BookmarkMapper.toDomainList(entities)
        }
    }

    override fun getRecentBookmarks(limit: Int): Flow<List<Bookmark>> {
        return bookmarkDao.getRecentBookmarks(limit).map { entities ->
            BookmarkMapper.toDomainList(entities)
        }
    }

    override suspend fun createBookmark(bookmark: Bookmark): Result<Bookmark> {
        return try {
            // URL 중복 체크
            if (bookmarkDao.existsByUrl(bookmark.url)) {
                return Result.Error(IllegalStateException("이미 동일한 URL의 북마크가 존재합니다."))
            }

            val entity = BookmarkMapper.toEntity(bookmark)
            bookmarkDao.insert(entity)
            Result.Success(bookmark)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun createBookmarkFromInsight(
        insightId: String,
        title: String,
        url: String,
        summary: String?,
        tags: List<String>
    ): Result<Bookmark> {
        return try {
            // 이미 해당 인사이트로 생성된 북마크가 있는지 확인
            val existing = bookmarkDao.getBookmarkByInsightId(insightId)
            if (existing != null) {
                return Result.Success(BookmarkMapper.toDomain(existing))
            }

            // URL 중복 체크
            if (bookmarkDao.existsByUrl(url)) {
                val existingByUrl = bookmarkDao.getByUrl(url)
                if (existingByUrl != null) {
                    return Result.Success(BookmarkMapper.toDomain(existingByUrl))
                }
            }

            val bookmark = Bookmark(
                id = UUID.randomUUID().toString(),
                title = title,
                url = url,
                summary = summary,
                tags = tags,
                faviconUrl = null,
                sourceInsightId = insightId,
                createdAt = Instant.now()
            )

            val entity = BookmarkMapper.toEntity(bookmark)
            bookmarkDao.insert(entity)
            Result.Success(bookmark)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateBookmark(bookmark: Bookmark): Result<Bookmark> {
        return try {
            val entity = BookmarkMapper.toEntity(bookmark)
            bookmarkDao.update(entity)
            Result.Success(bookmark)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteBookmark(id: String): Result<Unit> {
        return try {
            bookmarkDao.deleteById(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getBookmarkById(id: String): Result<Bookmark?> {
        return try {
            val entity = bookmarkDao.getById(id)
            Result.Success(entity?.let { BookmarkMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getBookmarkByUrl(url: String): Result<Bookmark?> {
        return try {
            val entity = bookmarkDao.getByUrl(url)
            Result.Success(entity?.let { BookmarkMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun existsByUrl(url: String): Boolean {
        return bookmarkDao.existsByUrl(url)
    }

    override fun getBookmarkCount(): Flow<Int> {
        return bookmarkDao.getBookmarkCount()
    }
}
