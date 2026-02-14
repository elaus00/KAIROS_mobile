package com.flit.app.presentation.settings.debug

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CaptureImageExportItem(
    val id: String,
    val path: String,
    val previewUri: String,
    val originalName: String,
    val fileExtension: String,
    val editableName: String,
    val isExporting: Boolean = false
)

data class CaptureImageExportUiState(
    val isLoading: Boolean = false,
    val items: List<CaptureImageExportItem> = emptyList()
)

sealed class CaptureImageExportEvent {
    data class ShowMessage(val message: String) : CaptureImageExportEvent()
}

@HiltViewModel
class CaptureImageExportViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(CaptureImageExportUiState(isLoading = true))
    val uiState: StateFlow<CaptureImageExportUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CaptureImageExportEvent>()
    val events: SharedFlow<CaptureImageExportEvent> = _events.asSharedFlow()

    init {
        loadImages()
    }

    fun reload() {
        loadImages()
    }

    fun updateEditableName(id: String, value: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == id) item.copy(editableName = value) else item
                }
            )
        }
    }

    fun exportImage(id: String) {
        val target = _uiState.value.items.firstOrNull { it.id == id } ?: return
        if (target.isExporting) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(items = state.items.map { item ->
                    if (item.id == id) item.copy(isExporting = true) else item
                })
            }

            val exportResult = runCatching {
                val file = File(target.path)
                if (!file.exists() || !file.isFile) {
                    throw IllegalStateException("원본 파일을 찾을 수 없습니다")
                }

                val sanitizedName = sanitizeFileName(target.editableName)
                    .ifBlank { file.nameWithoutExtension }
                val extension = target.fileExtension.ifBlank { "jpg" }
                val displayName = if (sanitizedName.contains('.')) {
                    sanitizedName
                } else {
                    "$sanitizedName.$extension"
                }

                val mimeType = when (extension.lowercase()) {
                    "png" -> "image/png"
                    "webp" -> "image/webp"
                    else -> "image/jpeg"
                }

                val resolver = appContext.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Flit")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val uri = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: throw IllegalStateException("이미지 저장 URI를 만들 수 없습니다")

                resolver.openOutputStream(uri)?.use { output ->
                    file.inputStream().use { input ->
                        input.copyTo(output)
                    }
                } ?: throw IllegalStateException("출력 스트림을 열 수 없습니다")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }

                "내보내기 완료: $displayName" to true
            }.getOrElse { throwable ->
                "실패: ${throwable.message}" to false
            }

            _uiState.update { state ->
                val updatedItems = if (exportResult.second) {
                    state.items.filterNot { it.id == id }
                } else {
                    state.items.map { item ->
                        if (item.id == id) item.copy(isExporting = false) else item
                    }
                }
                state.copy(items = updatedItems)
            }
            _events.emit(CaptureImageExportEvent.ShowMessage(exportResult.first))
        }
    }

    fun deleteImage(id: String) {
        val target = _uiState.value.items.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            val resultMessage = runCatching {
                val file = File(target.path)
                if (!file.exists() || !file.isFile) {
                    throw IllegalStateException("삭제할 파일을 찾을 수 없습니다")
                }
                if (!file.delete()) {
                    throw IllegalStateException("파일 삭제에 실패했습니다")
                }
                _uiState.update { state ->
                    state.copy(items = state.items.filterNot { it.id == id })
                }
                "삭제 완료: ${target.originalName}"
            }.getOrElse { throwable ->
                "실패: ${throwable.message}"
            }
            _events.emit(CaptureImageExportEvent.ShowMessage(resultMessage))
        }
    }

    private fun loadImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val capturesDir = File(appContext.filesDir, "captures")
            val items = if (capturesDir.exists() && capturesDir.isDirectory) {
                capturesDir.listFiles()
                    ?.filter { file ->
                        file.isFile && file.extension.lowercase() in setOf("jpg", "jpeg", "png", "webp")
                    }
                    ?.sortedByDescending { it.lastModified() }
                    ?.map { file ->
                        CaptureImageExportItem(
                            id = file.absolutePath,
                            path = file.absolutePath,
                            previewUri = Uri.fromFile(file).toString(),
                            originalName = file.name,
                            fileExtension = file.extension.lowercase(),
                            editableName = ""
                        )
                    } ?: emptyList()
            } else {
                emptyList()
            }
            _uiState.update { it.copy(isLoading = false, items = items) }
        }
    }

    private fun sanitizeFileName(input: String): String {
        return input.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }
}
