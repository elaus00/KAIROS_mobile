package com.example.kairos_mobile.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.PreferencesRepository
import com.example.kairos_mobile.domain.usecase.ConnectGoogleCalendarUseCase
import com.example.kairos_mobile.domain.usecase.ConnectTodoistUseCase
import com.example.kairos_mobile.domain.usecase.GetSyncStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Settings 화면 ViewModel
 * Phase 3: 외부 서비스 연동 및 AI 기능 설정 관리
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val connectGoogleCalendarUseCase: ConnectGoogleCalendarUseCase,
    private val connectTodoistUseCase: ConnectTodoistUseCase,
    private val getSyncStatusUseCase: GetSyncStatusUseCase,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        loadSyncStatus()
        loadPreferences()
    }

    // ========== 동기화 상태 로드 ==========

    /**
     * 외부 서비스 동기화 상태 로드
     */
    private fun loadSyncStatus() {
        viewModelScope.launch {
            // Google Calendar 상태
            when (val result = getSyncStatusUseCase.getGoogleSyncStatus()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            googleCalendarConnected = result.data.isConnected,
                            googleLastSyncTime = result.data.lastSyncTime,
                            googleSyncedCount = result.data.syncedCount
                        )
                    }
                }
                is Result.Error -> {
                    // 연결 실패 시 미연결 상태로 표시
                    _uiState.update {
                        it.copy(googleCalendarConnected = false)
                    }
                }
                is Result.Loading -> { /* ignore */ }
            }
        }

        viewModelScope.launch {
            // Todoist 상태
            when (val result = getSyncStatusUseCase.getTodoistSyncStatus()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            todoistConnected = result.data.isConnected,
                            todoistLastSyncTime = result.data.lastSyncTime,
                            todoistSyncedCount = result.data.syncedCount
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(todoistConnected = false)
                    }
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    /**
     * 설정 값 로드
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.getAutoSummarizeEnabled().collect { enabled ->
                _uiState.update { it.copy(autoSummarizeEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            preferencesRepository.getSmartTagsEnabled().collect { enabled ->
                _uiState.update { it.copy(smartTagsEnabled = enabled) }
            }
        }
    }

    // ========== M11: Google Calendar 연동 ==========

    /**
     * Google Calendar 연동 시작
     * OAuth URL을 가져와 Chrome Custom Tab 열기 요청
     */
    fun connectGoogleCalendar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true) }

            when (val result = connectGoogleCalendarUseCase.getAuthUrl()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            pendingOAuthUrl = result.data
                        )
                    }
                    _events.emit(SettingsEvent.OpenOAuthUrl(result.data))
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                    _events.emit(SettingsEvent.ShowError(
                        result.exception.message ?: "Google 연동 실패"
                    ))
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    /**
     * Google OAuth 콜백 처리
     */
    fun handleGoogleCallback(code: String, state: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true) }

            when (val result = connectGoogleCalendarUseCase.handleCallback(code, state)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            googleCalendarConnected = true,
                            pendingOAuthUrl = null
                        )
                    }
                    _events.emit(SettingsEvent.ShowSuccess("Google Calendar 연동 완료"))
                    // 동기화 상태 새로고침
                    loadSyncStatus()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            errorMessage = result.exception.message,
                            pendingOAuthUrl = null
                        )
                    }
                    _events.emit(SettingsEvent.ShowError(
                        result.exception.message ?: "Google 인증 실패"
                    ))
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    /**
     * Google Calendar 수동 동기화
     */
    fun syncGoogleCalendar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true) }

            when (val result = connectGoogleCalendarUseCase.sync()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            googleLastSyncTime = System.currentTimeMillis(),
                            googleSyncedCount = result.data.syncedCount
                        )
                    }
                    _events.emit(SettingsEvent.ShowSuccess(
                        "${result.data.syncedCount}개 항목 동기화됨"
                    ))
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                    _events.emit(SettingsEvent.ShowError(
                        result.exception.message ?: "동기화 실패"
                    ))
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    /**
     * Google Calendar 연동 해제
     */
    fun disconnectGoogleCalendar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true) }

            when (val result = connectGoogleCalendarUseCase.disconnect()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            googleCalendarConnected = false,
                            googleLastSyncTime = null,
                            googleSyncedCount = 0
                        )
                    }
                    _events.emit(SettingsEvent.ShowSuccess("Google Calendar 연동 해제됨"))
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                    _events.emit(SettingsEvent.ShowError(
                        result.exception.message ?: "연동 해제 실패"
                    ))
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    // ========== M12: Todoist 연동 ==========

    /**
     * Todoist 연동 시작
     */
    fun connectTodoist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTodoistLoading = true) }

            when (val result = connectTodoistUseCase.getAuthUrl()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isTodoistLoading = false,
                            pendingOAuthUrl = result.data
                        )
                    }
                    _events.emit(SettingsEvent.OpenOAuthUrl(result.data))
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isTodoistLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                    _events.emit(SettingsEvent.ShowError(
                        result.exception.message ?: "Todoist 연동 실패"
                    ))
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    /**
     * Todoist OAuth 콜백 처리
     */
    fun handleTodoistCallback(code: String, state: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTodoistLoading = true) }

            when (val result = connectTodoistUseCase.handleCallback(code, state)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isTodoistLoading = false,
                            todoistConnected = true,
                            pendingOAuthUrl = null
                        )
                    }
                    _events.emit(SettingsEvent.ShowSuccess("Todoist 연동 완료"))
                    loadSyncStatus()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isTodoistLoading = false,
                            errorMessage = result.exception.message,
                            pendingOAuthUrl = null
                        )
                    }
                    _events.emit(SettingsEvent.ShowError(
                        result.exception.message ?: "Todoist 인증 실패"
                    ))
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    /**
     * Todoist 수동 동기화
     */
    fun syncTodoist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTodoistLoading = true) }

            when (val result = connectTodoistUseCase.sync()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isTodoistLoading = false,
                            todoistLastSyncTime = System.currentTimeMillis(),
                            todoistSyncedCount = result.data.syncedCount
                        )
                    }
                    _events.emit(SettingsEvent.ShowSuccess(
                        "${result.data.syncedCount}개 항목 동기화됨"
                    ))
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isTodoistLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                    _events.emit(SettingsEvent.ShowError(
                        result.exception.message ?: "동기화 실패"
                    ))
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    /**
     * Todoist 연동 해제
     */
    fun disconnectTodoist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTodoistLoading = true) }

            when (val result = connectTodoistUseCase.disconnect()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isTodoistLoading = false,
                            todoistConnected = false,
                            todoistLastSyncTime = null,
                            todoistSyncedCount = 0
                        )
                    }
                    _events.emit(SettingsEvent.ShowSuccess("Todoist 연동 해제됨"))
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isTodoistLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                    _events.emit(SettingsEvent.ShowError(
                        result.exception.message ?: "연동 해제 실패"
                    ))
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    // ========== AI 기능 설정 ==========

    /**
     * 자동 요약 활성화 토글
     */
    fun toggleAutoSummarize(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoSummarizeEnabled(enabled)
            _uiState.update { it.copy(autoSummarizeEnabled = enabled) }
        }
    }

    /**
     * 스마트 태그 제안 활성화 토글
     */
    fun toggleSmartTags(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSmartTagsEnabled(enabled)
            _uiState.update { it.copy(smartTagsEnabled = enabled) }
        }
    }

    // ========== UI 이벤트 처리 ==========

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 성공 메시지 닫기
     */
    fun onSuccessDismissed() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
