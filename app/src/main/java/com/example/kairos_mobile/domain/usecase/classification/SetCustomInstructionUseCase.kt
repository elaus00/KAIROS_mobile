package com.example.kairos_mobile.domain.usecase.classification

import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import javax.inject.Inject

/** 사용자 정의 분류 지시사항 설정 */
class SetCustomInstructionUseCase @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) {
    suspend operator fun invoke(instruction: String) {
        userPreferenceRepository.setString("classification_custom_instruction", instruction)
    }
}
