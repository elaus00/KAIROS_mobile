package com.flit.app.domain.usecase.classification

import app.cash.turbine.test
import com.flit.app.domain.model.Capture
import com.flit.app.domain.model.CaptureSource
import com.flit.app.domain.model.Classification
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.ConfidenceLevel
import com.flit.app.domain.model.EntityType
import com.flit.app.domain.model.ExtractedEntity
import com.flit.app.domain.model.Folder
import com.flit.app.domain.model.NoteSubType
import com.flit.app.domain.model.ScheduleInfo
import com.flit.app.domain.model.SplitItem
import com.flit.app.domain.model.Tag
import com.flit.app.domain.model.TodoInfo
import com.flit.app.domain.repository.AnalyticsRepository
import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.repository.ClassificationLogRepository
import com.flit.app.domain.repository.ExtractedEntityRepository
import com.flit.app.domain.repository.NoteRepository
import com.flit.app.domain.repository.ScheduleRepository
import com.flit.app.domain.repository.TagRepository
import com.flit.app.domain.repository.TodoRepository
import com.flit.app.domain.repository.TransactionRunner
import com.flit.app.domain.usecase.analytics.TrackEventUseCase
import com.flit.app.domain.usecase.calendar.SyncScheduleToCalendarUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ClassificationUseCasesTest {

    @Test
    fun changeClassification_toTodo_deletes_old_entities_and_creates_todo() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val classificationLogRepository = mockk<ClassificationLogRepository>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val syncScheduleToCalendarUseCase = mockk<SyncScheduleToCalendarUseCase>(relaxed = true)
        val useCase = ChangeClassificationUseCase(
            captureRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            classificationLogRepository,
            trackEventUseCase,
            syncScheduleToCalendarUseCase
        )

        val mockCapture = Capture(id = "cap-1", originalText = "test", classifiedType = ClassifiedType.NOTES)
        coEvery { captureRepository.getCaptureById("cap-1") } returns mockCapture
        coEvery { todoRepository.deleteByCaptureId("cap-1") } just runs
        coEvery { scheduleRepository.deleteByCaptureId("cap-1") } just runs
        coEvery { noteRepository.deleteByCaptureId("cap-1") } just runs
        coEvery { captureRepository.updateClassifiedType("cap-1", ClassifiedType.TODO, null) } just runs
        coEvery { todoRepository.createTodo(any()) } just runs
        coEvery { classificationLogRepository.insert(any()) } just runs
        coEvery { trackEventUseCase(any(), any()) } just runs

        useCase("cap-1", ClassifiedType.TODO)

        coVerify {
            captureRepository.getCaptureById("cap-1")
            todoRepository.deleteByCaptureId("cap-1")
            scheduleRepository.deleteByCaptureId("cap-1")
            noteRepository.deleteByCaptureId("cap-1")
            captureRepository.updateClassifiedType("cap-1", ClassifiedType.TODO, null)
            todoRepository.createTodo(match { it.captureId == "cap-1" })
            classificationLogRepository.insert(any())
            trackEventUseCase(any(), any())
        }
    }

    @Test
    fun changeClassification_toSchedule_creates_schedule_entity() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val classificationLogRepository = mockk<ClassificationLogRepository>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val syncScheduleToCalendarUseCase = mockk<SyncScheduleToCalendarUseCase>(relaxed = true)
        val useCase = ChangeClassificationUseCase(
            captureRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            classificationLogRepository,
            trackEventUseCase,
            syncScheduleToCalendarUseCase
        )

        val mockCapture = Capture(id = "cap-1", originalText = "test", classifiedType = ClassifiedType.TODO)
        coEvery { captureRepository.getCaptureById("cap-1") } returns mockCapture
        coEvery { todoRepository.deleteByCaptureId(any()) } just runs
        coEvery { scheduleRepository.deleteByCaptureId(any()) } just runs
        coEvery { noteRepository.deleteByCaptureId(any()) } just runs
        coEvery {
            captureRepository.updateClassifiedType("cap-1", ClassifiedType.SCHEDULE, null)
        } just runs
        coEvery { scheduleRepository.createSchedule(any()) } just runs
        coEvery { classificationLogRepository.insert(any()) } just runs
        coEvery { trackEventUseCase(any(), any()) } just runs

        useCase("cap-1", ClassifiedType.SCHEDULE)

        coVerify { scheduleRepository.createSchedule(match { it.captureId == "cap-1" }) }
        coVerify(exactly = 1) { syncScheduleToCalendarUseCase(any()) }
    }

    @Test
    fun changeClassification_toNotes_maps_subtype_to_expected_folder() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val classificationLogRepository = mockk<ClassificationLogRepository>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val syncScheduleToCalendarUseCase = mockk<SyncScheduleToCalendarUseCase>(relaxed = true)
        val useCase = ChangeClassificationUseCase(
            captureRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            classificationLogRepository,
            trackEventUseCase,
            syncScheduleToCalendarUseCase
        )

        val mockCapture = Capture(id = "cap-1", originalText = "test", classifiedType = ClassifiedType.TODO)
        coEvery { captureRepository.getCaptureById("cap-1") } returns mockCapture
        coEvery { todoRepository.deleteByCaptureId(any()) } just runs
        coEvery { scheduleRepository.deleteByCaptureId(any()) } just runs
        coEvery { noteRepository.deleteByCaptureId(any()) } just runs
        coEvery {
            captureRepository.updateClassifiedType("cap-1", ClassifiedType.NOTES, NoteSubType.BOOKMARK)
        } just runs
        coEvery { noteRepository.createNote(any()) } just runs
        coEvery { classificationLogRepository.insert(any()) } just runs
        coEvery { trackEventUseCase(any(), any()) } just runs

        useCase("cap-1", ClassifiedType.NOTES, NoteSubType.BOOKMARK)

        coVerify {
            noteRepository.createNote(match {
                it.captureId == "cap-1" && it.folderId == Folder.SYSTEM_BOOKMARKS_ID
            })
        }
    }

    @Test
    fun changeClassification_toNotes_without_subtype_falls_back_to_inbox() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val classificationLogRepository = mockk<ClassificationLogRepository>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val syncScheduleToCalendarUseCase = mockk<SyncScheduleToCalendarUseCase>(relaxed = true)
        val useCase = ChangeClassificationUseCase(
            captureRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            classificationLogRepository,
            trackEventUseCase,
            syncScheduleToCalendarUseCase
        )

        val mockCapture = Capture(id = "cap-1", originalText = "test", classifiedType = ClassifiedType.TODO)
        coEvery { captureRepository.getCaptureById("cap-1") } returns mockCapture
        coEvery { todoRepository.deleteByCaptureId(any()) } just runs
        coEvery { scheduleRepository.deleteByCaptureId(any()) } just runs
        coEvery { noteRepository.deleteByCaptureId(any()) } just runs
        coEvery {
            captureRepository.updateClassifiedType("cap-1", ClassifiedType.NOTES, null)
        } just runs
        coEvery { noteRepository.createNote(any()) } just runs
        coEvery { classificationLogRepository.insert(any()) } just runs
        coEvery { trackEventUseCase(any(), any()) } just runs

        useCase("cap-1", ClassifiedType.NOTES)

        coVerify {
            noteRepository.createNote(match {
                it.captureId == "cap-1" && it.folderId == Folder.SYSTEM_INBOX_ID
            })
        }
    }

    @Test
    fun changeClassification_toTemp_creates_no_derived_entity() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val classificationLogRepository = mockk<ClassificationLogRepository>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val syncScheduleToCalendarUseCase = mockk<SyncScheduleToCalendarUseCase>(relaxed = true)
        val useCase = ChangeClassificationUseCase(
            captureRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            classificationLogRepository,
            trackEventUseCase,
            syncScheduleToCalendarUseCase
        )

        val mockCapture = Capture(id = "cap-1", originalText = "test", classifiedType = ClassifiedType.NOTES)
        coEvery { captureRepository.getCaptureById("cap-1") } returns mockCapture
        coEvery { todoRepository.deleteByCaptureId(any()) } just runs
        coEvery { scheduleRepository.deleteByCaptureId(any()) } just runs
        coEvery { noteRepository.deleteByCaptureId(any()) } just runs
        coEvery {
            captureRepository.updateClassifiedType("cap-1", ClassifiedType.TEMP, null)
        } just runs
        coEvery { classificationLogRepository.insert(any()) } just runs
        coEvery { trackEventUseCase(any(), any()) } just runs

        useCase("cap-1", ClassifiedType.TEMP)

        coVerify(exactly = 0) { todoRepository.createTodo(any()) }
        coVerify(exactly = 0) { scheduleRepository.createSchedule(any()) }
        coVerify(exactly = 0) { noteRepository.createNote(any()) }
    }

    @Test
    fun confirmClassification_delegates_to_capture_repository() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val confirmOne = ConfirmClassificationUseCase(captureRepository, trackEventUseCase)

        val capture = Capture(id = "cap-1", originalText = "test", classifiedType = ClassifiedType.TODO)
        coEvery { captureRepository.getCaptureById("cap-1") } returns capture
        coEvery { captureRepository.confirmClassification("cap-1") } just runs
        coEvery { trackEventUseCase(any(), any()) } just runs

        confirmOne("cap-1")

        coVerify(exactly = 1) { captureRepository.confirmClassification("cap-1") }
        coVerify(exactly = 1) {
            trackEventUseCase(
                "classification_confirmed",
                match { it != null && it.contains("cap-1") && it.contains("TODO") }
            )
        }
    }

    @Test
    fun getUnconfirmedUseCase_exposes_list_and_count_flows() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val useCase = GetUnconfirmedClassificationsUseCase(captureRepository)

        every { captureRepository.getUnconfirmedClassifications() } returns flowOf(emptyList())
        every { captureRepository.getUnconfirmedCount() } returns flowOf(3)

        useCase().test {
            assertEquals(emptyList<Any>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        useCase.count().test {
            assertEquals(3, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun processClassification_todo_updates_capture_entities_tags_and_todo() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val extractedRepository = mockk<ExtractedEntityRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val tagRepository = mockk<TagRepository>()
        val transactionRunner = mockk<TransactionRunner>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val syncScheduleToCalendarUseCase = mockk<SyncScheduleToCalendarUseCase>(relaxed = true)
        val useCase = ProcessClassificationResultUseCase(
            captureRepository,
            extractedRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            tagRepository,
            transactionRunner,
            trackEventUseCase,
            syncScheduleToCalendarUseCase
        )

        val entity = ExtractedEntity(
            captureId = "cap-1",
            type = EntityType.DATE,
            value = "tomorrow"
        )
        val classification = Classification(
            type = ClassifiedType.TODO,
            confidence = ConfidenceLevel.HIGH,
            aiTitle = "Buy milk",
            tags = listOf("shopping", "urgent"),
            entities = listOf(entity),
            todoInfo = TodoInfo(deadline = 1234L)
        )

        coEvery {
            captureRepository.updateClassification(
                captureId = "cap-1",
                classifiedType = classification.type,
                noteSubType = classification.subType,
                aiTitle = classification.aiTitle,
                confidence = classification.confidence
            )
        } just runs
        coEvery { extractedRepository.replaceForCapture("cap-1", classification.entities) } just runs
        coEvery { tagRepository.getOrCreate("shopping") } returns Tag(id = "t1", name = "shopping")
        coEvery { tagRepository.getOrCreate("urgent") } returns Tag(id = "t2", name = "urgent")
        coEvery { tagRepository.linkTagToCapture(any(), any()) } just runs
        coEvery { todoRepository.createTodo(any()) } just runs
        coEvery { transactionRunner.runInTransaction(any()) } coAnswers {
            firstArg<suspend () -> Unit>().invoke()
        }
        coEvery { trackEventUseCase(any(), any()) } just runs

        useCase("cap-1", classification)

        coVerify(exactly = 1) {
            captureRepository.updateClassification(
                captureId = "cap-1",
                classifiedType = ClassifiedType.TODO,
                noteSubType = null,
                aiTitle = "Buy milk",
                confidence = ConfidenceLevel.HIGH
            )
        }
        coVerify(exactly = 1) { extractedRepository.replaceForCapture("cap-1", listOf(entity)) }
        coVerify(exactly = 1) { tagRepository.linkTagToCapture("cap-1", "t1") }
        coVerify(exactly = 1) { tagRepository.linkTagToCapture("cap-1", "t2") }
        coVerify(exactly = 1) {
            todoRepository.createTodo(match {
                it.captureId == "cap-1" && it.deadline == 1234L
            })
        }
        coVerify(exactly = 0) { scheduleRepository.createSchedule(any()) }
        coVerify(exactly = 0) { noteRepository.createNote(any()) }
    }

    @Test
    fun processClassification_schedule_creates_schedule_with_values() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val extractedRepository = mockk<ExtractedEntityRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val tagRepository = mockk<TagRepository>()
        val transactionRunner = mockk<TransactionRunner>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val syncScheduleToCalendarUseCase = mockk<SyncScheduleToCalendarUseCase>(relaxed = true)
        val useCase = ProcessClassificationResultUseCase(
            captureRepository,
            extractedRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            tagRepository,
            transactionRunner,
            trackEventUseCase,
            syncScheduleToCalendarUseCase
        )

        val classification = Classification(
            type = ClassifiedType.SCHEDULE,
            confidence = ConfidenceLevel.MEDIUM,
            aiTitle = "Meeting",
            scheduleInfo = ScheduleInfo(
                startTime = 1000L,
                endTime = 2000L,
                location = "Office",
                isAllDay = false
            )
        )

        coEvery { captureRepository.updateClassification(any(), any(), any(), any(), any()) } just runs
        coEvery { extractedRepository.replaceForCapture(any(), any()) } just runs
        coEvery { scheduleRepository.createSchedule(any()) } just runs
        coEvery { transactionRunner.runInTransaction(any()) } coAnswers {
            firstArg<suspend () -> Unit>().invoke()
        }
        coEvery { trackEventUseCase(any(), any()) } just runs

        useCase("cap-1", classification)

        coVerify(exactly = 1) {
            scheduleRepository.createSchedule(match {
                it.captureId == "cap-1" &&
                    it.startTime == 1000L &&
                    it.endTime == 2000L &&
                    it.location == "Office" &&
                    !it.isAllDay
            })
        }
        coVerify(exactly = 1) { syncScheduleToCalendarUseCase(any()) }
    }

    @Test
    fun processClassification_notes_maps_bookmark_and_user_folder_subtypes() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val extractedRepository = mockk<ExtractedEntityRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val tagRepository = mockk<TagRepository>()
        val transactionRunner = mockk<TransactionRunner>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val syncScheduleToCalendarUseCase = mockk<SyncScheduleToCalendarUseCase>(relaxed = true)
        val useCase = ProcessClassificationResultUseCase(
            captureRepository,
            extractedRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            tagRepository,
            transactionRunner,
            trackEventUseCase,
            syncScheduleToCalendarUseCase
        )

        val bookmarkClassification = Classification(
            type = ClassifiedType.NOTES,
            subType = NoteSubType.BOOKMARK,
            confidence = ConfidenceLevel.LOW,
            aiTitle = "Article"
        )
        val userFolderClassification = Classification(
            type = ClassifiedType.NOTES,
            subType = NoteSubType.USER_FOLDER,
            confidence = ConfidenceLevel.LOW,
            aiTitle = "Memo"
        )

        coEvery { captureRepository.updateClassification(any(), any(), any(), any(), any()) } just runs
        coEvery { extractedRepository.replaceForCapture(any(), any()) } just runs
        coEvery { noteRepository.createNote(any()) } just runs
        coEvery { transactionRunner.runInTransaction(any()) } coAnswers {
            firstArg<suspend () -> Unit>().invoke()
        }
        coEvery { trackEventUseCase(any(), any()) } just runs

        useCase("cap-b", bookmarkClassification)
        useCase("cap-u", userFolderClassification)

        coVerify {
            noteRepository.createNote(match {
                it.captureId == "cap-b" && it.folderId == Folder.SYSTEM_BOOKMARKS_ID
            })
        }
        coVerify {
            noteRepository.createNote(match {
                it.captureId == "cap-u" && it.folderId == Folder.SYSTEM_INBOX_ID
            })
        }
    }

    @Test
    fun processClassification_splitItems_creates_child_captures_and_derived_entities() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val extractedRepository = mockk<ExtractedEntityRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val tagRepository = mockk<TagRepository>()
        val transactionRunner = mockk<TransactionRunner>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val syncScheduleToCalendarUseCase = mockk<SyncScheduleToCalendarUseCase>(relaxed = true)
        val useCase = ProcessClassificationResultUseCase(
            captureRepository,
            extractedRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            tagRepository,
            transactionRunner,
            trackEventUseCase,
            syncScheduleToCalendarUseCase
        )

        // 2개 분할 아이템: TODO + SCHEDULE
        val splitItems = listOf(
            SplitItem(
                splitText = "우유 사기",
                classifiedType = ClassifiedType.TODO,
                confidence = ConfidenceLevel.HIGH,
                aiTitle = "우유 구매",
                todoInfo = TodoInfo(deadline = 5000L)
            ),
            SplitItem(
                splitText = "저녁 약속",
                classifiedType = ClassifiedType.SCHEDULE,
                confidence = ConfidenceLevel.MEDIUM,
                aiTitle = "저녁 식사",
                scheduleInfo = ScheduleInfo(startTime = 6000L, endTime = 8000L)
            )
        )
        val classification = Classification(
            type = ClassifiedType.NOTES,
            confidence = ConfidenceLevel.HIGH,
            aiTitle = "복합 입력",
            splitItems = splitItems
        )

        coEvery { captureRepository.updateClassification(any(), any(), any(), any(), any()) } just runs
        coEvery { captureRepository.saveCapture(any()) } answers { firstArg() }
        coEvery { extractedRepository.replaceForCapture(any(), any()) } just runs
        coEvery { todoRepository.createTodo(any()) } just runs
        coEvery { scheduleRepository.createSchedule(any()) } just runs
        coEvery { transactionRunner.runInTransaction(any()) } coAnswers {
            firstArg<suspend () -> Unit>().invoke()
        }
        coEvery { trackEventUseCase(any(), any()) } just runs

        useCase("cap-1", classification)

        // 부모 캡처 분류 업데이트
        coVerify {
            captureRepository.updateClassification(
                captureId = "cap-1",
                classifiedType = ClassifiedType.NOTES,
                noteSubType = null,
                aiTitle = "복합 입력",
                confidence = ConfidenceLevel.HIGH
            )
        }

        // 자식 캡처 2개 생성 (source = SPLIT, parentCaptureId = "cap-1")
        coVerify(exactly = 2) {
            captureRepository.saveCapture(match {
                it.source == CaptureSource.SPLIT && it.parentCaptureId == "cap-1"
            })
        }

        // 자식별 분류 업데이트 (2회) + 부모 (1회) = 총 3회
        coVerify(exactly = 3) {
            captureRepository.updateClassification(any(), any(), any(), any(), any())
        }

        // 부모 캡처 추출 엔티티 저장 호출
        coVerify(exactly = 1) { extractedRepository.replaceForCapture("cap-1", emptyList()) }

        // 파생 엔티티 생성: TODO 1개 + SCHEDULE 1개
        coVerify(exactly = 1) { todoRepository.createTodo(match { it.deadline == 5000L }) }
        coVerify(exactly = 1) {
            scheduleRepository.createSchedule(match {
                it.startTime == 6000L && it.endTime == 8000L
            })
        }

        // 분석 이벤트: split_capture_created
        coVerify {
            trackEventUseCase(
                "split_capture_created",
                match { it.contains("cap-1") && it.contains("2") }
            )
        }
    }

    @Test
    fun processClassification_temp_creates_no_derived_entities() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val extractedRepository = mockk<ExtractedEntityRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val tagRepository = mockk<TagRepository>()
        val transactionRunner = mockk<TransactionRunner>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val syncScheduleToCalendarUseCase = mockk<SyncScheduleToCalendarUseCase>(relaxed = true)
        val useCase = ProcessClassificationResultUseCase(
            captureRepository,
            extractedRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            tagRepository,
            transactionRunner,
            trackEventUseCase,
            syncScheduleToCalendarUseCase
        )

        val classification = Classification(
            type = ClassifiedType.TEMP,
            confidence = ConfidenceLevel.MEDIUM,
            aiTitle = "Temp"
        )

        coEvery { captureRepository.updateClassification(any(), any(), any(), any(), any()) } just runs
        coEvery { extractedRepository.replaceForCapture(any(), any()) } just runs
        coEvery { transactionRunner.runInTransaction(any()) } coAnswers {
            firstArg<suspend () -> Unit>().invoke()
        }
        coEvery { trackEventUseCase(any(), any()) } just runs

        useCase("cap-1", classification)

        coVerify(exactly = 0) { todoRepository.createTodo(any()) }
        coVerify(exactly = 0) { scheduleRepository.createSchedule(any()) }
        coVerify(exactly = 0) { noteRepository.createNote(any()) }
    }
}
