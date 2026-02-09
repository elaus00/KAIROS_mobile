package com.example.kairos_mobile.domain.model

/** 캘린더 충돌 해결 방식 */
enum class ConflictResolution {
    /** 병합 (기본: Google 우선) */
    MERGE,
    /** 로컬 데이터를 Google로 덮어쓰기 */
    OVERRIDE_LOCAL,
    /** Google 데이터를 로컬로 덮어쓰기 */
    OVERRIDE_GOOGLE
}
