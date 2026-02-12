package com.flit.app.domain.usecase.folder

import com.flit.app.domain.model.Folder
import com.flit.app.domain.model.FolderType
import com.flit.app.domain.repository.FolderRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 폴더 생성 UseCase
 */
@Singleton
class CreateFolderUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    suspend operator fun invoke(name: String) {
        require(name.isNotBlank()) { "폴더 이름이 비어있습니다" }
        require(name.length <= 30) { "폴더 이름은 30자 이내여야 합니다" }
        require(!folderRepository.existsByName(name)) { "이미 존재하는 폴더 이름입니다" }

        val folder = Folder(
            name = name,
            type = FolderType.USER
        )
        folderRepository.createFolder(folder)
    }
}
