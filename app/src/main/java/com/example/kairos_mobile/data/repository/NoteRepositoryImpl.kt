package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.local.database.dao.NoteDao
import com.example.kairos_mobile.data.mapper.NoteMapper
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.NoteFolder
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Note Repository 구현체 (PRD v4.0)
 */
@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            NoteMapper.toDomainList(entities)
        }
    }

    override fun getNotesByFolder(folder: NoteFolder): Flow<List<Note>> {
        return noteDao.getNotesByFolder(folder.name).map { entities ->
            NoteMapper.toDomainList(entities)
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query).map { entities ->
            NoteMapper.toDomainList(entities)
        }
    }

    override fun getNotesByTag(tag: String): Flow<List<Note>> {
        return noteDao.getNotesByTag(tag).map { entities ->
            NoteMapper.toDomainList(entities)
        }
    }

    override fun getRecentNotes(limit: Int): Flow<List<Note>> {
        return noteDao.getRecentNotes(limit).map { entities ->
            NoteMapper.toDomainList(entities)
        }
    }

    override suspend fun createNote(note: Note): Result<Note> {
        return try {
            val entity = NoteMapper.toEntity(note)
            noteDao.insert(entity)
            Result.Success(note)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun createNoteFromInsight(
        insightId: String,
        title: String,
        content: String,
        folder: NoteFolder,
        tags: List<String>
    ): Result<Note> {
        return try {
            // 이미 해당 인사이트로 생성된 노트가 있는지 확인
            val existing = noteDao.getNoteByInsightId(insightId)
            if (existing != null) {
                return Result.Success(NoteMapper.toDomain(existing))
            }

            val now = Instant.now()
            val note = Note(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                folder = folder,
                tags = tags,
                sourceInsightId = insightId,
                createdAt = now,
                updatedAt = now
            )

            val entity = NoteMapper.toEntity(note)
            noteDao.insert(entity)
            Result.Success(note)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateNote(note: Note): Result<Note> {
        return try {
            val updated = note.copy(updatedAt = Instant.now())
            val entity = NoteMapper.toEntity(updated)
            noteDao.update(entity)
            Result.Success(updated)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteNote(id: String): Result<Unit> {
        return try {
            noteDao.deleteById(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getNoteById(id: String): Result<Note?> {
        return try {
            val entity = noteDao.getById(id)
            Result.Success(entity?.let { NoteMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun moveToFolder(id: String, newFolder: NoteFolder): Result<Unit> {
        return try {
            noteDao.moveToFolder(id, newFolder.name, System.currentTimeMillis())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getNoteCount(): Flow<Int> {
        return noteDao.getNoteCount()
    }

    override fun getNoteCountByFolder(folder: NoteFolder): Flow<Int> {
        return noteDao.getNoteCountByFolder(folder.name)
    }
}
