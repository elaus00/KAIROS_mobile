package com.example.kairos_mobile.domain.usecase.classification

import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ExtractedEntityRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.repository.TagRepository
import com.example.kairos_mobile.domain.repository.TodoRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 분류 결과 처리 UseCase
 * Capture 업데이트 + 파생 엔티티(Todo/Schedule/Note) 생성 + 태그 연결
 */
@Singleton
class ProcessClassificationResultUseCase @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val extractedEntityRepository: ExtractedEntityRepository,
    private val todoRepository: TodoRepository,
    private val scheduleRepository: ScheduleRepository,
    private val noteRepository: NoteRepository,
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(captureId: String, classification: Classification) {
        // 1. Capture 분류 결과 업데이트
        captureRepository.updateClassification(
            captureId = captureId,
            classifiedType = classification.type,
            noteSubType = classification.subType,
            aiTitle = classification.aiTitle,
            confidence = classification.confidence
        )

        // 2. 추출 엔티티 저장 (기존값 교체)
        extractedEntityRepository.replaceForCapture(
            captureId = captureId,
            entities = classification.entities
        )

        // 3. 태그 연결
        for (tagName in classification.tags) {
            val tag = tagRepository.getOrCreate(tagName)
            tagRepository.linkTagToCapture(captureId, tag.id)
        }

        // 4. 파생 엔티티 생성
        when (classification.type) {
            ClassifiedType.TODO -> {
                val todo = Todo(
                    captureId = captureId,
                    deadline = classification.todoInfo?.deadline
                )
                todoRepository.createTodo(todo)
            }
            ClassifiedType.SCHEDULE -> {
                val info = classification.scheduleInfo
                val schedule = Schedule(
                    captureId = captureId,
                    startTime = info?.startTime,
                    endTime = info?.endTime,
                    location = info?.location,
                    isAllDay = info?.isAllDay ?: false,
                    confidence = classification.confidence
                )
                scheduleRepository.createSchedule(schedule)
            }
            ClassifiedType.NOTES -> {
                val folderId = mapNoteSubTypeToFolderId(classification.subType)
                val note = Note(
                    captureId = captureId,
                    folderId = folderId
                )
                noteRepository.createNote(note)
            }
            ClassifiedType.TEMP -> {
                // TEMP는 파생 엔티티 없음
            }
        }
    }

    /**
     * NoteSubType → 시스템 폴더 ID 매핑
     */
    private fun mapNoteSubTypeToFolderId(subType: NoteSubType?): String {
        return when (subType) {
            NoteSubType.IDEA -> Folder.SYSTEM_IDEAS_ID
            NoteSubType.BOOKMARK -> Folder.SYSTEM_BOOKMARKS_ID
            NoteSubType.USER_FOLDER -> Folder.SYSTEM_INBOX_ID
            NoteSubType.INBOX, null -> Folder.SYSTEM_INBOX_ID
        }
    }
}
