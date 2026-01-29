package com.example.kairos_mobile.data.mapper

import com.example.kairos_mobile.domain.model.CaptureSource

/**
 * CaptureSource ↔ API content_type 문자열 변환 Mapper
 */
object ContentTypeMapper {

    /**
     * CaptureSource → API content_type 문자열
     */
    fun toCaptureApiContentType(source: CaptureSource): String {
        return when (source) {
            CaptureSource.TEXT -> "text"
            CaptureSource.IMAGE -> "image"
            CaptureSource.VOICE -> "audio"
            CaptureSource.SHARE -> "text"  // 공유 인텐트는 텍스트로 처리
            CaptureSource.WEB_CLIP -> "url"
        }
    }

    /**
     * API content_type 문자열 → CaptureSource
     */
    fun fromApiContentType(contentType: String): CaptureSource {
        return when (contentType.lowercase()) {
            "text" -> CaptureSource.TEXT
            "image" -> CaptureSource.IMAGE
            "audio" -> CaptureSource.VOICE
            "url" -> CaptureSource.WEB_CLIP
            else -> CaptureSource.TEXT  // 기본값
        }
    }
}
