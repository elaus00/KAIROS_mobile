package com.flit.app.presentation.classification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.model.Capture
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.NoteSubType
import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.usecase.classification.ChangeClassificationUseCase
import com.flit.app.domain.usecase.classification.ConfirmClassificationUseCase
import com.flit.app.domain.usecase.classification.GetUnconfirmedClassificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AI Status Sheet ViewModel
 * 미확인 분류 목록 관리 + 확인/변경 처리
 */
@HiltViewModel
class AIStatusSheetViewModel @Inject constructor(
    private val getUnconfirmedClassificationsUseCase: GetUnconfirmedClassificationsUseCase,
    private val confirmClassificationUseCase: ConfirmClassificationUseCase,
    private val captureRepository: CaptureRepository,
    private val changeClassificationUseCase: ChangeClassificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIStatusSheetUiState())
    val uiState: StateFlow<AIStatusSheetUiState> = _uiState.asStateFlow()

    init {
        observeUnconfirmedClassifications()
    }

    /**
     * 미확인 분류 목록 실시간 관찰
     */
    private fun observeUnconfirmedClassifications() {
        viewModelScope.launch {
            getUnconfirmedClassificationsUseCase().collect { captures ->
                _uiState.update {
                    it.copy(
                        unconfirmedCaptures = captures,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * 개별 분류 확인
     */
    fun confirmClassification(captureId: String) {
        viewModelScope.launch {
            try {
                confirmClassificationUseCase(captureId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "확인에 실패했습니다.")
                }
            }
        }
    }

    /**
     * 전체 미확인 분류 일괄 확인
     */
    fun confirmAll() {
        viewModelScope.launch {
            try {
                captureRepository.confirmAllClassifications()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "일괄 확인에 실패했습니다.")
                }
            }
        }
    }

    /**
     * 분류 유형 변경
     */
    fun changeClassification(
        captureId: String,
        newType: ClassifiedType,
        newSubType: NoteSubType?
    ) {
        viewModelScope.launch {
            try {
                changeClassificationUseCase(captureId, newType, newSubType)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "분류 변경에 실패했습니다.")
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

/**
 * AI Status Sheet UI 상태
 */
data class AIStatusSheetUiState(
    /** 미확인 분류 목록 */
    val unconfirmedCaptures: List<Capture> = emptyList(),
    /** 로딩 중 여부 */
    val isLoading: Boolean = true,
    /** 에러 메시지 */
    val errorMessage: String? = null
)
