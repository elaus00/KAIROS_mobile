package com.flit.app.domain.model

/**
 * 캡처 분류 유형
 * AI 분류 또는 사용자 수동 수정으로 결정됨
 */
enum class ClassifiedType(val displayName: String) {
    /** 미분류 임시 상태. 캡처 직후 기본값, AI 분류 실패/오프라인 시 유지 */
    TEMP("미분류"),

    /** 할 일 */
    TODO("할 일"),

    /** 일정 */
    SCHEDULE("일정"),

    /** 노트 (note_sub_type으로 세분화) */
    NOTES("노트")
}
