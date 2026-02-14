package com.flit.app.presentation.settings

import android.content.Context
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.model.FontSizePreference
import com.flit.app.domain.model.ThemePreference
import com.flit.app.domain.model.CalendarSyncStatus
import com.flit.app.domain.repository.AuthRepository
import com.flit.app.domain.repository.CalendarRepository
import com.flit.app.domain.repository.ImageRepository
import com.flit.app.domain.repository.ScheduleRepository
import com.flit.app.domain.repository.SubscriptionRepository
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.model.NoteViewType
import com.flit.app.domain.usecase.calendar.SyncScheduleToCalendarUseCase
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
    private val imageRepository: ImageRepository,
    private val scheduleRepository: ScheduleRepository,
    private val getCalendarSettingsUseCase: GetCalendarSettingsUseCase,
    private val setCalendarSettingsUseCase: SetCalendarSettingsUseCase,
    private val syncScheduleToCalendarUseCase: SyncScheduleToCalendarUseCase,
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
        loadFontSizePreference()
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

    /** 앱 글씨 크기 로드 */
    private fun loadFontSizePreference() {
        viewModelScope.launch {
            val size = userPreferenceRepository.getString(
                PreferenceKeys.KEY_CAPTURE_FONT_SIZE,
                FontSizePreference.MEDIUM.name
            )
            _uiState.update { it.copy(fontSizePreference = FontSizePreference.fromString(size)) }
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
            syncExistingSchedulesToCalendar()
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

    /** 앱 글씨 크기 변경 */
    fun setFontSizePreference(size: FontSizePreference) {
        viewModelScope.launch {
            userPreferenceRepository.setString(PreferenceKeys.KEY_CAPTURE_FONT_SIZE, size.name)
            _uiState.update { it.copy(fontSizePreference = size) }
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
            if (enabled) {
                syncExistingSchedulesToCalendar()
            }
        }
    }

    private suspend fun syncExistingSchedulesToCalendar() {
        val targets = scheduleRepository.getAllSchedulesForSync().filter { schedule ->
            schedule.calendarSyncStatus == CalendarSyncStatus.NOT_LINKED ||
                schedule.calendarSyncStatus == CalendarSyncStatus.SYNC_FAILED ||
                schedule.calendarSyncStatus == CalendarSyncStatus.SUGGESTION_PENDING
        }
        targets.forEach { schedule ->
            runCatching { syncScheduleToCalendarUseCase(schedule.id) }
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

    fun dismissImportResult() {
        _uiState.update { it.copy(importResult = null) }
    }

    fun importCaptureImage(sourceUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importResult = null) }
            try {
                imageRepository.saveImage(sourceUri)
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importResult = "이미지 업로드 완료 (캡처 저장소)"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importResult = "실패: ${e.message}"
                    )
                }
            }
        }
    }

    /** 내보내기 결과 메시지 닫기 */
    fun dismissExportResult() {
        _uiState.update { it.copy(exportResult = null) }
    }

    /** 캡처 이미지를 Pictures/Flit/으로 내보내기 */
    fun exportCaptureImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportResult = null) }
            try {
                val capturesDir = java.io.File(appContext.filesDir, "captures")
                if (!capturesDir.exists() || !capturesDir.isDirectory) {
                    _uiState.update {
                        it.copy(isExporting = false, exportResult = "내보낼 이미지가 없습니다")
                    }
                    return@launch
                }

                val imageFiles = capturesDir.listFiles()?.filter {
                    it.isFile && it.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp")
                } ?: emptyList()

                if (imageFiles.isEmpty()) {
                    _uiState.update {
                        it.copy(isExporting = false, exportResult = "내보낼 이미지가 없습니다")
                    }
                    return@launch
                }

                var exportedCount = 0
                val resolver = appContext.contentResolver

                imageFiles.forEach { file ->
                    val mimeType = when (file.extension.lowercase()) {
                        "png" -> "image/png"
                        "webp" -> "image/webp"
                        else -> "image/jpeg"
                    }

                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Flit")
                            put(MediaStore.Images.Media.IS_PENDING, 1)
                        }
                    }

                    val uri = resolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    ) ?: return@forEach

                    resolver.openOutputStream(uri)?.use { output ->
                        file.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(uri, contentValues, null, null)
                    }

                    exportedCount++
                }

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportResult = "${exportedCount}장 내보냄 → Pictures/Flit/"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isExporting = false, exportResult = "실패: ${e.message}")
                }
            }
        }
    }

}
