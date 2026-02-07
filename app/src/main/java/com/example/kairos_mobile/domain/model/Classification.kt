package com.example.kairos_mobile.domain.model

/**
 * AI 분류 응답 매핑
 * 서버 분류 결과를 도메인 모델로 변환할 때 사용
 */
data class Classification(
    /** 분류된 유형 */
    val type: ClassifiedType,
    /** 노트 서브 분류 (type=NOTES일 때) */
    val subType: NoteSubType? = null,
    /** 신뢰도 */
    val confidence: ConfidenceLevel,
    /** AI 생성 제목 */
    val aiTitle: String,
    /** 추출된 태그 */
    val tags: List<String> = emptyList(),
    /** 추출된 엔티티 */
    val entities: List<ExtractedEntity> = emptyList(),
    /** 일정 정보 (type=SCHEDULE일 때) */
    val scheduleInfo: ScheduleInfo? = null,
    /** 할 일 정보 (type=TODO일 때) */
    val todoInfo: TodoInfo? = null,
    /** 다중 의도 분리 결과 (null이면 단일 의도) */
    val splitItems: List<SplitItem>? = null
)

/**
 * AI 추출 일정 정보
 */
data class ScheduleInfo(
    val startTime: Long? = null,
    val endTime: Long? = null,
    val location: String? = null,
    val isAllDay: Boolean = false
)

/**
 * AI 추출 할 일 정보
 */
data class TodoInfo(
    val deadline: Long? = null,
    val deadlineSource: DeadlineSource? = null
)
