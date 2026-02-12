package com.flit.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.model.FontSizePreference
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.settings.PreferenceKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LegalWebViewUiState(
    val captureFontSize: String = FontSizePreference.MEDIUM.name
)

@HiltViewModel
class LegalWebViewViewModel @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LegalWebViewUiState())
    val uiState: StateFlow<LegalWebViewUiState> = _uiState.asStateFlow()

    init {
        loadCaptureFontSize()
    }

    private fun loadCaptureFontSize() {
        viewModelScope.launch {
            val size = userPreferenceRepository.getString(
                PreferenceKeys.KEY_CAPTURE_FONT_SIZE,
                FontSizePreference.MEDIUM.name
            )
            _uiState.update { it.copy(captureFontSize = size) }
        }
    }
}
