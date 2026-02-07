package com.example.kairos_mobile.domain.usecase.classification

import app.cash.turbine.test
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.ConfidenceLevel
import com.example.kairos_mobile.domain.model.EntityType
import com.example.kairos_mobile.domain.model.ExtractedEntity
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.model.ScheduleInfo
import com.example.kairos_mobile.domain.model.Tag
import com.example.kairos_mobile.domain.model.TodoInfo
import com.example.kairos_mobile.domain.repository.AnalyticsRepository
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ClassificationLogRepository
import com.example.kairos_mobile.domain.repository.ExtractedEntityRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.repository.TagRepository
import com.example.kairos_mobile.domain.repository.TodoRepository
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
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
        val useCase = ChangeClassificationUseCase(
            captureRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            classificationLogRepository,
            trackEventUseCase
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
        val useCase = ChangeClassificationUseCase(
            captureRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            classificationLogRepository,
            trackEventUseCase
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
    }

    @Test
    fun changeClassification_toNotes_maps_subtype_to_expected_folder() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val classificationLogRepository = mockk<ClassificationLogRepository>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val useCase = ChangeClassificationUseCase(
            captureRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            classificationLogRepository,
            trackEventUseCase
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
        val useCase = ChangeClassificationUseCase(
            captureRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            classificationLogRepository,
            trackEventUseCase
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
        val useCase = ChangeClassificationUseCase(
            captureRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            classificationLogRepository,
            trackEventUseCase
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
    fun confirmUseCases_delegate_to_capture_repository() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val confirmOne = ConfirmClassificationUseCase(captureRepository, trackEventUseCase)
        val confirmAll = ConfirmAllClassificationsUseCase(captureRepository)

        coEvery { captureRepository.confirmClassification("cap-1") } just runs
        coEvery { captureRepository.confirmAllClassifications() } just runs
        coEvery { trackEventUseCase(any(), any()) } just runs

        confirmOne("cap-1")
        confirmAll()

        coVerify(exactly = 1) { captureRepository.confirmClassification("cap-1") }
        coVerify(exactly = 1) { captureRepository.confirmAllClassifications() }
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
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val useCase = ProcessClassificationResultUseCase(
            captureRepository,
            extractedRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            tagRepository,
            trackEventUseCase
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
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val useCase = ProcessClassificationResultUseCase(
            captureRepository,
            extractedRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            tagRepository,
            trackEventUseCase
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
    }

    @Test
    fun processClassification_notes_maps_bookmark_and_user_folder_subtypes() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val extractedRepository = mockk<ExtractedEntityRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val tagRepository = mockk<TagRepository>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val useCase = ProcessClassificationResultUseCase(
            captureRepository,
            extractedRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            tagRepository,
            trackEventUseCase
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
    fun processClassification_temp_creates_no_derived_entities() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val extractedRepository = mockk<ExtractedEntityRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val tagRepository = mockk<TagRepository>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val useCase = ProcessClassificationResultUseCase(
            captureRepository,
            extractedRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            tagRepository,
            trackEventUseCase
        )

        val classification = Classification(
            type = ClassifiedType.TEMP,
            confidence = ConfidenceLevel.MEDIUM,
            aiTitle = "Temp"
        )

        coEvery { captureRepository.updateClassification(any(), any(), any(), any(), any()) } just runs
        coEvery { extractedRepository.replaceForCapture(any(), any()) } just runs
        coEvery { trackEventUseCase(any(), any()) } just runs

        useCase("cap-1", classification)

        coVerify(exactly = 0) { todoRepository.createTodo(any()) }
        coVerify(exactly = 0) { scheduleRepository.createSchedule(any()) }
        coVerify(exactly = 0) { noteRepository.createNote(any()) }
    }
}
