package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 휴지통 비우기 UseCase
 * 모든 휴지통 항목 완전 삭제
 */
@Singleton
class EmptyTrashUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    suspend operator fun invoke() {
        // threshold=Long.MAX_VALUE로 모든 휴지통 항목 가져옴
        val allTrashed = captureRepository.getTrashedOverdue(Long.MAX_VALUE)
        allTrashed.forEach { captureRepository.hardDelete(it.id) }
    }
}
