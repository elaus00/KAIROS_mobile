package com.example.kairos_mobile.domain.usecase

import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.domain.util.KeywordMatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 키워드 매칭 Use Case
 *
 * 비즈니스 로직:
 * - 입력된 텍스트에서 키워드를 분석
 * - 적합한 CaptureType을 추천
 * - QuickTypeButtons에 동적으로 표시할 타입 제공
 */
@Singleton
class MatchKeywordsUseCase @Inject constructor() {

    /**
     * 텍스트에서 매칭되는 CaptureType 찾기
     *
     * @param text 분석할 텍스트
     * @return 추천 CaptureType 리스트 (최대 3개)
     */
    operator fun invoke(text: String): List<CaptureType> {
        return KeywordMatcher.matchTypes(text)
    }

    /**
     * 가장 강하게 매칭되는 타입 반환
     *
     * @param text 분석할 텍스트
     * @return 가장 적합한 CaptureType (없으면 null)
     */
    fun getBestMatch(text: String): CaptureType? {
        return KeywordMatcher.getBestMatch(text)
    }
}
