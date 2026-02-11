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
        // 분류 확인 전 캡처 정보 조회 (메트릭 데이터용)
        val capture = captureRepository.getCaptureById(captureId)

        captureRepository.confirmClassification(captureId)

        // 분석 이벤트 추적 — capture_id, confirmed_type, changed 포함
        trackEventUseCase(
            eventType = "classification_confirmed",
            eventData = """{"capture_id":"$captureId","confirmed_type":"${capture?.classifiedType?.name ?: "UNKNOWN"}","changed":false}"""
        )
    }
}
