package com.flit.app.domain.usecase.search

import com.flit.app.domain.model.ApiException
import com.flit.app.domain.model.SemanticSearchResult
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.domain.repository.NoteAiRepository
import com.flit.app.domain.repository.SubscriptionRepository
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
