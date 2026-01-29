package com.example.kairos_mobile.presentation.result

import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.ConfidenceLevel
import com.example.kairos_mobile.domain.model.CaptureType

/**
 * ResultScreen UI 상태
 */
data class ResultUiState(
    // 캡처 정보
    val captureId: String = "",
    val content: String = "",
    val classification: Classification? = null,
    val confidenceLevel: ConfidenceLevel = ConfidenceLevel.LOW,

    // 자동저장 모드 (95%+)
    val autoSaveProgress: Float = 0f,
    val isAutoSaveActive: Boolean = false,
    val autoSaveCountdown: Int = 3,  // 초

    // 수정 모드
    val isEditMode: Boolean = false,
    val editedType: CaptureType? = null,
    val editedTitle: String = "",
    val editedTags: List<String> = emptyList(),

    // 저장 상태
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,

    // 로딩 및 에러
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    /**
     * 현재 선택된 타입 (수정 모드에서는 수정된 값, 아니면 분류된 값)
     */
    val currentType: CaptureType?
        get() = editedType ?: classification?.type

    /**
     * 현재 제목 (수정 모드에서는 수정된 값, 아니면 분류된 값)
     */
    val currentTitle: String
        get() = editedTitle.takeIf { it.isNotBlank() }
            ?: classification?.title
            ?: ""

    /**
     * 현재 태그 (수정 모드에서는 수정된 값, 아니면 분류된 값)
     */
    val currentTags: List<String>
        get() = editedTags.takeIf { it.isNotEmpty() }
            ?: classification?.tags
            ?: emptyList()

    /**
     * 신뢰도 퍼센트 표시용
     */
    val confidencePercent: Int
        get() = ((classification?.confidence ?: 0f) * 100).toInt()
}
