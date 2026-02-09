package com.example.kairos_mobile.domain.usecase.classification

import com.example.kairos_mobile.domain.repository.ClassificationLogRepository
import javax.inject.Inject

/** 분류 수정 패턴 히스토리 빌드 */
class BuildModificationHistoryUseCase @Inject constructor(
    private val classificationLogRepository: ClassificationLogRepository
) {
    companion object {
        private const val MIN_HISTORY_REQUIRED = 20
    }

    suspend operator fun invoke(): Map<String, Any?> {
        val patterns = classificationLogRepository.getModificationPatterns()
        val totalModifications = patterns.sumOf { it.third }
        if (totalModifications < MIN_HISTORY_REQUIRED) {
            return emptyMap()
        }
        return mapOf(
            "total_modifications" to totalModifications,
            "patterns" to patterns.map { (fromType, toType, count) ->
                mapOf("from" to fromType, "to" to toType, "count" to count)
            }
        )
    }
}
