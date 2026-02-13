package com.flit.app.domain.model

/**
 * 노트 보기 유형
 */
enum class NoteViewType {
    /** 리스트 (기본) — 확장 가능 카드 */
    LIST,
    /** 그리드 (2열) — 카드형 */
    GRID,
    /** 컴팩트 — 제목만, 미리보기 없음 */
    COMPACT
}
