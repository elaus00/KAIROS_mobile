package com.example.kairos_mobile.presentation.detail

import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.NoteSubType

/**
 * 캡처 상세 화면 UI 상태
 */
data class CaptureDetailUiState(
    val isLoading: Boolean = false,
    val captureId: String = "",
    val originalText: String = "",
    val aiTitle: String? = null,
    val classifiedType: ClassifiedType = ClassifiedType.TEMP,
    val noteSubType: NoteSubType? = null,
    val createdAt: Long = 0L,
    val errorMessage: String? = null
)
