package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.NoteDao
import com.example.kairos_mobile.data.mapper.NoteMapper
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.NoteWithCapturePreview
import com.example.kairos_mobile.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 노트 Repository 구현체
 */
@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val noteMapper: NoteMapper
) : NoteRepository {

    override suspend fun createNote(note: Note) {
        noteDao.insert(noteMapper.toEntity(note))
    }

    override suspend fun getNoteByCaptureId(captureId: String): Note? {
        return noteDao.getByCaptureId(captureId)?.let { noteMapper.toDomain(it) }
    }

    override fun getNotesByFolderId(folderId: String): Flow<List<Note>> {
        return noteDao.getNotesByFolder(folderId)
            .map { entities -> entities.map { noteMapper.toDomain(it) } }
    }

    override suspend fun moveToFolder(noteId: String, folderId: String) {
        noteDao.moveToFolder(noteId, folderId, System.currentTimeMillis())
    }

    override suspend fun deleteByCaptureId(captureId: String) {
        noteDao.deleteByCaptureId(captureId)
    }

    override fun getNoteCountByFolderId(folderId: String): Flow<Int> {
        return noteDao.getNoteCountByFolder(folderId)
    }

    override fun getFolderNoteCounts(): Flow<Map<String, Int>> {
        return noteDao.getFolderNoteCounts()
            .map { rows ->
                rows.mapNotNull { row ->
                    row.folderId?.let { folderId -> folderId to row.noteCount }
                }.toMap()
            }
    }

    override fun getNotesWithActiveCaptureByFolderId(folderId: String): Flow<List<NoteWithCapturePreview>> {
        return noteDao.getNotesWithActiveCaptureByFolder(folderId)
            .map { rows ->
                rows.map { row ->
                    NoteWithCapturePreview(
                        noteId = row.noteId,
                        captureId = row.captureId,
                        aiTitle = row.aiTitle,
                        originalText = row.originalText,
                        createdAt = row.createdAt
                    )
                }
            }
    }
}
