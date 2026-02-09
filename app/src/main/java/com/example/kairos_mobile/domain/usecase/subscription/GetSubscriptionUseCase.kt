package com.example.kairos_mobile.domain.usecase.subscription

import com.example.kairos_mobile.domain.model.SubscriptionFeatures
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import javax.inject.Inject

/** 구독 정보 조회 */
class GetSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(): Pair<SubscriptionTier, SubscriptionFeatures> {
        return subscriptionRepository.getSubscription()
    }
}
