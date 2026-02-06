package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캡처 소프트 삭제 취소 UseCase
 * Snackbar에서 "실행 취소" 선택 시
 */
@Singleton
class UndoDeleteCaptureUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    suspend operator fun invoke(captureId: String) {
        captureRepository.undoSoftDelete(captureId)
    }
}
