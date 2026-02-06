package com.example.kairos_mobile.domain.usecase.note

import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 폴더별 노트 조회 UseCase
 */
@Singleton
class GetNotesByFolderUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(folderId: String): Flow<List<Note>> {
        return noteRepository.getNotesByFolderId(folderId)
    }
}
