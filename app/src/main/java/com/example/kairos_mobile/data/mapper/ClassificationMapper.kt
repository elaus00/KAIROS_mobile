package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.data.remote.dto.ClassificationResponse
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.domain.model.Classification
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Classification DTO <-> Domain 변환 Mapper
 */
@Singleton
class ClassificationMapper @Inject constructor() {

    /**
     * ClassificationResponse (DTO) → Classification (Domain)
     */
    fun toDomain(response: ClassificationResponse): Classification {
        return Classification(
            type = CaptureType.valueOf(response.type),
            destinationPath = response.destinationPath,
            title = response.title,
            tags = response.tags,
            confidence = response.confidence,
            metadata = response.metadata ?: emptyMap()
        )
    }
}
