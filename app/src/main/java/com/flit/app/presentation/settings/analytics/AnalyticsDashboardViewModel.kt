package com.flit.app.presentation.settings.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.model.AnalyticsDashboard
import com.flit.app.domain.repository.NoteAiRepository
import com.flit.app.domain.usecase.subscription.CheckFeatureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 분석 대시보드 UI 상태 */
data class AnalyticsDashboardUiState(
    val dashboard: AnalyticsDashboard? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/** 분석 대시보드 ViewModel */
@HiltViewModel
class AnalyticsDashboardViewModel @Inject constructor(
    private val noteAiRepository: NoteAiRepository,
    private val checkFeatureUseCase: CheckFeatureUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsDashboardUiState())
    val uiState: StateFlow<AnalyticsDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    /** 대시보드 데이터 로드 */
    fun loadDashboard() {
        viewModelScope.launch {
            if (!checkFeatureUseCase("analytics_dashboard")) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Premium 구독에서만 사용 가능합니다."
                    )
                }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val dashboard = noteAiRepository.getDashboard()
                _uiState.update { it.copy(dashboard = dashboard, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    /** 에러 메시지 닫기 */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
