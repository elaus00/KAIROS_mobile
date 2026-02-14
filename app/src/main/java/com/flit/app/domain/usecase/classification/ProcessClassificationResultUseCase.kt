package com.flit.app.domain.usecase.classification

import com.flit.app.domain.model.Capture
import com.flit.app.domain.model.CaptureSource
import com.flit.app.domain.model.Classification
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.Folder
import com.flit.app.domain.model.Note
import com.flit.app.domain.model.NoteSubType
import com.flit.app.domain.model.Schedule
import com.flit.app.domain.model.Todo
import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.repository.ExtractedEntityRepository
import com.flit.app.domain.repository.NoteRepository
import com.flit.app.domain.repository.ScheduleRepository
import com.flit.app.domain.repository.TagRepository
import com.flit.app.domain.repository.TodoRepository
import com.flit.app.domain.repository.TransactionRunner
import com.flit.app.domain.usecase.analytics.TrackEventUseCase
import com.flit.app.domain.usecase.calendar.SyncScheduleToCalendarUseCase
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
    private val transactionRunner: TransactionRunner,
    private val trackEventUseCase: TrackEventUseCase,
    private val syncScheduleToCalendarUseCase: SyncScheduleToCalendarUseCase
) {
    suspend operator fun invoke(captureId: String, classification: Classification) {
        val scheduleIdsToSync = mutableListOf<String>()
        val analyticsEvents = mutableListOf<Pair<String, String>>()

        transactionRunner.runInTransaction {
            // 멀티 인텐트 분할 처리
            if (!classification.splitItems.isNullOrEmpty()) {
                processSplitItems(captureId, classification, scheduleIdsToSync, analyticsEvents)
                return@runInTransaction
            }

            // 단일 의도 처리
            processSingleIntent(captureId, classification, scheduleIdsToSync, analyticsEvents)
        }

        // 트랜잭션 이후 부수효과 처리
        scheduleIdsToSync.forEach { scheduleId ->
            syncScheduleToCalendarUseCase(scheduleId)
        }
        analyticsEvents.forEach { (eventType, eventData) ->
            trackEventUseCase(eventType = eventType, eventData = eventData)
        }
    }

    /**
     * 멀티 인텐트 분할 처리
     * 각 splitItem에 대해 새 Capture를 생성하고 개별 분류 결과를 적용한다.
     */
    private suspend fun processSplitItems(
        parentCaptureId: String,
        classification: Classification,
        scheduleIdsToSync: MutableList<String>,
        analyticsEvents: MutableList<Pair<String, String>>
    ) {
        // 원본 캡처를 NOTES/INBOX로 업데이트 (부모 역할)
        captureRepository.updateClassification(
            captureId = parentCaptureId,
            classifiedType = classification.type,
            noteSubType = classification.subType,
            aiTitle = classification.aiTitle,
            confidence = classification.confidence
        )

        // 부모 캡처 추출 엔티티 저장 (split 응답에도 엔티티가 있을 수 있음)
        extractedEntityRepository.replaceForCapture(
            captureId = parentCaptureId,
            entities = classification.entities
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
            processSingleIntent(
                captureId = childCapture.id,
                classification = childClassification,
                scheduleIdsToSync = scheduleIdsToSync,
                analyticsEvents = analyticsEvents
            )
        }

        // 분할 캡처 생성 분석 이벤트
        analyticsEvents += "split_capture_created" to
            """{"parent_capture_id":"$parentCaptureId","split_count":${classification.splitItems.size}}"""
    }

    /**
     * 단일 의도 분류 결과 처리
     */
    private suspend fun processSingleIntent(
        captureId: String,
        classification: Classification,
        scheduleIdsToSync: MutableList<String>,
        analyticsEvents: MutableList<Pair<String, String>>
    ) {
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
                scheduleIdsToSync += schedule.id
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
        analyticsEvents += "classification_completed" to
            "type=${classification.type.name},confidence=${classification.confidence.name}"
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
