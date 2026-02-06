package com.example.kairos_mobile.domain.usecase.classification

import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.repository.TodoRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 분류 유형 변경 UseCase
 * @Transaction: 기존 파생 엔티티 삭제 → type 변경 → 새 파생 엔티티 생성
 */
@Singleton
class ChangeClassificationUseCase @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val todoRepository: TodoRepository,
    private val scheduleRepository: ScheduleRepository,
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(
        captureId: String,
        newType: ClassifiedType,
        newSubType: NoteSubType? = null
    ) {
        // 1. 기존 파생 엔티티 삭제
        todoRepository.deleteByCaptureId(captureId)
        scheduleRepository.deleteByCaptureId(captureId)
        noteRepository.deleteByCaptureId(captureId)

        // 2. 분류 유형 변경
        captureRepository.updateClassifiedType(captureId, newType, newSubType)

        // 3. 새 파생 엔티티 생성
        when (newType) {
            ClassifiedType.TODO -> {
                todoRepository.createTodo(Todo(captureId = captureId))
            }
            ClassifiedType.SCHEDULE -> {
                scheduleRepository.createSchedule(Schedule(captureId = captureId))
            }
            ClassifiedType.NOTES -> {
                val folderId = when (newSubType) {
                    NoteSubType.IDEA -> Folder.SYSTEM_IDEAS_ID
                    NoteSubType.BOOKMARK -> Folder.SYSTEM_BOOKMARKS_ID
                    else -> Folder.SYSTEM_INBOX_ID
                }
                noteRepository.createNote(Note(captureId = captureId, folderId = folderId))
            }
            ClassifiedType.TEMP -> {
                // TEMP는 파생 엔티티 없음
            }
        }
    }
}
