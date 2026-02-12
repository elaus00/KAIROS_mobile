package com.flit.app.domain.model

import java.util.UUID

/**
 * 폴더 도메인 모델
 * 노트의 분류 컨테이너
 */
data class Folder(
    val id: String = UUID.randomUUID().toString(),
    /** 폴더명 */
    val name: String,
    /** 폴더 유형 */
    val type: FolderType,
    /** 폴더 정렬 순서 */
    val sortOrder: Int = 0,
    /** 생성 시각 */
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /** 시스템 폴더 ID */
        const val SYSTEM_INBOX_ID = "system-inbox"
        const val SYSTEM_IDEAS_ID = "system-ideas"
        const val SYSTEM_BOOKMARKS_ID = "system-bookmarks"
    }
}
