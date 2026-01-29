package com.example.kairos_mobile.domain.usecase.ai

import com.example.kairos_mobile.data.remote.dto.ai.SuggestedTag
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M10: 스마트 태그 제안 Use Case
 *
 * 비즈니스 로직:
 * - 콘텐츠 분석을 통한 태그 제안
 * - 과거 패턴 기반 학습 태그 제안 (서버 AI)
 * - 분류 타입에 따른 관련 태그 제안
 */
@Singleton
class SuggestTagsUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    companion object {
        /**
         * 태그 제안 최소 콘텐츠 길이
         */
        const val MIN_CONTENT_LENGTH = 10
    }

    /**
     * 스마트 태그 제안
     *
     * @param content 분석할 콘텐츠
     * @param classification 분류 타입 (선택, 예: "SCHEDULE", "TODO")
     * @return 제안된 태그 리스트 또는 에러
     */
    suspend operator fun invoke(
        content: String,
        classification: String? = null
    ): Result<List<SuggestedTag>> {
        // 입력 검증: 너무 짧은 콘텐츠는 태그 제안 어려움
        if (content.length < MIN_CONTENT_LENGTH) {
            return Result.Error(
                IllegalArgumentException("콘텐츠가 너무 짧아 태그를 제안하기 어렵습니다")
            )
        }

        return repository.suggestTags(content, classification)
    }

    /**
     * 태그 제안이 가능한지 확인
     *
     * @param content 확인할 콘텐츠
     * @return 태그 제안 가능 여부
     */
    fun canSuggestTags(content: String): Boolean {
        return content.length >= MIN_CONTENT_LENGTH
    }
}
