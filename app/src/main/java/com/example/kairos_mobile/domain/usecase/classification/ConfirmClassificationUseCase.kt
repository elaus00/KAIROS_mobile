package com.example.kairos_mobile.domain.usecase.classification

import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 개별 AI 분류 확인 UseCase
 */
@Singleton
class ConfirmClassificationUseCase @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val trackEventUseCase: TrackEventUseCase
) {
    suspend operator fun invoke(captureId: String) {
        captureRepository.confirmClassification(captureId)

        // 분석 이벤트 추적
        trackEventUseCase(
            eventType = "classification_confirmed",
            eventData = null
        )
    }
}
