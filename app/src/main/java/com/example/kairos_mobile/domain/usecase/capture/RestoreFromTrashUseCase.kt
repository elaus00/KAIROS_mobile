package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캡처 휴지통 복원 UseCase
 * is_trashed=0 설정
 */
@Singleton
class RestoreFromTrashUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    suspend operator fun invoke(captureId: String) {
        captureRepository.restoreFromTrash(captureId)
    }
}
