package com.example.kairos_mobile.domain.usecase.note

import com.example.kairos_mobile.domain.model.ApiException
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.repository.NoteAiRepository
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import javax.inject.Inject

/** 인박스 자동 분류 (프리미엄 전용) */
class InboxAutoClassifyUseCase @Inject constructor(
    private val noteAiRepository: NoteAiRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(captureIds: List<String>): List<Triple<String, String, Double>> {
        if (subscriptionRepository.getCachedTier() != SubscriptionTier.PREMIUM) {
            throw ApiException.SubscriptionRequired()
        }
        return noteAiRepository.inboxClassify(captureIds)
    }
}
