package com.example.kairos_mobile.domain.usecase.folder

import com.example.kairos_mobile.domain.repository.FolderRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 폴더 이름 변경 UseCase
 */
@Singleton
class RenameFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(folderId: String, newName: String) {
        require(newName.isNotBlank()) { "폴더 이름이 비어있습니다" }
        require(!folderRepository.existsByName(newName)) { "이미 존재하는 폴더 이름입니다" }
        folderRepository.renameFolder(folderId, newName)
    }
}
