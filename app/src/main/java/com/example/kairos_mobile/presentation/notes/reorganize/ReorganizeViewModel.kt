package com.example.kairos_mobile.presentation.notes.reorganize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.ApiException
import com.example.kairos_mobile.domain.repository.FolderRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.usecase.note.ReorganizeNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 이동 제안 아이템
 */
data class MoveItem(
    val noteId: String,
    val noteTitle: String,
    val currentFolder: String,
    val suggestedFolder: String
)

/**
 * 재구성 화면 UI 상태
 */
data class ReorganizeUiState(
    val isLoading: Boolean = true,
    val isApplying: Boolean = false,
    val moves: List<MoveItem> = emptyList(),
    val error: String? = null
)

/**
 * 노트 AI 재구성 ViewModel
 * ReorganizeNotesUseCase로 AI 제안 로드 → 적용
 */
@HiltViewModel
class ReorganizeViewModel @Inject constructor(
    private val reorganizeNotesUseCase: ReorganizeNotesUseCase,
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReorganizeUiState())
    val uiState: StateFlow<ReorganizeUiState> = _uiState.asStateFlow()

    // 원본 이동 데이터 (noteId → 새 folderId)
    private var rawMoves: List<Pair<String, String>> = emptyList()

    init {
        loadReorganization()
    }

    /**
     * AI 재구성 제안 로드
     */
    private fun loadReorganization() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val (moves, _) = reorganizeNotesUseCase()
                rawMoves = moves

                // 이동 목록을 UI용으로 변환 (제목/폴더명 표시)
                val moveItems = moves.map { (noteId, newFolderId) ->
                    MoveItem(
                        noteId = noteId,
                        noteTitle = noteId.take(8), // placeholder
                        currentFolder = "현재 폴더",
                        suggestedFolder = newFolderId
                    )
                }
                _uiState.update {
                    it.copy(isLoading = false, moves = moveItems)
                }
            } catch (e: ApiException.SubscriptionRequired) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Premium 구독이 필요합니다")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "재구성 실패")
                }
            }
        }
    }

    /**
     * 제안 적용 (노트 폴더 이동)
     */
    fun onApply() {
        viewModelScope.launch {
            _uiState.update { it.copy(isApplying = true) }
            try {
                rawMoves.forEach { (noteId, newFolderId) ->
                    noteRepository.moveToFolder(noteId, newFolderId)
                }
                _uiState.update {
                    it.copy(isApplying = false, moves = emptyList())
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isApplying = false, error = e.message ?: "적용 실패")
                }
            }
        }
    }

    /**
     * 다시 시도
     */
    fun onRetry() {
        loadReorganization()
    }
}
