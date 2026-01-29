package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kairos_mobile.data.local.database.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Note DAO (PRD v4.0)
 * 노트 데이터베이스 접근
 */
@Dao
interface NoteDao {

    /**
     * 새 노트 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    /**
     * 노트 업데이트
     */
    @Update
    suspend fun update(note: NoteEntity)

    /**
     * 노트 삭제
     */
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * ID로 노트 조회
     */
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: String): NoteEntity?

    /**
     * 모든 노트 조회 (최신순)
     */
    @Query("SELECT * FROM notes ORDER BY updated_at DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    /**
     * 폴더별 노트 조회
     */
    @Query("""
        SELECT * FROM notes
        WHERE folder = :folder
        ORDER BY updated_at DESC
    """)
    fun getNotesByFolder(folder: String): Flow<List<NoteEntity>>

    /**
     * 노트 검색 (제목, 내용)
     */
    @Query("""
        SELECT * FROM notes
        WHERE title LIKE '%' || :query || '%'
           OR content LIKE '%' || :query || '%'
        ORDER BY updated_at DESC
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    /**
     * 태그로 노트 검색
     */
    @Query("""
        SELECT * FROM notes
        WHERE tags LIKE '%' || :tag || '%'
        ORDER BY updated_at DESC
    """)
    fun getNotesByTag(tag: String): Flow<List<NoteEntity>>

    /**
     * 캡처 ID로 연결된 노트 조회
     */
    @Query("SELECT * FROM notes WHERE source_capture_id = :captureId")
    suspend fun getNoteByCaptureId(captureId: String): NoteEntity?

    /**
     * 노트 개수 조회
     */
    @Query("SELECT COUNT(*) FROM notes")
    fun getNoteCount(): Flow<Int>

    /**
     * 폴더별 노트 개수 조회
     */
    @Query("SELECT COUNT(*) FROM notes WHERE folder = :folder")
    fun getNoteCountByFolder(folder: String): Flow<Int>

    /**
     * 최근 노트 조회 (제한된 개수)
     */
    @Query("SELECT * FROM notes ORDER BY updated_at DESC LIMIT :limit")
    fun getRecentNotes(limit: Int): Flow<List<NoteEntity>>

    /**
     * 노트 폴더 변경
     */
    @Query("""
        UPDATE notes
        SET folder = :newFolder, updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun moveToFolder(id: String, newFolder: String, updatedAt: Long)
}
