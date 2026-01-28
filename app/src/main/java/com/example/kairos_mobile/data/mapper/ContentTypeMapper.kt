package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.domain.model.InsightSource

/**
 * InsightSource ↔ API content_type 문자열 변환 Mapper
 */
object ContentTypeMapper {

    /**
     * InsightSource → API content_type 문자열
     */
    fun toApiContentType(source: InsightSource): String {
        return when (source) {
            InsightSource.TEXT -> "text"
            InsightSource.IMAGE -> "image"
            InsightSource.VOICE -> "audio"
            InsightSource.SHARE -> "text"  // 공유 인텐트는 텍스트로 처리
            InsightSource.WEB_CLIP -> "url"
        }
    }

    /**
     * API content_type 문자열 → InsightSource
     */
    fun fromApiContentType(contentType: String): InsightSource {
        return when (contentType.lowercase()) {
            "text" -> InsightSource.TEXT
            "image" -> InsightSource.IMAGE
            "audio" -> InsightSource.VOICE
            "url" -> InsightSource.WEB_CLIP
            else -> InsightSource.TEXT  // 기본값
        }
    }
}
