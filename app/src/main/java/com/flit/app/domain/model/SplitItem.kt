package com.flit.app.domain.model

/**
 * 다중 의도 분리 항목 도메인 모델
 * AI 분류 시 여러 의도가 감지된 경우 각 분리 항목을 나타낸다.
 */
data class SplitItem(
    /** 분리된 의도에 해당하는 텍스트 */
    val splitText: String,
    /** 분류 유형 */
    val classifiedType: ClassifiedType,
    /** 노트 서브 분류 */
    val noteSubType: NoteSubType? = null,
    /** 신뢰도 */
    val confidence: ConfidenceLevel,
    /** AI 생성 제목 */
    val aiTitle: String,
    /** 태그 */
    val tags: List<String> = emptyList(),
    /** 일정 정보 (SCHEDULE일 때) */
    val scheduleInfo: ScheduleInfo? = null,
    /** 할 일 정보 (TODO일 때) */
    val todoInfo: TodoInfo? = null
)
