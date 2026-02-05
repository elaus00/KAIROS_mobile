package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Draft 저장 Use Case
 *
 * QuickCapture 축소 시 임시저장
 */
@Singleton
class SaveDraftUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    /**
     * Draft 저장
     *
     * @param content 캡처 내용
     * @param suggestedType 추천된 타입 (선택)
     * @return 저장된 Draft
     */
    suspend operator fun invoke(
        content: String,
        suggestedType: CaptureType? = null
    ): Result<Capture> {
        // 빈 내용은 저장하지 않음
        if (content.isBlank()) {
            return Result.Error(IllegalArgumentException("Content cannot be empty"))
        }

        return repository.saveDraft(content, suggestedType)
    }
}
