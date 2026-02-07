package com.example.kairos_mobile.domain.usecase.analytics

import com.example.kairos_mobile.domain.model.AnalyticsEvent
import com.example.kairos_mobile.domain.repository.AnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 분석 이벤트 추적 UseCase
 * 사용자 행동을 기록하여 분석 데이터 수집
 */
@Singleton
class TrackEventUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(eventType: String, eventData: String? = null) {
        val event = AnalyticsEvent(
            eventType = eventType,
            eventData = eventData
        )
        analyticsRepository.insert(event)
    }
}
