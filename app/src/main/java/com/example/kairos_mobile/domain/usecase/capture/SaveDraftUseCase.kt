package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 임시 저장 UseCase
 * QuickCapture 축소 시 현재 텍스트를 저장
 */
@Singleton
class SaveDraftUseCase @Inject constructor(
    private val preferenceRepository: UserPreferenceRepository
) {
    companion object {
        internal const val KEY_DRAFT_TEXT = "draft_text"
    }

    suspend operator fun invoke(text: String) {
        preferenceRepository.setString(KEY_DRAFT_TEXT, text)
    }
}
