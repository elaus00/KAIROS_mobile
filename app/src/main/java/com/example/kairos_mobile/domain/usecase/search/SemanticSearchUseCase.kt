package com.example.kairos_mobile.domain.usecase.search

import com.example.kairos_mobile.domain.model.ApiException
import com.example.kairos_mobile.domain.model.SemanticSearchResult
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.repository.NoteAiRepository
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import javax.inject.Inject

/** 시맨틱 검색 (프리미엄 전용) */
class SemanticSearchUseCase @Inject constructor(
    private val noteAiRepository: NoteAiRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(query: String, limit: Int = 20): List<SemanticSearchResult> {
        if (subscriptionRepository.getCachedTier() != SubscriptionTier.PREMIUM) {
            throw ApiException.SubscriptionRequired()
        }
        return noteAiRepository.semanticSearch(query, limit)
    }
}
