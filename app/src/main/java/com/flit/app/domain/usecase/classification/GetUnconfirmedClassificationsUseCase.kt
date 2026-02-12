package com.flit.app.domain.usecase.classification

import com.flit.app.domain.model.Capture
import com.flit.app.domain.repository.CaptureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 미확인 AI 분류 조회 UseCase
 * AI Status Sheet용 미확인 분류 목록
 */
@Singleton
class GetUnconfirmedClassificationsUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {
    /** 미확인 분류 목록 */
    operator fun invoke(): Flow<List<Capture>> {
        return captureRepository.getUnconfirmedClassifications()
    }

    /** 미확인 분류 수 */
    fun count(): Flow<Int> {
        return captureRepository.getUnconfirmedCount()
    }
}
