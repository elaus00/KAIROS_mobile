package com.flit.app.domain.model

/**
 * 노트 서브 분류
 * classified_type=NOTES일 때만 사용
 */
enum class NoteSubType {
    /** 미분류 노트. 기본 서브 분류 */
    INBOX,

    /** 아이디어성 내용 */
    IDEA,

    /** URL 포함 콘텐츠 */
    BOOKMARK,

    /** 사용자가 직접 폴더로 이동한 노트 */
    USER_FOLDER
}
