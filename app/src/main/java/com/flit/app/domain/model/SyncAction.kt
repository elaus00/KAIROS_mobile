package com.flit.app.domain.model

/**
 * 동기화 큐 작업 유형
 */
enum class SyncAction {
    /** AI 분류 요청 */
    CLASSIFY,

    /** TEMP 재분류 */
    RECLASSIFY,

    /** 캘린더 이벤트 생성 (Phase 2a) */
    CALENDAR_CREATE,

    /** 캘린더 이벤트 삭제 (Phase 2a) */
    CALENDAR_DELETE,

    /** 분석 이벤트 배치 전송 (Phase 2a) */
    ANALYTICS_BATCH
}
