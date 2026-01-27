package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.InsightQueueEntity
import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.model.InsightSource
import com.example.kairos_mobile.domain.model.InsightType
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.SyncStatus
import com.example.kairos_mobile.domain.model.WebMetadata
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Insight Entity <-> Domain 변환 Mapper
 */
@Singleton
class InsightMapper @Inject constructor(
    private val gson: Gson
) {

    /**
     * Insight (Domain) → InsightQueueEntity (Entity)
     */
    fun toEntity(insight: Insight): InsightQueueEntity {
        return InsightQueueEntity(
            id = insight.id,
            content = insight.content,
            timestamp = insight.timestamp,
            syncStatus = insight.syncStatus.name,
            classificationType = insight.classification?.type?.name,
            destinationPath = insight.classification?.destinationPath,
            title = insight.classification?.title,
            tags = insight.classification?.tags?.let { gson.toJson(it) },
            confidence = insight.classification?.confidence,
            metadata = insight.classification?.metadata?.let { gson.toJson(it) },
            errorMessage = insight.error,

            // Phase 2: 멀티모달 캡처 필드
            source = insight.source.name,
            imageUri = insight.imageUri,
            audioUri = insight.audioUri,
            webUrl = insight.webMetadata?.url,
            webTitle = insight.webMetadata?.title,
            webDescription = insight.webMetadata?.description,
            webImageUrl = insight.webMetadata?.imageUrl,

            // Phase 3: 스마트 처리 기능
            summary = insight.summary,
            suggestedTags = insight.suggestedTags.takeIf { it.isNotEmpty() }?.let { gson.toJson(it) },
            appliedTags = insight.appliedTags.takeIf { it.isNotEmpty() }?.let { gson.toJson(it) }
        )
    }

    /**
     * InsightQueueEntity (Entity) → Insight (Domain)
     */
    fun toDomain(entity: InsightQueueEntity): Insight {
        val classification = if (entity.classificationType != null) {
            Classification(
                type = InsightType.valueOf(entity.classificationType),
                destinationPath = entity.destinationPath ?: "",
                title = entity.title ?: "",
                tags = entity.tags?.let {
                    gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
                } ?: emptyList(),
                confidence = entity.confidence ?: 0f,
                metadata = entity.metadata?.let {
                    gson.fromJson(it, object : TypeToken<Map<String, String>>() {}.type)
                } ?: emptyMap()
            )
        } else null

        // Phase 2: WebMetadata 복원
        val webMetadata = if (entity.webUrl != null) {
            WebMetadata(
                url = entity.webUrl,
                title = entity.webTitle,
                description = entity.webDescription,
                imageUrl = entity.webImageUrl
            )
        } else null

        return Insight(
            id = entity.id,
            content = entity.content,
            source = InsightSource.valueOf(entity.source),  // Phase 2
            timestamp = entity.timestamp,
            syncStatus = SyncStatus.valueOf(entity.syncStatus),
            classification = classification,
            error = entity.errorMessage,

            // Phase 2: 멀티모달 캡처 필드
            imageUri = entity.imageUri,
            audioUri = entity.audioUri,
            webMetadata = webMetadata,

            // Phase 3: 스마트 처리 기능
            summary = entity.summary,
            suggestedTags = entity.suggestedTags?.let {
                gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
            } ?: emptyList(),
            appliedTags = entity.appliedTags?.let {
                gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
            } ?: emptyList()
        )
    }
}
