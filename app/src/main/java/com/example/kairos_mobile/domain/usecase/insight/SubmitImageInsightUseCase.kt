package com.example.kairos_mobile.domain.usecase.insight

import android.net.Uri
import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.repository.InsightRepository
import com.example.kairos_mobile.domain.model.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M05: 이미지 인사이트 + OCR Use Case
 *
 * 이미지를 받아 OCR로 텍스트를 추출하고 Insight를 생성합니다.
 */
@Singleton
class SubmitImageInsightUseCase @Inject constructor(
    private val insightRepository: InsightRepository
) {

    /**
     * 이미지 URI를 받아 OCR 처리 후 Insight 제출
     *
     * @param imageUri 이미지 URI
     * @return OCR 처리 및 제출 결과
     */
    suspend operator fun invoke(imageUri: Uri): Result<Insight> {
        return insightRepository.submitImageInsight(imageUri)
    }
}
