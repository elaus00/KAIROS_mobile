package com.example.kairos_mobile.data.repository

import android.content.SharedPreferences
import com.example.kairos_mobile.data.remote.ApiResponseHandler
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.v2.SubscriptionFeaturesDto
import com.example.kairos_mobile.data.remote.dto.v2.SubscriptionVerifyRequest
import com.example.kairos_mobile.domain.model.SubscriptionFeatures
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * 구독 Repository 구현체
 * 서버에서 구독 정보 조회/검증, EncryptedSharedPreferences에 캐시
 */
@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val api: KairosApi,
    @Named("encrypted_prefs") private val prefs: SharedPreferences
) : SubscriptionRepository {

    companion object {
        private const val KEY_TIER = "subscription_tier"
        private const val KEY_FEATURES = "subscription_features"
    }

    private val gson = Gson()

    override suspend fun getSubscription(): Pair<SubscriptionTier, SubscriptionFeatures> {
        val response = ApiResponseHandler.safeCall { api.getSubscription() }
        val tier = parseTier(response.tier)
        val features = mapFeatures(response.features)
        cacheTierAndFeatures(tier, features)
        return tier to features
    }

    override suspend fun verifyPurchase(
        purchaseToken: String,
        productId: String
    ): Pair<SubscriptionTier, SubscriptionFeatures> {
        val request = SubscriptionVerifyRequest(
            purchaseToken = purchaseToken,
            productId = productId
        )
        val response = ApiResponseHandler.safeCall { api.verifySubscription(request) }
        val tier = parseTier(response.tier)
        val features = mapFeatures(response.features)
        cacheTierAndFeatures(tier, features)
        return tier to features
    }

    override suspend fun checkFeature(featureName: String): Boolean {
        val features = getCachedFeatures()
        return when (featureName) {
            "ai_grouping" -> features.aiGrouping
            "inbox_classify" -> features.inboxClassify
            "semantic_search" -> features.semanticSearch
            "note_reorganize" -> features.noteReorganize
            "analytics_dashboard" -> features.analyticsDashboard
            "ocr" -> features.ocr
            "classification_preset" -> features.classificationPreset
            "custom_instruction" -> features.customInstruction
            "modification_learning" -> features.modificationLearning
            else -> false
        }
    }

    override fun getCachedTier(): SubscriptionTier {
        val tierStr = prefs.getString(KEY_TIER, "FREE") ?: "FREE"
        return parseTier(tierStr)
    }

    override fun getCachedFeatures(): SubscriptionFeatures {
        val json = prefs.getString(KEY_FEATURES, null)
            ?: return SubscriptionFeatures()
        return try {
            gson.fromJson(json, SubscriptionFeatures::class.java)
        } catch (_: Exception) {
            SubscriptionFeatures()
        }
    }

    private fun parseTier(tier: String): SubscriptionTier {
        return try {
            SubscriptionTier.valueOf(tier.uppercase())
        } catch (_: Exception) {
            SubscriptionTier.FREE
        }
    }

    private fun mapFeatures(dto: SubscriptionFeaturesDto): SubscriptionFeatures {
        return SubscriptionFeatures(
            aiGrouping = dto.aiGrouping,
            inboxClassify = dto.inboxClassify,
            semanticSearch = dto.semanticSearch,
            noteReorganize = dto.noteReorganize,
            analyticsDashboard = dto.analyticsDashboard,
            ocr = dto.ocr,
            classificationPreset = dto.classificationPreset,
            customInstruction = dto.customInstruction,
            modificationLearning = dto.modificationLearning
        )
    }

    private fun cacheTierAndFeatures(tier: SubscriptionTier, features: SubscriptionFeatures) {
        prefs.edit()
            .putString(KEY_TIER, tier.name)
            .putString(KEY_FEATURES, gson.toJson(features))
            .apply()
    }
}
