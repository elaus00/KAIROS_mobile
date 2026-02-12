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
     * 캡처 ID로 노트 삭제
     */
    @Query("DELETE FROM notes WHERE capture_id = :captureId")
    suspend fun deleteByCaptureId(captureId: String)

    /**
     * 폴더별 노트 + 활성 캡처 정보 조회
     */
    @Query("""
        SELECT
            n.id AS note_id,
            n.capture_id AS capture_id,
            c.ai_title AS ai_title,
            c.original_text AS original_text,
            c.created_at AS created_at,
            n.body AS body,
            n.folder_id AS folder_id,
            c.note_sub_type AS note_sub_type
        FROM notes n
        INNER JOIN captures c ON c.id = n.capture_id
        WHERE n.folder_id = :folderId
        AND c.is_deleted = 0
        AND c.is_trashed = 0
        ORDER BY n.updated_at DESC
    """)
    fun getNotesWithActiveCaptureByFolder(folderId: String): Flow<List<NoteWithCaptureRow>>

    /**
     * 전체 노트 + 활성 캡처 정보 조회 (삭제/휴지통 제외, 최신순)
     */
    @Query("""
        SELECT
            n.id AS note_id,
            n.capture_id AS capture_id,
            c.ai_title AS ai_title,
            c.original_text AS original_text,
            c.created_at AS created_at,
            n.body AS body,
            n.folder_id AS folder_id,
            c.note_sub_type AS note_sub_type
        FROM notes n
        INNER JOIN captures c ON c.id = n.capture_id
        WHERE c.is_deleted = 0
        AND c.is_trashed = 0
        ORDER BY n.updated_at DESC
    """)
    fun getAllNotesWithActiveCapture(): Flow<List<NoteWithCaptureRow>>

    /**
     * 노트 본문 업데이트
     */
    @Query("UPDATE notes SET body = :body, updated_at = :updatedAt WHERE id = :noteId")
    suspend fun updateBody(noteId: String, body: String?, updatedAt: Long)

    /**
     * 폴더 미지정(Inbox) 활성 노트 ID 목록 조회 (AI 그룹화 대상)
     */
    @Query("""
        SELECT n.id FROM notes n
        INNER JOIN captures c ON c.id = n.capture_id
        WHERE (n.folder_id IS NULL OR n.folder_id = 'system-inbox')
        AND c.is_deleted = 0
        AND c.is_trashed = 0
    """)
    suspend fun getUngroupedNoteIds(): List<String>

    /**
     * 노트 상세 조회 (캡처 정보 포함)
     */
    @Query("""
        SELECT
            n.id AS note_id,
            n.capture_id AS capture_id,
            c.ai_title AS ai_title,
            c.original_text AS original_text,
            n.body AS body,
            c.classified_type AS classified_type,
            c.note_sub_type AS note_sub_type,
            n.folder_id AS folder_id,
            c.image_uri AS image_uri,
            n.created_at AS created_at,
            n.updated_at AS updated_at
        FROM notes n
        INNER JOIN captures c ON c.id = n.capture_id
        WHERE n.id = :noteId
    """)
    fun getNoteWithCapture(noteId: String): Flow<NoteDetailRow?>

    /** 동기화용 전체 노트 조회 */
    @Query("SELECT * FROM notes")
    suspend fun getAllForSync(): List<NoteEntity>
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
    val createdAt: Long,
    @ColumnInfo(name = "body")
    val body: String? = null,
    @ColumnInfo(name = "folder_id")
    val folderId: String? = null,
    @ColumnInfo(name = "note_sub_type")
    val noteSubType: String? = null
)

/**
 * 노트 상세 조회용 Row (캡처 정보 포함)
 */
data class NoteDetailRow(
    @ColumnInfo(name = "note_id")
    val noteId: String,
    @ColumnInfo(name = "capture_id")
    val captureId: String,
    @ColumnInfo(name = "ai_title")
    val aiTitle: String?,
    @ColumnInfo(name = "original_text")
    val originalText: String,
    @ColumnInfo(name = "body")
    val body: String?,
    @ColumnInfo(name = "classified_type")
    val classifiedType: String,
    @ColumnInfo(name = "note_sub_type")
    val noteSubType: String?,
    @ColumnInfo(name = "folder_id")
    val folderId: String?,
    @ColumnInfo(name = "image_uri")
    val imageUri: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
