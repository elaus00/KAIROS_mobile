package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캡처 소프트 삭제 UseCase
 * Snackbar 실행 취소용 — is_deleted=1 설정
 */
@Singleton
class SoftDeleteCaptureUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    suspend operator fun invoke(captureId: String) {
        captureRepository.softDelete(captureId)
    }
}
