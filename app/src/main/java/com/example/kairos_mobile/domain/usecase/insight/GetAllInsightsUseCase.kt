package com.example.kairos_mobile.domain.usecase.insight

import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.repository.InsightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 모든 인사이트 조회 Use Case
 *
 * 비즈니스 로직:
 * - 페이징을 지원하는 전체 인사이트 목록 조회
 * - Archive 화면에서 사용
 */
@Singleton
class GetAllInsightsUseCase @Inject constructor(
    private val repository: InsightRepository
) {
    /**
     * 모든 인사이트 조회
     *
     * @param offset 페이징 시작 위치
     * @param limit 페이지 크기
     * @return 인사이트 리스트 Flow
     */
    operator fun invoke(
        offset: Int = 0,
        limit: Int = 20
    ): Flow<List<Insight>> {
        return repository.getAllInsights(offset, limit)
    }

    /**
     * 날짜별로 그룹화된 인사이트 조회
     *
     * @return 날짜 키와 인사이트 리스트 맵
     */
    fun getInsightsGroupedByDate(): Flow<Map<String, List<Insight>>> {
        return repository.getInsightsGroupedByDate()
    }
}
