package com.example.kairos_mobile.domain.usecase

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.repository.CaptureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 대기중인 캡처 조회 Use Case
 *
 * 비즈니스 로직:
 * - 오프라인 큐에서 동기화 대기중인 캡처 목록 조회
 * - UI에서 오프라인 상태 표시용
 */
@Singleton
class GetPendingCapturesUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    /**
     * 대기중인 캡처 목록 Flow 반환
     */
    operator fun invoke(): Flow<List<Capture>> {
        return repository.getPendingCaptures()
    }
}
