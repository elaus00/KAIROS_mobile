package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 웹 클립 제출 Use Case
 *
 * 비즈니스 로직:
 * - URL에서 메타데이터 추출
 * - 추출된 정보로 캡처 제출
 */
@Singleton
class SubmitWebClipUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    /**
     * 웹 클립 제출
     */
    suspend operator fun invoke(url: String): Result<Capture> {
        if (url.isBlank()) {
            return Result.Error(IllegalArgumentException("URL cannot be empty"))
        }
        return repository.submitWebClip(url)
    }
}
