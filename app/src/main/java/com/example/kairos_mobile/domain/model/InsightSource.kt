package com.example.kairos_mobile.domain.model

/**
 * 인사이트 소스 타입
 * Phase 2에서 추가된 멀티모달 캡처 지원
 */
enum class InsightSource {
    /**
     * 텍스트 입력 (Phase 1)
     */
    TEXT,

    /**
     * 이미지 + OCR (Phase 2 - M05)
     */
    IMAGE,

    /**
     * 음성 입력 + STT (Phase 2 - M06)
     */
    VOICE,

    /**
     * 공유 인텐트 (Phase 2 - M07)
     */
    SHARE,

    /**
     * 웹 클립 + 메타데이터 추출 (Phase 2 - M08)
     */
    WEB_CLIP
}
