package com.example.kairos_mobile.domain.usecase.search

import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.SearchQuery
import com.example.kairos_mobile.domain.repository.InsightRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 인사이트 검색 Use Case
 *
 * 비즈니스 로직:
 * - 검색 쿼리 검증
 * - Repository를 통해 인사이트 검색
 * - 페이징 지원
 */
@Singleton
class SearchInsightsUseCase @Inject constructor(
    private val repository: InsightRepository
) {
    /**
     * 인사이트 검색
     *
     * @param query 검색 조건
     * @param offset 페이징 시작 위치
     * @param limit 페이지 크기
     * @return 검색 결과 리스트
     */
    suspend operator fun invoke(
        query: SearchQuery,
        offset: Int = 0,
        limit: Int = 20
    ): Result<List<Insight>> {
        // 페이징 파라미터 검증
        if (offset < 0 || limit <= 0) {
            return Result.Error(IllegalArgumentException("Invalid pagination parameters"))
        }

        return repository.searchInsights(query, offset, limit)
    }
}
