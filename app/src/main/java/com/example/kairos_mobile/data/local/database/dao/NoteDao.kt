package com.example.kairos_mobile.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.ColumnInfo
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
        SELECT n.* FROM notes n
        INNER JOIN captures c ON c.id = n.capture_id
        WHERE n.folder_id = :folderId
        AND c.is_deleted = 0
        AND c.is_trashed = 0
        ORDER BY n.updated_at DESC
    """)
    fun getNotesByFolder(folderId: String): Flow<List<NoteEntity>>

    /**
     * 폴더별 노트 수 조회
     */
    @Query("""
        SELECT COUNT(*) FROM notes n
        INNER JOIN captures c ON c.id = n.capture_id
        WHERE n.folder_id = :folderId
        AND c.is_deleted = 0
        AND c.is_trashed = 0
    """)
    fun getNoteCountByFolder(folderId: String): Flow<Int>

    /**
     * 전체 폴더별 노트 수 조회
     */
    @Query("""
        SELECT n.folder_id, COUNT(*) AS note_count
        FROM notes n
        INNER JOIN captures c ON c.id = n.capture_id
        WHERE n.folder_id IS NOT NULL
        AND c.is_deleted = 0
        AND c.is_trashed = 0
        GROUP BY n.folder_id
    """)
    fun getFolderNoteCounts(): Flow<List<FolderNoteCountRow>>

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

    /**
     * 폴더별 노트 + 활성 캡처 정보 조회
     */
    @Query("""
        SELECT
            n.id AS note_id,
            n.capture_id AS capture_id,
            c.ai_title AS ai_title,
            c.original_text AS original_text,
            c.created_at AS created_at
        FROM notes n
        INNER JOIN captures c ON c.id = n.capture_id
        WHERE n.folder_id = :folderId
        AND c.is_deleted = 0
        AND c.is_trashed = 0
        ORDER BY n.updated_at DESC
    """)
    fun getNotesWithActiveCaptureByFolder(folderId: String): Flow<List<NoteWithCaptureRow>>
}

data class FolderNoteCountRow(
    @ColumnInfo(name = "folder_id")
    val folderId: String?,
    @ColumnInfo(name = "note_count")
    val noteCount: Int
)

data class NoteWithCaptureRow(
    @ColumnInfo(name = "note_id")
    val noteId: String,
    @ColumnInfo(name = "capture_id")
    val captureId: String,
    @ColumnInfo(name = "ai_title")
    val aiTitle: String?,
    @ColumnInfo(name = "original_text")
    val originalText: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
