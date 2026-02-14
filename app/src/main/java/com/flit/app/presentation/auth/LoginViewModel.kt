package com.flit.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.usecase.auth.LoginWithGoogleUseCase
import com.flit.app.domain.usecase.sync.InitialSyncUseCase
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
 * 로그인 화면 UI 상태
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

sealed class LoginEvent {
    data object LaunchGoogleLogin : LoginEvent()
}

/**
 * 로그인 화면 ViewModel
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val initialSyncUseCase: InitialSyncUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    /** Google 로그인 시작 */
    fun startGoogleLogin() {
        viewModelScope.launch {
            _events.emit(LoginEvent.LaunchGoogleLogin)
        }
    }

    /** Google Sign-In에서 받은 idToken으로 서버 로그인 */
    fun onGoogleIdTokenReceived(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                loginWithGoogleUseCase(idToken)

                // 로그인 성공 직후 초기 동기화 수행
                val syncResult = initialSyncUseCase()
                if (!syncResult.success) {
                    val errorMessage = syncResult.message
                        ?: if (syncResult.skipped) "초기 동기화가 보류되었습니다."
                        else "초기 동기화에 실패했습니다."
                    _uiState.update {
                        it.copy(isLoading = false, error = errorMessage, isLoggedIn = false)
                    }
                    return@launch
                }

                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "로그인 실패")
                }
            }
        }
    }

    fun onGoogleLoginError(message: String) {
        _uiState.update { it.copy(error = message) }
    }
}
