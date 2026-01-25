package com.example.kairos_mobile.domain.usecase

import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M09: AI 요약 생성 Use Case
 *
 * 비즈니스 로직:
 * - 콘텐츠 길이 검증 (최소 길이 체크)
 * - 서버 AI를 통한 요약 생성
 * - 긴 콘텐츠를 간결하게 요약
 */
@Singleton
class GenerateSummaryUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    companion object {
        /**
         * 요약 대상 최소 콘텐츠 길이
         * 이 길이 미만의 콘텐츠는 요약할 필요 없음
         */
        const val MIN_CONTENT_LENGTH = 200
    }

    /**
     * AI 요약 생성
     *
     * @param captureId 캡처 ID
     * @param content 요약할 콘텐츠
     * @return 요약된 텍스트 또는 에러
     */
    suspend operator fun invoke(captureId: String, content: String): Result<String> {
        // 입력 검증: 너무 짧은 콘텐츠는 요약 불필요
        if (content.length < MIN_CONTENT_LENGTH) {
            return Result.Error(
                IllegalArgumentException("콘텐츠가 너무 짧아 요약이 필요하지 않습니다 (${content.length}자)")
            )
        }

        return repository.generateSummary(captureId, content)
    }

    /**
     * 요약이 필요한지 확인
     *
     * @param content 확인할 콘텐츠
     * @return 요약 필요 여부
     */
    fun shouldSummarize(content: String): Boolean {
        return content.length >= MIN_CONTENT_LENGTH
    }
}
