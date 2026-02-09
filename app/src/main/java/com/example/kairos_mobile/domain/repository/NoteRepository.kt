package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.NoteDetail
import com.example.kairos_mobile.domain.model.NoteWithCapturePreview
import kotlinx.coroutines.flow.Flow

/**
 * 노트 Repository 인터페이스
 */
interface NoteRepository {

    /** 노트 생성 */
    suspend fun createNote(note: Note)

    /** 폴더 변경 */
    suspend fun moveToFolder(noteId: String, folderId: String)

    /** capture_id로 삭제 */
    suspend fun deleteByCaptureId(captureId: String)

    /** 폴더별 노트 수 */
    fun getNoteCountByFolderId(folderId: String): Flow<Int>

    /** 전체 폴더별 노트 수 */
    fun getFolderNoteCounts(): Flow<Map<String, Int>>

    /** 삭제되지 않은 캡처만 포함한 폴더별 노트 목록 */
    fun getNotesWithActiveCaptureByFolderId(folderId: String): Flow<List<NoteWithCapturePreview>>

    /** 전체 활성 노트 목록 (삭제/휴지통 제외) */
    fun getAllNotesWithActiveCapture(): Flow<List<NoteWithCapturePreview>>

    /** 노트 상세 조회 (캡처 정보 포함) */
    fun getNoteDetail(noteId: String): Flow<NoteDetail?>

    /** 노트 본문 업데이트 */
    suspend fun updateNoteBody(noteId: String, body: String?)

    /** 폴더 미지정(Inbox) 노트 ID 목록 */
    suspend fun getUngroupedNoteIds(): List<String>
}
