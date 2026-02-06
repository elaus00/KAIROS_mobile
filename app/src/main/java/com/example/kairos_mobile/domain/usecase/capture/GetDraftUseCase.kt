package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 임시 저장 텍스트 조회 UseCase
 * QuickCapture 복귀 시 저장된 텍스트 로드
 */
@Singleton
class GetDraftUseCase @Inject constructor(
    private val preferenceRepository: UserPreferenceRepository
) {
    suspend operator fun invoke(): String {
        return preferenceRepository.getString(SaveDraftUseCase.KEY_DRAFT_TEXT, "")
    }
}
