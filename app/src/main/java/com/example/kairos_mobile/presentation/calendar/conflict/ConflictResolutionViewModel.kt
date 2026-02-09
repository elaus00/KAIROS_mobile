package com.example.kairos_mobile.presentation.calendar.conflict

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.CalendarConflict
import com.example.kairos_mobile.domain.model.ConflictResolution
import com.example.kairos_mobile.domain.usecase.calendar.DetectCalendarConflictsUseCase
import com.example.kairos_mobile.domain.usecase.calendar.ResolveCalendarConflictUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 충돌 해결 UI 상태 */
data class ConflictResolutionUiState(
    val conflicts: List<CalendarConflict> = emptyList(),
    val isLoading: Boolean = false,
    val resolvingId: String? = null,
    val errorMessage: String? = null
)

/** 캘린더 충돌 해결 ViewModel */
@HiltViewModel
class ConflictResolutionViewModel @Inject constructor(
    private val detectConflictsUseCase: DetectCalendarConflictsUseCase,
    private val resolveConflictUseCase: ResolveCalendarConflictUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConflictResolutionUiState())
    val uiState: StateFlow<ConflictResolutionUiState> = _uiState.asStateFlow()

    init {
        loadConflicts()
    }

    /** 충돌 목록 로드 */
    fun loadConflicts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val conflicts = detectConflictsUseCase()
                _uiState.update { it.copy(conflicts = conflicts, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    /** 충돌 해결 */
    fun resolveConflict(conflict: CalendarConflict, resolution: ConflictResolution) {
        viewModelScope.launch {
            _uiState.update { it.copy(resolvingId = conflict.scheduleId) }
            try {
                resolveConflictUseCase(conflict, resolution)
                _uiState.update { state ->
                    state.copy(
                        conflicts = state.conflicts.filter { it.scheduleId != conflict.scheduleId },
                        resolvingId = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(resolvingId = null, errorMessage = e.message) }
            }
        }
    }

    /** 에러 메시지 닫기 */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
