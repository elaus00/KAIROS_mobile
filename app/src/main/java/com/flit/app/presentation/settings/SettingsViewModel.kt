package com.flit.app.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.model.FontSizePreference
import com.flit.app.domain.model.ThemePreference
import com.flit.app.domain.repository.AuthRepository
import com.flit.app.domain.repository.CalendarRepository
import com.flit.app.domain.repository.ImageRepository
import com.flit.app.domain.repository.SubscriptionRepository
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.model.NoteViewType
import com.flit.app.domain.usecase.capture.SubmitCaptureUseCase
import com.flit.app.domain.usecase.settings.GetCalendarSettingsUseCase
import com.flit.app.domain.usecase.settings.PreferenceKeys
import com.flit.app.domain.usecase.settings.SetCalendarSettingsUseCase
import com.flit.app.presentation.widget.WidgetUpdateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Settings 화면 ViewModel
 * 테마/캘린더 토글/계정/구독 설정 관리
 * 캘린더 세부 설정과 AI 분류 설정은 각각의 세부 화면으로 분리됨
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val calendarRepository: CalendarRepository,
    private val getCalendarSettingsUseCase: GetCalendarSettingsUseCase,
    private val setCalendarSettingsUseCase: SetCalendarSettingsUseCase,
    private val submitCaptureUseCase: SubmitCaptureUseCase,
    private val imageRepository: ImageRepository,
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
        loadCalendarSettings()
        refreshCalendarPermissionState()
        loadAccountInfo()
        loadCaptureFontSize()
        loadNoteViewType()
    }

    /** 설정 값 로드 */
    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferenceRepository.getThemePreference().collect { theme ->
                _uiState.update { it.copy(themePreference = theme) }
            }
        }
    }

    /** 캘린더 설정 로드 */
    private fun loadCalendarSettings() {
        viewModelScope.launch {
            val calendarEnabled = getCalendarSettingsUseCase.isCalendarEnabled()
            _uiState.update {
                it.copy(isCalendarEnabled = calendarEnabled)
            }
        }
    }

    /** 계정 정보 로드 */
    private fun loadAccountInfo() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val tier = subscriptionRepository.getCachedTier()
            val features = subscriptionRepository.getCachedFeatures()
            _uiState.update {
                it.copy(user = user, subscriptionTier = tier, features = features)
            }
        }
    }

    /** 캡처 글씨 크기 로드 */
    private fun loadCaptureFontSize() {
        viewModelScope.launch {
            val size = userPreferenceRepository.getString(PreferenceKeys.KEY_CAPTURE_FONT_SIZE, FontSizePreference.MEDIUM.name)
            _uiState.update { it.copy(captureFontSize = size) }
        }
    }

    /** 캘린더 권한 상태 새로고침 */
    fun refreshCalendarPermissionState() {
        viewModelScope.launch {
            val granted = calendarRepository.isCalendarPermissionGranted()
            if (!granted) {
                if (_uiState.value.isCalendarEnabled) {
                    setCalendarSettingsUseCase.setCalendarEnabled(false)
                }
                _uiState.update {
                    it.copy(
                        isCalendarPermissionGranted = false,
                        isCalendarEnabled = false
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isCalendarPermissionGranted = true) }
        }
    }

    /** 권한 요청 결과 처리 */
    fun onCalendarPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (!granted) {
                setCalendarSettingsUseCase.setCalendarEnabled(false)
                _uiState.update {
                    it.copy(
                        isCalendarPermissionGranted = false,
                        isCalendarEnabled = false,
                        calendarAuthMessage = "캘린더 권한이 거부되었습니다."
                    )
                }
                return@launch
            }

            setCalendarSettingsUseCase.setCalendarEnabled(true)
            _uiState.update {
                it.copy(
                    isCalendarPermissionGranted = true,
                    isCalendarEnabled = true,
                    calendarAuthMessage = "캘린더 권한이 허용되었습니다."
                )
            }
        }
    }

    /** 노트 보기 유형 로드 */
    private fun loadNoteViewType() {
        viewModelScope.launch {
            val type = userPreferenceRepository.getString(
                PreferenceKeys.KEY_NOTE_VIEW_TYPE,
                NoteViewType.LIST.name
            )
            _uiState.update { it.copy(noteViewType = type) }
        }
    }

    /** 노트 보기 유형 변경 */
    fun setNoteViewType(type: String) {
        viewModelScope.launch {
            userPreferenceRepository.setString(PreferenceKeys.KEY_NOTE_VIEW_TYPE, type)
            _uiState.update { it.copy(noteViewType = type) }
        }
    }

    /** 캡처 글씨 크기 변경 */
    fun setCaptureFontSize(size: String) {
        viewModelScope.launch {
            userPreferenceRepository.setString(PreferenceKeys.KEY_CAPTURE_FONT_SIZE, size)
            _uiState.update { it.copy(captureFontSize = size) }
        }
    }

    /** 로그아웃 */
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _uiState.update {
                    it.copy(user = null)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    /** 테마 변경 (LIGHT / DARK / SYSTEM) */
    fun setTheme(theme: ThemePreference) {
        viewModelScope.launch {
            userPreferenceRepository.setThemePreference(theme)
            _uiState.update { it.copy(themePreference = theme) }
            WidgetUpdateHelper.updateAllWidgets(appContext)
        }
    }

    /** 캘린더 연동 토글 */
    fun toggleCalendar(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !calendarRepository.isCalendarPermissionGranted()) {
                _uiState.update {
                    it.copy(
                        isCalendarEnabled = false,
                        calendarAuthMessage = "캘린더 권한이 필요합니다."
                    )
                }
                return@launch
            }

            setCalendarSettingsUseCase.setCalendarEnabled(enabled)
            _uiState.update {
                it.copy(
                    isCalendarEnabled = enabled,
                    isCalendarPermissionGranted = calendarRepository.isCalendarPermissionGranted()
                )
            }
        }
    }

    /** 에러 메시지 닫기 */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissCalendarAuthMessage() {
        _uiState.update { it.copy(calendarAuthMessage = null) }
    }

    fun showCalendarAuthMessage(message: String) {
        _uiState.update { it.copy(calendarAuthMessage = message) }
    }

    /** 디버그: 이미지 URI로 캡처 제출 */
    fun debugSubmitImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(debugSubmitting = true, debugResult = null) }
            try {
                val savedUri = imageRepository.saveImage(uri)
                val capture = submitCaptureUseCase(
                    text = "[디버그] 이미지 캡처 테스트",
                    imageUri = savedUri
                )
                _uiState.update {
                    it.copy(
                        debugSubmitting = false,
                        debugResult = "캡처 생성됨: ${capture.id.take(8)}..."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        debugSubmitting = false,
                        debugResult = "실패: ${e.message}"
                    )
                }
            }
        }
    }

    /** 디버그 결과 메시지 닫기 */
    fun dismissDebugResult() {
        _uiState.update { it.copy(debugResult = null) }
    }
}
