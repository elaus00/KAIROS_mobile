package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.SubscriptionFeatures
import com.example.kairos_mobile.domain.model.SubscriptionTier

/** 구독 Repository 인터페이스 */
interface SubscriptionRepository {
    suspend fun getSubscription(): Pair<SubscriptionTier, SubscriptionFeatures>
    suspend fun verifyPurchase(purchaseToken: String, productId: String): Pair<SubscriptionTier, SubscriptionFeatures>
    suspend fun checkFeature(featureName: String): Boolean
    fun getCachedTier(): SubscriptionTier
    fun getCachedFeatures(): SubscriptionFeatures
}
