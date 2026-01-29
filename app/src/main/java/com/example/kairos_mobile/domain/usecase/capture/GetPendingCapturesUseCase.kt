package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.repository.CaptureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 대기중인 캡처 조회 Use Case
 *
 * 비즈니스 로직:
 * - 동기화되지 않은 캡처 목록 조회
 */
@Singleton
class GetPendingCapturesUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    /**
     * 대기중인 캡처 조회
     */
    operator fun invoke(): Flow<List<Capture>> {
        return repository.getPendingCaptures()
    }
}
