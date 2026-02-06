package com.example.kairos_mobile.domain.usecase.search

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.repository.CaptureRepository
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
    operator fun invoke(query: String): Flow<List<Capture>> {
        if (query.isBlank()) return emptyFlow()
        return captureRepository.searchCaptures(query)
    }
}
