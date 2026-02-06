package com.example.kairos_mobile.domain.model

/**
 * 캡처 입력 소스
 */
enum class CaptureSource {
    /** 앱 내 직접 입력 */
    APP,

    /** 외부 앱 공유 인텐트 */
    SHARE_INTENT,

    /** 홈 화면 위젯 (Phase 2a) */
    WIDGET,

    /** 멀티 인텐트 분할 (Phase 2b) */
    SPLIT
}
