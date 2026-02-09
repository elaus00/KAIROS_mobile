package com.example.kairos_mobile.domain.usecase.classification

import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import javax.inject.Inject

/** 분류 프리셋 설정 */
class SetPresetUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) {
    suspend operator fun invoke(presetId: String) {
        userPreferenceRepository.setString("classification_preset_id", presetId)
    }
}
