package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캡처 휴지통 이동 UseCase
 * is_trashed=1 설정
 */
@Singleton
class MoveToTrashUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    suspend operator fun invoke(captureId: String) {
        captureRepository.moveToTrash(captureId)
    }
}
