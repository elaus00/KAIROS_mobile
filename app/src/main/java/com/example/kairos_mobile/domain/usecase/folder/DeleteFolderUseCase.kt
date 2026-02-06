package com.example.kairos_mobile.domain.usecase.folder

import com.example.kairos_mobile.domain.repository.FolderRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 폴더 삭제 UseCase
 * 소속 노트는 Inbox로 자동 이동
 */
@Singleton
class DeleteFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(folderId: String) {
        folderRepository.deleteFolder(folderId)
    }
}
