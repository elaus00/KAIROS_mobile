package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.repository.CaptureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 휴지통 항목 조회 UseCase
 */
@Singleton
class GetTrashItemsUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    operator fun invoke(): Flow<List<Capture>> = captureRepository.getTrashedItems()
}
