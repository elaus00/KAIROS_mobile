package com.example.kairos_mobile.domain.usecase.note

import com.example.kairos_mobile.data.local.database.dao.CaptureDao
import com.example.kairos_mobile.domain.repository.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 노트 업데이트 UseCase
 * 제목, 본문, 폴더 이동을 처리
 */
@Singleton
class UpdateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val captureDao: CaptureDao
) {
    /**
     * 노트 제목(AI 제목) 업데이트
     */
    suspend fun updateTitle(captureId: String, title: String) {
        captureDao.updateAiTitle(captureId, title, System.currentTimeMillis())
    }

    /**
     * 노트 본문 업데이트
     */
    suspend fun updateBody(noteId: String, body: String?) {
        noteRepository.updateNoteBody(noteId, body)
    }

    /**
     * 노트 폴더 이동
     */
    suspend fun moveToFolder(noteId: String, folderId: String) {
        noteRepository.moveToFolder(noteId, folderId)
    }
}
