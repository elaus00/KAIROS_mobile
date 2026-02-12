package com.flit.app.presentation.notes.reorganize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.model.ApiException
import com.flit.app.domain.model.FolderType
import com.flit.app.domain.model.NoteAiInput
import com.flit.app.domain.model.ProposedStructure
import com.flit.app.domain.repository.FolderRepository
import com.flit.app.domain.repository.NoteRepository
import com.flit.app.domain.usecase.note.ReorganizeNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 재구성 제안 아이템
 */
data class ProposedItem(
    val folderName: String,
    val folderType: String,
    val action: String?,
    val captureIds: List<String>
)

/**
 * 재구성 화면 UI 상태
 */
data class ReorganizeUiState(
    val isLoading: Boolean = true,
    val isApplying: Boolean = false,
    val proposals: List<ProposedItem> = emptyList(),
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

    // 원본 제안 구조
    private var rawProposals: List<ProposedStructure> = emptyList()

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
                // 전체 노트 + 폴더 조회
                val allNotes = noteRepository.getAllNotesWithActiveCapture().first()
                val noteInputs = allNotes.map { note ->
                    NoteAiInput(
                        captureId = note.captureId,
                        aiTitle = note.aiTitle ?: "",
                        tags = emptyList(),
                        noteSubType = null,
                        folderId = note.folderId
                    )
                }
                val folders = folderRepository.getAllFolders().first()

                val proposals = reorganizeNotesUseCase(noteInputs, folders)
                rawProposals = proposals

                val proposedItems = proposals.map {
                    ProposedItem(
                        folderName = it.folderName,
                        folderType = it.folderType,
                        action = it.action,
                        captureIds = it.captureIds
                    )
                }
                _uiState.update {
                    it.copy(isLoading = false, proposals = proposedItems)
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
     * 제안 적용 (폴더 생성 + 노트 이동)
     */
    fun onApply() {
        viewModelScope.launch {
            _uiState.update { it.copy(isApplying = true) }
            try {
                val allNotes = noteRepository.getAllNotesWithActiveCapture().first()
                val captureToNoteMap = allNotes.associate { it.captureId to it.noteId }

                for (proposal in rawProposals) {
                    val folderType = try {
                        FolderType.valueOf(proposal.folderType.uppercase())
                    } catch (_: Exception) {
                        FolderType.USER
                    }
                    val folder = folderRepository.getOrCreateFolder(proposal.folderName, folderType)
                    for (captureId in proposal.captureIds) {
                        val noteId = captureToNoteMap[captureId] ?: continue
                        noteRepository.moveToFolder(noteId, folder.id)
                    }
                }
                _uiState.update { it.copy(isApplying = false, proposals = emptyList()) }
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
