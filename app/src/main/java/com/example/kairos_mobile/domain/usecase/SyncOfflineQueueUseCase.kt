package com.example.kairos_mobile.domain.usecase

import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 오프라인 큐 동기화 Use Case
 *
 * 비즈니스 로직:
 * - WorkManager에서 주기적으로 호출
 * - 대기중인 캡처들을 서버와 동기화
 * - 성공한 항목 수 반환
 */
@Singleton
class SyncOfflineQueueUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    /**
     * 오프라인 큐 동기화 실행
     * @return 동기화된 항목 수
     */
    suspend operator fun invoke(): Result<Int> {
        return repository.syncOfflineQueue()
    }
}
