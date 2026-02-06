package com.example.kairos_mobile.domain.usecase.classification

import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 전체 미확인 분류 일괄 확인 UseCase
 */
@Singleton
class ConfirmAllClassificationsUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    suspend operator fun invoke() {
        captureRepository.confirmAllClassifications()
    }
}
