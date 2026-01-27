package com.example.kairos_mobile.domain.usecase.insight

import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.InsightRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 인사이트 제출 Use Case
 *
 * 비즈니스 로직:
 * - 입력 검증
 * - Repository를 통해 인사이트 제출
 * - 네트워크 있음: 분류 + Obsidian 생성 + 로컬 저장
 * - 네트워크 없음: 로컬 저장만 (나중에 동기화)
 */
@Singleton
class SubmitInsightUseCase @Inject constructor(
    private val repository: InsightRepository
) {
    /**
     * 인사이트 제출
     */
    suspend operator fun invoke(content: String): Result<Insight> {
        // 입력 검증
        if (content.isBlank()) {
            return Result.Error(IllegalArgumentException("Content cannot be empty"))
        }

        return repository.submitInsight(content)
    }
}
