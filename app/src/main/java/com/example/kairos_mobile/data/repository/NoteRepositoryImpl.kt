package com.example.kairos_mobile.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.kairos_mobile.data.local.database.dao.CaptureDao
import com.example.kairos_mobile.data.local.database.dao.CaptureTagDao
import com.example.kairos_mobile.data.local.database.dao.FolderDao
import com.example.kairos_mobile.data.local.database.dao.NoteDao
import com.example.kairos_mobile.data.mapper.NoteMapper
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.NoteAiInput
import com.example.kairos_mobile.domain.model.NoteDetail
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.model.NoteWithCapturePreview
import com.example.kairos_mobile.domain.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 노트 Repository 구현체
 */
@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val captureDao: CaptureDao,
    private val folderDao: FolderDao,
    private val captureTagDao: CaptureTagDao,
    private val noteMapper: NoteMapper
) : NoteRepository {
    companion object {
        private const val TAG = "NoteRepository"
    }

    override suspend fun createNote(note: Note) {
        // 캡처가 없으면 FK 위반이므로 노트 생성을 스킵한다.
        if (captureDao.getById(note.captureId) == null) {
            Log.w(TAG, "createNote skipped: capture not found (${note.captureId})")
            return
        }

        // 폴더 FK가 깨진 경우에는 null로 저장하여 노트 데이터 손실을 방지한다.
        val safeFolderId = note.folderId?.takeIf { folderId ->
            folderDao.getById(folderId) != null
        }
        if (note.folderId != null && safeFolderId == null) {
            Log.w(TAG, "createNote fallback: folder not found (${note.folderId})")
        }

        try {
            noteDao.insert(noteMapper.toEntity(note.copy(folderId = safeFolderId)))
        } catch (e: SQLiteConstraintException) {
            // 분류 처리 중 캡처가 삭제되는 경쟁 상태를 앱 크래시 없이 흡수한다.
            Log.w(TAG, "createNote skipped due to FK constraint (captureId=${note.captureId})", e)
        }
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
            .map { rows -> rows.map { it.toPreview() } }
    }

    override fun getAllNotesWithActiveCapture(): Flow<List<NoteWithCapturePreview>> {
        return noteDao.getAllNotesWithActiveCapture()
            .map { rows -> rows.map { it.toPreview() } }
    }

    /** NoteWithCaptureRow → NoteWithCapturePreview 변환 */
    private fun com.example.kairos_mobile.data.local.database.dao.NoteWithCaptureRow.toPreview() =
        NoteWithCapturePreview(
            noteId = noteId,
            captureId = captureId,
            aiTitle = aiTitle,
            originalText = originalText,
            createdAt = createdAt,
            body = body,
            folderId = folderId,
            noteSubType = noteSubType
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getNoteDetail(noteId: String): Flow<NoteDetail?> {
        return noteDao.getNoteWithCapture(noteId)
            .mapLatest { row ->
                row?.let {
                    val tags = captureTagDao.getTagNamesByCaptureId(it.captureId)
                    NoteDetail(
                        noteId = it.noteId,
                        captureId = it.captureId,
                        aiTitle = it.aiTitle,
                        originalText = it.originalText,
                        body = it.body,
                        classifiedType = try {
                            ClassifiedType.valueOf(it.classifiedType)
                        } catch (_: Exception) {
                            ClassifiedType.TEMP
                        },
                        noteSubType = it.noteSubType?.let { subType ->
                            try {
                                NoteSubType.valueOf(subType)
                            } catch (_: Exception) {
                                null
                            }
                        },
                        folderId = it.folderId,
                        imageUri = it.imageUri,
                        tags = tags,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                }
            }
    }

    override suspend fun updateNoteBody(noteId: String, body: String?) {
        noteDao.updateBody(noteId, body, System.currentTimeMillis())
    }

    override suspend fun getUngroupedNoteIds(): List<String> {
        return noteDao.getUngroupedNoteIds()
    }

    override suspend fun getNoteAiInputs(noteIds: List<String>): List<NoteAiInput> {
        return noteIds.mapNotNull { noteId ->
            val row = noteDao.getNoteWithCapture(noteId).first() ?: return@mapNotNull null
            NoteAiInput(
                captureId = row.captureId,
                aiTitle = row.aiTitle ?: "",
                tags = emptyList(),
                noteSubType = row.noteSubType,
                folderId = row.folderId
            )
        }
    }
}
