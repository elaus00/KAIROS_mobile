package com.example.kairos_mobile.presentation.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.usecase.capture.EmptyTrashUseCase
import com.example.kairos_mobile.domain.usecase.capture.HardDeleteCaptureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 휴지통 화면 ViewModel
 * 휴지통 항목 조회, 복원, 개별 삭제, 전체 비우기
 */
@HiltViewModel
class TrashViewModel @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val hardDeleteCaptureUseCase: HardDeleteCaptureUseCase,
    private val emptyTrashUseCase: EmptyTrashUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState: StateFlow<TrashUiState> = _uiState.asStateFlow()

    init {
        loadTrashItems()
    }

    /**
     * 휴지통 항목 로드
     */
    private fun loadTrashItems() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            captureRepository.getTrashedItems()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "휴지통을 불러오지 못했습니다."
                        )
                    }
                }
                .collect { items ->
                    _uiState.update {
                        it.copy(
                            items = items,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    /**
     * 휴지통에서 복원
     */
    fun restoreItem(captureId: String) {
        viewModelScope.launch {
            try {
                captureRepository.restoreFromTrash(captureId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "복원에 실패했습니다.")
                }
            }
        }
    }

    /**
     * 개별 항목 완전 삭제
     */
    fun deleteItem(captureId: String) {
        viewModelScope.launch {
            try {
                hardDeleteCaptureUseCase(captureId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "삭제에 실패했습니다.")
                }
            }
        }
    }

    /**
     * 휴지통 비우기 (전체 삭제)
     */
    fun emptyTrash() {
        viewModelScope.launch {
            try {
                emptyTrashUseCase()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "휴지통 비우기에 실패했습니다.")
                }
            }
        }
    }

    /**
     * 에러 메시지 닫기
     */
    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
