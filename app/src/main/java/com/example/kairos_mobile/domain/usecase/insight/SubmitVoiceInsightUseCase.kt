package com.example.kairos_mobile.domain.usecase.insight

import android.net.Uri
import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.repository.InsightRepository
import com.example.kairos_mobile.domain.model.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M06: 음성 입력 Use Case
 *
 * 음성으로 변환된 텍스트를 받아 Insight를 생성합니다.
 */
@Singleton
class SubmitVoiceInsightUseCase @Inject constructor(
    private val insightRepository: InsightRepository
) {

    /**
     * 음성 인식 결과 텍스트로 Insight 제출
     *
     * @param audioText STT로 변환된 텍스트
     * @param audioUri 녹음 파일 URI (선택사항)
     * @return 제출 결과
     */
    suspend operator fun invoke(audioText: String, audioUri: Uri? = null): Result<Insight> {
        return insightRepository.submitVoiceInsight(audioText, audioUri)
    }
}
