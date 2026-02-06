package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * 노트 Repository 인터페이스
 */
interface NoteRepository {

    /** 노트 생성 */
    suspend fun createNote(note: Note)

    /** 노트 조회 (capture_id 기준) */
    suspend fun getNoteByCaptureId(captureId: String): Note?

    /** 폴더별 노트 목록 */
    fun getNotesByFolderId(folderId: String): Flow<List<Note>>

    /** 폴더 변경 */
    suspend fun moveToFolder(noteId: String, folderId: String)

    /** capture_id로 삭제 */
    suspend fun deleteByCaptureId(captureId: String)

    /** 폴더별 노트 수 */
    fun getNoteCountByFolderId(folderId: String): Flow<Int>
}
