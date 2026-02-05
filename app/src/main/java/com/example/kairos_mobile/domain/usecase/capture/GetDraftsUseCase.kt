package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.repository.CaptureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Draft 목록 조회 Use Case
 *
 * 임시저장된 캡처 목록 조회
 */
@Singleton
class GetDraftsUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    /**
     * Draft 목록 조회
     *
     * @return Draft 리스트 Flow
     */
    operator fun invoke(): Flow<List<Capture>> {
        return repository.getDrafts()
    }
}
