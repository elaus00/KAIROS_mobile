package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.local.database.entities.CaptureQueueEntity
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.Destination
import com.example.kairos_mobile.domain.model.SyncStatus
import com.example.kairos_mobile.domain.model.WebMetadata
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Capture Entity <-> Domain 변환 Mapper
 */
@Singleton
class CaptureMapper @Inject constructor(
    private val gson: Gson
) {

    /**
     * Capture (Domain) → CaptureQueueEntity (Entity)
     */
    fun toEntity(capture: Capture): CaptureQueueEntity {
        return CaptureQueueEntity(
            id = capture.id,
            content = capture.content,
            timestamp = capture.timestamp,
            syncStatus = capture.syncStatus.name,
            classificationType = capture.classification?.type?.name,
            destinationPath = capture.classification?.suggestedPath,
            title = capture.classification?.title,
            tags = capture.classification?.tags?.let { gson.toJson(it) },
            confidence = capture.classification?.confidence,
            metadata = null,
            errorMessage = capture.error,

            // 멀티모달 캡처 필드
            source = capture.source.name,
            imageUri = capture.imageUri,
            audioUri = capture.audioUri,
            webUrl = capture.webMetadata?.url,
            webTitle = capture.webMetadata?.title,
            webDescription = capture.webMetadata?.description,
            webImageUrl = capture.webMetadata?.imageUrl,

            // 스마트 처리 기능
            summary = capture.summary,
            suggestedTags = capture.suggestedTags.takeIf { it.isNotEmpty() }?.let { gson.toJson(it) },
            appliedTags = capture.appliedTags.takeIf { it.isNotEmpty() }?.let { gson.toJson(it) }
        )
    }

    /**
     * CaptureQueueEntity (Entity) → Capture (Domain)
     */
    fun toDomain(entity: CaptureQueueEntity): Capture {
        val classification = if (entity.classificationType != null) {
            val captureType = CaptureType.valueOf(entity.classificationType)
            // destination 결정: TODO 타입이면 TODO, 그 외는 OBSIDIAN
            val destination = if (captureType == CaptureType.TODO) {
                Destination.TODO
            } else {
                Destination.OBSIDIAN
            }
            Classification(
                type = captureType,
                destination = destination,
                confidence = entity.confidence ?: 0f,
                title = entity.title ?: "",
                tags = entity.tags?.let {
                    gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
                } ?: emptyList(),
                suggestedPath = entity.destinationPath
            )
        } else null

        // WebMetadata 복원
        val webMetadata = if (entity.webUrl != null) {
            WebMetadata(
                url = entity.webUrl,
                title = entity.webTitle,
                description = entity.webDescription,
                imageUrl = entity.webImageUrl
            )
        } else null

        return Capture(
            id = entity.id,
            content = entity.content,
            source = CaptureSource.valueOf(entity.source),
            timestamp = entity.timestamp,
            syncStatus = SyncStatus.valueOf(entity.syncStatus),
            classification = classification,
            error = entity.errorMessage,

            // 멀티모달 캡처 필드
            imageUri = entity.imageUri,
            audioUri = entity.audioUri,
            webMetadata = webMetadata,

            // 스마트 처리 기능
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
