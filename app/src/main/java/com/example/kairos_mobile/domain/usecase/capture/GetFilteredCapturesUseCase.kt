package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 필터링된 캡처 조회 UseCase (분류 유형 + 날짜 범위)
 */
@Singleton
class GetFilteredCapturesUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    suspend operator fun invoke(
        type: ClassifiedType? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        limit: Int = 20,
        offset: Int = 0
    ): List<Capture> {
        return captureRepository.getFilteredCaptures(
            type = type,
            startDate = startDate,
            endDate = endDate,
            limit = limit,
            offset = offset
        )
    }
}
