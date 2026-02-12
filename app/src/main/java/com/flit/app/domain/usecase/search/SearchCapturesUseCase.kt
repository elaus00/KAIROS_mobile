package com.flit.app.domain.usecase.search

import com.flit.app.domain.model.Capture
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.repository.CaptureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캡처 FTS 검색 UseCase
 */
@Singleton
class SearchCapturesUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    /**
     * 기본 검색 (기존 호환)
     */
    operator fun invoke(query: String): Flow<List<Capture>> {
        if (query.isBlank()) return emptyFlow()
        return captureRepository.searchCaptures(query)
    }

    /**
     * 필터 포함 검색 (분류 유형 + 날짜 범위)
     */
    suspend fun searchFiltered(
        query: String,
        type: ClassifiedType? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): List<Capture> {
        if (query.isBlank()) return emptyList()
        return captureRepository.searchCapturesFiltered(
            query = query,
            type = type,
            startDate = startDate,
            endDate = endDate
        )
    }
}
