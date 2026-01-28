package com.example.kairos_mobile.domain.model

/**
 * AI 분류 결과 (API v2.1 기준)
 */
data class Classification(
    val type: InsightType,                           // 분류된 타입
    val destination: Destination,                    // 라우팅 목적지 (todo/obsidian)
    val confidence: Float,                           // 신뢰도 (0.0 ~ 1.0)
    val reasoning: String? = null,                   // AI 분류 근거
    val title: String,                               // 자동 생성된 제목
    val tags: List<String>,                          // 추출된 태그
    val suggestedFilename: String? = null,           // 제안 파일명 (Obsidian용)
    val suggestedPath: String? = null,               // 제안 경로 (Obsidian용)
    val todoMetadata: TodoMetadata? = null,          // Todo 메타데이터 (destination이 TODO인 경우)

    // 하위 호환성을 위한 필드
    @Deprecated("Use suggestedPath instead")
    val destinationPath: String = suggestedPath ?: "",
    @Deprecated("Use todoMetadata and other fields instead")
    val metadata: Map<String, String> = emptyMap()
)
