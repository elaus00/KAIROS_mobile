package com.example.kairos_mobile.domain.usecase.classification

import com.example.kairos_mobile.domain.model.ClassificationLog
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.Todo
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ClassificationLogRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.repository.TodoRepository
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
import com.example.kairos_mobile.domain.usecase.calendar.SyncScheduleToCalendarUseCase
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
    private val noteRepository: NoteRepository,
    private val classificationLogRepository: ClassificationLogRepository,
    private val trackEventUseCase: TrackEventUseCase,
    private val syncScheduleToCalendarUseCase: SyncScheduleToCalendarUseCase
) {
    suspend operator fun invoke(
        captureId: String,
        newType: ClassifiedType,
        newSubType: NoteSubType? = null
    ) {
        // 0. 기존 캡처 정보 조회 (로깅용)
        val capture = captureRepository.getCaptureById(captureId)
            ?: throw IllegalArgumentException("캡처를 찾을 수 없습니다: $captureId")

        val originalType = capture.classifiedType
        val originalSubType = capture.noteSubType

        // AI 분류 완료 후 수정까지 걸린 시간 계산
        val timeSinceClassificationMs = capture.classificationCompletedAt?.let { completedAt ->
            System.currentTimeMillis() - completedAt
        }

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
                val schedule = Schedule(captureId = captureId)
                scheduleRepository.createSchedule(schedule)
                syncScheduleToCalendarUseCase(schedule.id)
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

        // 4. 분류 수정 로그 기록
        val log = ClassificationLog(
            captureId = captureId,
            originalType = originalType,
            originalSubType = originalSubType,
            newType = newType,
            newSubType = newSubType,
            timeSinceClassificationMs = timeSinceClassificationMs
        )
        classificationLogRepository.insert(log)

        // 5. 분석 이벤트 추적
        trackEventUseCase(
            eventType = "classification_modified",
            eventData = "from=${originalType.name},to=${newType.name}"
        )
    }
}
