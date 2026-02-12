package com.flit.app.domain.model

/**
 * 폴더 유형
 */
enum class FolderType {
    /** 미분류 노트 기본 배치 (system-inbox) */
    INBOX,

    /** 아이디어 (system-ideas) */
    IDEAS,

    /** 북마크/URL (system-bookmarks) */
    BOOKMARKS,

    /** AI 자동 그룹화 (Phase 3a) */
    AI_GROUP,

    /** 사용자 생성 폴더 */
    USER
}
