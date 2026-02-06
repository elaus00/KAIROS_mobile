package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.Folder
import kotlinx.coroutines.flow.Flow

/**
 * 폴더 Repository 인터페이스
 */
interface FolderRepository {

    /** 전체 폴더 조회 (sort_order 순) */
    fun getAllFolders(): Flow<List<Folder>>

    /** 폴더 생성 */
    suspend fun createFolder(folder: Folder)

    /** 폴더 이름 변경 */
    suspend fun renameFolder(folderId: String, newName: String)

    /** 폴더 삭제 (소속 노트는 Inbox로 이동) */
    suspend fun deleteFolder(folderId: String)

    /** 이름 중복 확인 */
    suspend fun existsByName(name: String): Boolean
}
