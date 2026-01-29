package com.example.kairos_mobile.domain.model

/**
 * 캡처 소스 타입
 * 멀티모달 캡처 지원
 */
enum class CaptureSource {
    /**
     * 텍스트 입력
     */
    TEXT,

    /**
     * 이미지 + OCR
     */
    IMAGE,

    /**
     * 음성 입력 + STT
     */
    VOICE,

    /**
     * 공유 인텐트
     */
    SHARE,

    /**
     * 웹 클립 + 메타데이터 추출
     */
    WEB_CLIP
}
