package com.example.kairos_mobile.presentation.trash

import com.example.kairos_mobile.domain.model.Capture

/**
 * 휴지통 화면 UI 상태
 */
data class TrashUiState(
    val items: List<Capture> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
