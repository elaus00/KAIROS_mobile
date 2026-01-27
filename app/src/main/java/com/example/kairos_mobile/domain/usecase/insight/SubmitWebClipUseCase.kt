package com.example.kairos_mobile.domain.usecase.insight

import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.repository.InsightRepository
import com.example.kairos_mobile.domain.model.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M08: 웹 클립 Use Case
 *
 * URL을 받아 메타데이터를 추출하고 Insight를 생성합니다.
 */
@Singleton
class SubmitWebClipUseCase @Inject constructor(
    private val insightRepository: InsightRepository
) {

    /**
     * URL에서 메타데이터 추출 후 Insight 제출
     *
     * @param url 웹 페이지 URL
     * @return 메타데이터 추출 및 제출 결과
     */
    suspend operator fun invoke(url: String): Result<Insight> {
        return insightRepository.submitWebClip(url)
    }
}
