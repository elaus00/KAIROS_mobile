package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kairos_mobile.data.local.database.entities.BookmarkEntity
import kotlinx.coroutines.flow.Flow

/**
 * Bookmark DAO (PRD v4.0)
 * 북마크 데이터베이스 접근
 */
@Dao
interface BookmarkDao {

    /**
     * 새 북마크 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    /**
     * 북마크 업데이트
     */
    @Update
    suspend fun update(bookmark: BookmarkEntity)

    /**
     * 북마크 삭제
     */
    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * ID로 북마크 조회
     */
    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getById(id: String): BookmarkEntity?

    /**
     * URL로 북마크 조회 (중복 체크용)
     */
    @Query("SELECT * FROM bookmarks WHERE url = :url")
    suspend fun getByUrl(url: String): BookmarkEntity?

    /**
     * 모든 북마크 조회 (최신순)
     */
    @Query("SELECT * FROM bookmarks ORDER BY created_at DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    /**
     * 북마크 검색 (제목, URL, 요약)
     */
    @Query("""
        SELECT * FROM bookmarks
        WHERE title LIKE '%' || :query || '%'
           OR url LIKE '%' || :query || '%'
           OR summary LIKE '%' || :query || '%'
        ORDER BY created_at DESC
    """)
    fun searchBookmarks(query: String): Flow<List<BookmarkEntity>>

    /**
     * 태그로 북마크 검색
     */
    @Query("""
        SELECT * FROM bookmarks
        WHERE tags LIKE '%' || :tag || '%'
        ORDER BY created_at DESC
    """)
    fun getBookmarksByTag(tag: String): Flow<List<BookmarkEntity>>

    /**
     * 캡처 ID로 연결된 북마크 조회
     */
    @Query("SELECT * FROM bookmarks WHERE source_capture_id = :captureId")
    suspend fun getBookmarkByCaptureId(captureId: String): BookmarkEntity?

    /**
     * 북마크 개수 조회
     */
    @Query("SELECT COUNT(*) FROM bookmarks")
    fun getBookmarkCount(): Flow<Int>

    /**
     * 최근 북마크 조회 (제한된 개수)
     */
    @Query("SELECT * FROM bookmarks ORDER BY created_at DESC LIMIT :limit")
    fun getRecentBookmarks(limit: Int): Flow<List<BookmarkEntity>>

    /**
     * URL 존재 여부 확인
     */
    @Query("SELECT COUNT(*) > 0 FROM bookmarks WHERE url = :url")
    suspend fun existsByUrl(url: String): Boolean
}
