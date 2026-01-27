package com.example.kairos_mobile.domain.usecase.insight

import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.repository.InsightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 대기중인 인사이트 조회 Use Case
 *
 * 비즈니스 로직:
 * - 오프라인 큐에서 동기화 대기중인 인사이트 목록 조회
 * - UI에서 오프라인 상태 표시용
 */
@Singleton
class GetPendingInsightsUseCase @Inject constructor(
    private val repository: InsightRepository
) {
    /**
     * 대기중인 인사이트 목록 Flow 반환
     */
    operator fun invoke(): Flow<List<Insight>> {
        return repository.getPendingInsights()
    }
}
