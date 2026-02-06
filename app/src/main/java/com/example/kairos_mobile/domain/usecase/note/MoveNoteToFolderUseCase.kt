package com.example.kairos_mobile.domain.usecase.note

import com.example.kairos_mobile.domain.repository.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 노트 폴더 이동 UseCase
 */
@Singleton
class MoveNoteToFolderUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: String, folderId: String) {
        noteRepository.moveToFolder(noteId, folderId)
    }
}
