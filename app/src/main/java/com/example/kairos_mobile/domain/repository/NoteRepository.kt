package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.NoteFolder
import com.example.kairos_mobile.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Note Repository 인터페이스 (PRD v4.0)
 */
interface NoteRepository {

    /**
     * 모든 노트 조회 (최신순)
     */
    fun getAllNotes(): Flow<List<Note>>

    /**
     * 폴더별 노트 조회
     */
    fun getNotesByFolder(folder: NoteFolder): Flow<List<Note>>

    /**
     * 노트 검색 (제목, 내용)
     */
    fun searchNotes(query: String): Flow<List<Note>>

    /**
     * 태그로 노트 검색
     */
    fun getNotesByTag(tag: String): Flow<List<Note>>

    /**
     * 최근 노트 조회 (제한된 개수)
     */
    fun getRecentNotes(limit: Int = 10): Flow<List<Note>>

    /**
     * 새 노트 생성
     */
    suspend fun createNote(note: Note): Result<Note>

    /**
     * 캡처에서 노트 생성
     */
    suspend fun createNoteFromCapture(
        captureId: String,
        title: String,
        content: String,
        folder: NoteFolder = NoteFolder.INBOX,
        tags: List<String> = emptyList()
    ): Result<Note>

    /**
     * 노트 업데이트
     */
    suspend fun updateNote(note: Note): Result<Note>

    /**
     * 노트 삭제
     */
    suspend fun deleteNote(id: String): Result<Unit>

    /**
     * ID로 노트 조회
     */
    suspend fun getNoteById(id: String): Result<Note?>

    /**
     * 노트 폴더 변경
     */
    suspend fun moveToFolder(id: String, newFolder: NoteFolder): Result<Unit>

    /**
     * 노트 개수 조회
     */
    fun getNoteCount(): Flow<Int>

    /**
     * 폴더별 노트 개수 조회
     */
    fun getNoteCountByFolder(folder: NoteFolder): Flow<Int>
}
