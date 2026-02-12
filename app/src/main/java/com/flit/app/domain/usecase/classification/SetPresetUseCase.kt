package com.flit.app.domain.usecase.classification

import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.settings.PreferenceKeys
import javax.inject.Inject

/** 분류 프리셋 설정 */
class SetPresetUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) {
    suspend operator fun invoke(presetId: String) {
        userPreferenceRepository.setString(PreferenceKeys.KEY_CLASSIFICATION_PRESET_ID, presetId)
    }
}
