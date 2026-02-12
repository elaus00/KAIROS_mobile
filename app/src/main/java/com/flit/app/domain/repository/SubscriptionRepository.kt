package com.flit.app.domain.repository

import com.flit.app.domain.model.SubscriptionFeatures
import com.flit.app.domain.model.SubscriptionTier

/** 구독 Repository 인터페이스 */
interface SubscriptionRepository {
    suspend fun getSubscription(): Pair<SubscriptionTier, SubscriptionFeatures>
    suspend fun verifyPurchase(purchaseToken: String, productId: String): Pair<SubscriptionTier, SubscriptionFeatures>
    suspend fun checkFeature(featureName: String): Boolean
    fun getCachedTier(): SubscriptionTier
    fun getCachedFeatures(): SubscriptionFeatures
}
