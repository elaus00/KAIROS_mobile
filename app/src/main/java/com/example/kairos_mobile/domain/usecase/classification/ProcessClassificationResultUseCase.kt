package com.example.kairos_mobile.domain.usecase.classification

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureSource
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
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
import com.example.kairos_mobile.domain.usecase.calendar.SyncScheduleToCalendarUseCase
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
    private val tagRepository: TagRepository,
    private val trackEventUseCase: TrackEventUseCase,
    private val syncScheduleToCalendarUseCase: SyncScheduleToCalendarUseCase
) {
    suspend operator fun invoke(captureId: String, classification: Classification) {
        // 멀티 인텐트 분할 처리
        if (!classification.splitItems.isNullOrEmpty()) {
            processSplitItems(captureId, classification)
            return
        }

        // 단일 의도 처리
        processSingleIntent(captureId, classification)
    }

    /**
     * 멀티 인텐트 분할 처리
     * 각 splitItem에 대해 새 Capture를 생성하고 개별 분류 결과를 적용한다.
     */
    private suspend fun processSplitItems(parentCaptureId: String, classification: Classification) {
        // 원본 캡처를 NOTES/INBOX로 업데이트 (부모 역할)
        captureRepository.updateClassification(
            captureId = parentCaptureId,
            classifiedType = classification.type,
            noteSubType = classification.subType,
            aiTitle = classification.aiTitle,
            confidence = classification.confidence
        )

        // 각 splitItem에 대해 자식 캡처 생성 + 분류 적용
        for (splitItem in classification.splitItems!!) {
            val childCapture = Capture(
                originalText = splitItem.splitText,
                source = CaptureSource.SPLIT,
                parentCaptureId = parentCaptureId
            )
            captureRepository.saveCapture(childCapture)

            // splitItem을 Classification으로 변환하여 단일 의도 처리
            val childClassification = Classification(
                type = splitItem.classifiedType,
                subType = splitItem.noteSubType,
                confidence = splitItem.confidence,
                aiTitle = splitItem.aiTitle,
                tags = splitItem.tags,
                entities = emptyList(),
                scheduleInfo = splitItem.scheduleInfo,
                todoInfo = splitItem.todoInfo
            )
            processSingleIntent(childCapture.id, childClassification)
        }

        // 분할 캡처 생성 분석 이벤트
        trackEventUseCase(
            eventType = "split_capture_created",
            eventData = """{"parent_capture_id":"$parentCaptureId","split_count":${classification.splitItems.size}}"""
        )
    }

    /**
     * 단일 의도 분류 결과 처리
     */
    private suspend fun processSingleIntent(captureId: String, classification: Classification) {
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
                    deadline = classification.todoInfo?.deadline,
                    deadlineSource = classification.todoInfo?.deadlineSource
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
                syncScheduleToCalendarUseCase(schedule.id)
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

        // 5. 분석 이벤트 추적
        trackEventUseCase(
            eventType = "classification_completed",
            eventData = "type=${classification.type.name},confidence=${classification.confidence.name}"
        )
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
