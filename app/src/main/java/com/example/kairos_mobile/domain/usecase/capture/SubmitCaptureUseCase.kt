package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캡처 제출 Use Case
 *
 * 비즈니스 로직:
 * - 입력 검증
 * - Repository를 통해 캡처 제출
 * - 네트워크 있음: 분류 + Obsidian 생성 + 로컬 저장
 * - 네트워크 없음: 로컬 저장만 (나중에 동기화)
 */
@Singleton
class SubmitCaptureUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    /**
     * 캡처 제출
     */
    suspend operator fun invoke(content: String): Result<Capture> {
        // 입력 검증
        if (content.isBlank()) {
            return Result.Error(IllegalArgumentException("Content cannot be empty"))
        }

        return repository.submitCapture(content)
    }
}
