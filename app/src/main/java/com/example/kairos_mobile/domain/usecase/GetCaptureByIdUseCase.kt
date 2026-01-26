package com.example.kairos_mobile.domain.usecase

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 특정 캡처 조회 Use Case
 *
 * 비즈니스 로직:
 * - ID로 특정 캡처 조회
 * - 알림에서 캡처로 이동할 때 사용
 */
@Singleton
class GetCaptureByIdUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    /**
     * ID로 캡처 조회
     *
     * @param id 캡처 ID
     * @return 캡처 객체 (없으면 null)
     */
    suspend operator fun invoke(id: String): Result<Capture?> {
        if (id.isBlank()) {
            return Result.Error(IllegalArgumentException("Capture ID cannot be empty"))
        }

        return repository.getCaptureById(id)
    }
}
