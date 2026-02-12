package com.flit.app.domain.usecase.classification

import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.settings.PreferenceKeys
import javax.inject.Inject

/** 사용자 정의 분류 지시사항 설정 */
class SetCustomInstructionUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) {
    suspend operator fun invoke(instruction: String) {
        userPreferenceRepository.setString(PreferenceKeys.KEY_CLASSIFICATION_CUSTOM_INSTRUCTION, instruction)
    }
}
