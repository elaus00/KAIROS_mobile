package com.example.kairos_mobile.presentation.notes.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.NoteFolder
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * NoteEditScreen ViewModel (PRD v4.0)
 */
@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: String? = savedStateHandle.get<String>("noteId")?.takeIf { it != "new" }

    private val _uiState = MutableStateFlow(NoteEditUiState(noteId = noteId))
    val uiState: StateFlow<NoteEditUiState> = _uiState.asStateFlow()

    init {
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    /**
     * 이벤트 처리
     */
    fun onEvent(event: NoteEditEvent) {
        when (event) {
            is NoteEditEvent.UpdateTitle -> updateTitle(event.title)
            is NoteEditEvent.UpdateContent -> updateContent(event.content)
            is NoteEditEvent.UpdateFolder -> updateFolder(event.folder)
            is NoteEditEvent.UpdateNewTagInput -> updateNewTagInput(event.input)
            is NoteEditEvent.AddTag -> addTag()
            is NoteEditEvent.RemoveTag -> removeTag(event.tag)
            is NoteEditEvent.Save -> save()
            is NoteEditEvent.Delete -> delete()
            is NoteEditEvent.DismissError -> dismissError()
        }
    }

    /**
     * 노트 로드
     */
    private fun loadNote(noteId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = noteRepository.getNoteById(noteId)) {
                is Result.Success -> {
                    result.data?.let { note ->
                        _uiState.update {
                            it.copy(
                                noteId = note.id,
                                title = note.title,
                                content = note.content,
                                folder = note.folder,
                                tags = note.tags,
                                isLoading = false
                            )
                        }
                    } ?: run {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "노트를 찾을 수 없습니다"
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "노트 로드 실패"
                        )
                    }
                }
                is Result.Loading -> { /* 무시 */ }
            }
        }
    }

    private fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    private fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    private fun updateFolder(folder: NoteFolder) {
        _uiState.update { it.copy(folder = folder) }
    }

    private fun updateNewTagInput(input: String) {
        _uiState.update { it.copy(newTagInput = input) }
    }

    private fun addTag() {
        val newTag = _uiState.value.newTagInput.trim()
            .removePrefix("#")
            .lowercase()

        if (newTag.isNotEmpty() && !_uiState.value.tags.contains(newTag)) {
            _uiState.update {
                it.copy(
                    tags = it.tags + newTag,
                    newTagInput = ""
                )
            }
        }
    }

    private fun removeTag(tag: String) {
        _uiState.update {
            it.copy(tags = it.tags.filter { t -> t != tag })
        }
    }

    /**
     * 노트 저장
     */
    private fun save() {
        val state = _uiState.value
        if (!state.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val now = Instant.now()
            val note = Note(
                id = state.noteId ?: UUID.randomUUID().toString(),
                title = state.title,
                content = state.content,
                folder = state.folder,
                tags = state.tags,
                createdAt = now,
                updatedAt = now
            )

            val result = if (state.isNewNote) {
                noteRepository.createNote(note)
            } else {
                noteRepository.updateNote(note)
            }

            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.exception.message ?: "저장 실패"
                        )
                    }
                }
                is Result.Loading -> { /* 무시 */ }
            }
        }
    }

    /**
     * 노트 삭제
     */
    private fun delete() {
        val noteId = _uiState.value.noteId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            when (val result = noteRepository.deleteNote(noteId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.exception.message ?: "삭제 실패"
                        )
                    }
                }
                is Result.Loading -> { /* 무시 */ }
            }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
