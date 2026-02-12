package com.flit.app.data.repository

import androidx.room.withTransaction
import com.flit.app.data.local.database.FlitDatabase
import com.flit.app.data.local.database.dao.CaptureDao
import com.flit.app.data.local.database.dao.FolderDao
import com.flit.app.data.local.database.dao.NoteDao
import com.flit.app.data.mapper.FolderMapper
import com.flit.app.domain.model.Folder
import com.flit.app.domain.model.FolderType
import com.flit.app.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 폴더 Repository 구현체
 */
@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val database: FlitDatabase,
    private val folderDao: FolderDao,
    private val noteDao: NoteDao,
    private val captureDao: CaptureDao,
    private val folderMapper: FolderMapper
) : FolderRepository {

    override fun getAllFolders(): Flow<List<Folder>> {
        return folderDao.getAllFolders()
            .map { entities -> entities.map { folderMapper.toDomain(it) } }
    }

    override suspend fun createFolder(folder: Folder) {
        folderDao.insert(folderMapper.toEntity(folder))
    }

    override suspend fun renameFolder(folderId: String, newName: String) {
        folderDao.rename(folderId, newName)
    }

    override suspend fun deleteFolder(folderId: String) {
        // 소속 노트를 Inbox로 이동 + 캡처 note_sub_type 변경 + 폴더 삭제 (트랜잭션)
        database.withTransaction {
            val now = System.currentTimeMillis()
            captureDao.updateNoteSubTypeByFolderId(folderId, "INBOX", now)
            noteDao.moveAllToInbox(folderId, now)
            folderDao.deleteById(folderId)
        }
    }

    override suspend fun existsByName(name: String): Boolean {
        return folderDao.getByName(name) != null
    }

    override suspend fun getOrCreateFolder(name: String, type: FolderType): Folder {
        // 같은 이름 + 유형의 기존 폴더가 있으면 재사용
        val existing = folderDao.getByNameAndType(name, type.name)
        if (existing != null) {
            return folderMapper.toDomain(existing)
        }
        val folder = Folder(name = name, type = type)
        folderDao.insert(folderMapper.toEntity(folder))
        return folder
    }
}
