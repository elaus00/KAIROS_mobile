package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Draft 삭제 Use Case
 *
 * 임시저장된 캡처 삭제 (QuickCapture 닫기 버튼)
 */
@Singleton
class DeleteDraftUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    /**
     * Draft 삭제
     *
     * @param captureId 삭제할 Draft ID
     * @return 삭제 결과
     */
    suspend operator fun invoke(captureId: String): Result<Unit> {
        return repository.deleteDraft(captureId)
    }
}
