package com.example.kairos_mobile.domain.usecase.folder

import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 전체 폴더 조회 UseCase
 */
@Singleton
class GetAllFoldersUseCase @Inject constructor(
    private val folderRepository: FolderRepository
) {
    operator fun invoke(): Flow<List<Folder>> {
        return folderRepository.getAllFolders()
    }
}
