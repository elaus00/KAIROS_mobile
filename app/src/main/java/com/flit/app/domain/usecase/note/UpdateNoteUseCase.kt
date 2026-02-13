package com.flit.app.domain.usecase.note

import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.repository.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 노트 업데이트 UseCase
 * 제목, 본문, 폴더 이동을 처리
 */
@Singleton
class UpdateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val captureRepository: CaptureRepository
) {
    /**
     * 노트 제목(AI 제목) 업데이트
     */
    suspend fun updateTitle(captureId: String, title: String) {
        captureRepository.updateAiTitle(captureId, title)
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
