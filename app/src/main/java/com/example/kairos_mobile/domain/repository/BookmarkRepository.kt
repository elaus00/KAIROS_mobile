package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.Bookmark
import com.example.kairos_mobile.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Bookmark Repository 인터페이스 (PRD v4.0)
 */
interface BookmarkRepository {

    /**
     * 모든 북마크 조회 (최신순)
     */
    fun getAllBookmarks(): Flow<List<Bookmark>>

    /**
     * 북마크 검색 (제목, URL, 요약)
     */
    fun searchBookmarks(query: String): Flow<List<Bookmark>>

    /**
     * 태그로 북마크 검색
     */
    fun getBookmarksByTag(tag: String): Flow<List<Bookmark>>

    /**
     * 최근 북마크 조회 (제한된 개수)
     */
    fun getRecentBookmarks(limit: Int = 10): Flow<List<Bookmark>>

    /**
     * 새 북마크 생성
     */
    suspend fun createBookmark(bookmark: Bookmark): Result<Bookmark>

    /**
     * 인사이트에서 북마크 생성
     */
    suspend fun createBookmarkFromInsight(
        insightId: String,
        title: String,
        url: String,
        summary: String? = null,
        tags: List<String> = emptyList()
    ): Result<Bookmark>

    /**
     * 북마크 업데이트
     */
    suspend fun updateBookmark(bookmark: Bookmark): Result<Bookmark>

    /**
     * 북마크 삭제
     */
    suspend fun deleteBookmark(id: String): Result<Unit>

    /**
     * ID로 북마크 조회
     */
    suspend fun getBookmarkById(id: String): Result<Bookmark?>

    /**
     * URL로 북마크 조회
     */
    suspend fun getBookmarkByUrl(url: String): Result<Bookmark?>

    /**
     * URL 존재 여부 확인 (중복 체크용)
     */
    suspend fun existsByUrl(url: String): Boolean

    /**
     * 북마크 개수 조회
     */
    fun getBookmarkCount(): Flow<Int>
}
