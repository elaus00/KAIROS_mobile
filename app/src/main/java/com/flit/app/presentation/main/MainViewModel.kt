package com.flit.app.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.repository.AuthRepository
import com.flit.app.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isSyncing: Boolean = false
)

sealed class MainUiEvent {
    data class ShowMessage(val message: String) : MainUiEvent()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MainUiEvent>()
    val events: SharedFlow<MainUiEvent> = _events.asSharedFlow()

    fun retryServerSync() {
        if (_uiState.value.isSyncing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val message = try {
                performSync()
            } catch (e: Exception) {
                e.message ?: "동기화 재시도 중 오류가 발생했습니다."
            }
            _events.emit(MainUiEvent.ShowMessage(message))
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    /** push → pull 순서로 동기화 수행, 실패 시 에러 메시지 반환 */
    private suspend fun performSync(): String {
        val user = authRepository.getCurrentUser()
            ?: return "로그인 후 동기화를 다시 시도해주세요."

        val pushResult = syncRepository.pushLocalData(user.id)
        if (!pushResult.success) {
            return pushResult.message ?: "로컬 데이터 업로드 동기화에 실패했습니다."
        }

        val pullResult = syncRepository.pullServerData(user.id)
        if (!pullResult.success) {
            return pullResult.message ?: "서버 데이터 다운로드 동기화에 실패했습니다."
        }

        return "동기화 완료: 업로드 ${pushResult.pushedCount}건, 다운로드 ${pullResult.pulledCount}건"
    }
}
