package com.example.kairos_mobile.domain.usecase.classification

import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 개별 AI 분류 확인 UseCase
 */
@Singleton
class ConfirmClassificationUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    suspend operator fun invoke(captureId: String) {
        captureRepository.confirmClassification(captureId)
    }
}
