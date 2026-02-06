package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kairos_mobile.data.local.database.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * 노트 DAO
 */
@Dao
interface NoteDao {

    /**
     * 노트 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    /**
     * 노트 업데이트
     */
    @Update
    suspend fun update(note: NoteEntity)

    /**
     * ID로 노트 조회
     */
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: String): NoteEntity?

    /**
     * 캡처 ID로 노트 조회
     */
    @Query("SELECT * FROM notes WHERE capture_id = :captureId")
    suspend fun getByCaptureId(captureId: String): NoteEntity?

    /**
     * 폴더별 노트 조회 (최신순)
     */
    @Query("""
        SELECT * FROM notes
        WHERE folder_id = :folderId
        ORDER BY updated_at DESC
    """)
    fun getNotesByFolder(folderId: String): Flow<List<NoteEntity>>

    /**
     * 폴더별 노트 수 조회
     */
    @Query("SELECT COUNT(*) FROM notes WHERE folder_id = :folderId")
    fun getNoteCountByFolder(folderId: String): Flow<Int>

    /**
     * 폴더 이동
     */
    @Query("""
        UPDATE notes
        SET folder_id = :newFolderId, updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun moveToFolder(id: String, newFolderId: String, updatedAt: Long)

    /**
     * 특정 폴더의 모든 노트를 inbox로 이동 (폴더 삭제 시)
     */
    @Query("""
        UPDATE notes
        SET folder_id = 'system-inbox', updated_at = :updatedAt
        WHERE folder_id = :folderId
    """)
    suspend fun moveAllToInbox(folderId: String, updatedAt: Long)

    /**
     * 노트 삭제
     */
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * 캡처 ID로 노트 삭제
     */
    @Query("DELETE FROM notes WHERE capture_id = :captureId")
    suspend fun deleteByCaptureId(captureId: String)

    /**
     * 모든 노트 조회
     */
    @Query("SELECT * FROM notes ORDER BY updated_at DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
}
