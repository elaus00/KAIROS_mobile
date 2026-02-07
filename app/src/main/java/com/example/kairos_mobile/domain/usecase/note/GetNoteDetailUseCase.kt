package com.example.kairos_mobile.domain.usecase.note

import com.example.kairos_mobile.domain.model.NoteDetail
import com.example.kairos_mobile.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 노트 상세 조회 UseCase
 */
@Singleton
class GetNoteDetailUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(noteId: String): Flow<NoteDetail?> {
        return noteRepository.getNoteDetail(noteId)
    }
}
