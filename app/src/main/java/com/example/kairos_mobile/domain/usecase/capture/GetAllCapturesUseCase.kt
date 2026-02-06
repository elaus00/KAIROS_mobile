package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.repository.CaptureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 전체 캡처 조회 UseCase (페이지네이션)
 */
@Singleton
class GetAllCapturesUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    operator fun invoke(
        offset: Int = 0,
        limit: Int = 20
    ): Flow<List<Capture>> {
        return captureRepository.getAllCaptures(offset, limit)
    }
}
