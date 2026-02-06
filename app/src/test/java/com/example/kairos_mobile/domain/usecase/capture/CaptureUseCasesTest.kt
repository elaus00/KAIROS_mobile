package com.example.kairos_mobile.domain.usecase.capture

import androidx.work.WorkManager
import app.cash.turbine.test
import com.example.kairos_mobile.data.worker.ClassifyCaptureWorker
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.SyncAction
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.repository.SyncQueueRepository
import com.example.kairos_mobile.domain.repository.TagRepository
import com.example.kairos_mobile.domain.repository.TodoRepository
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureUseCasesTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun saveDraft_persists_text_with_expected_key() = runTest {
        val prefs = mockk<UserPreferenceRepository>()
        val useCase = SaveDraftUseCase(prefs)

        coEvery { prefs.setString(any(), any()) } just runs

        useCase("draft")

        coVerify(exactly = 1) { prefs.setString(SaveDraftUseCase.KEY_DRAFT_TEXT, "draft") }
    }

    @Test
    fun getDraft_reads_text_with_expected_key() = runTest {
        val prefs = mockk<UserPreferenceRepository>()
        val useCase = GetDraftUseCase(prefs)

        coEvery { prefs.getString(SaveDraftUseCase.KEY_DRAFT_TEXT, "") } returns "saved"

        val result = useCase()

        assertEquals("saved", result)
    }

    @Test
    fun deleteDraft_clears_draft_text() = runTest {
        val prefs = mockk<UserPreferenceRepository>()
        val useCase = DeleteDraftUseCase(prefs)

        coEvery { prefs.setString(any(), any()) } just runs

        useCase()

        coVerify(exactly = 1) { prefs.setString(SaveDraftUseCase.KEY_DRAFT_TEXT, "") }
    }

    @Test
    fun getAllCaptures_forwards_default_paging_values() = runTest {
        val repository = mockk<CaptureRepository>()
        val expected = listOf(
            Capture(
                id = "c1",
                originalText = "hello",
                classifiedType = ClassifiedType.TEMP,
                source = CaptureSource.APP
            )
        )
        val useCase = GetAllCapturesUseCase(repository)

        every { repository.getAllCaptures(0, 20) } returns flowOf(expected)

        useCase().test {
            assertEquals(expected, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun softDelete_delegates_to_repository() = runTest {
        val repository = mockk<CaptureRepository>()
        val useCase = SoftDeleteCaptureUseCase(repository)

        coEvery { repository.softDelete("cap-1") } just runs

        useCase("cap-1")

        coVerify(exactly = 1) { repository.softDelete("cap-1") }
    }

    @Test
    fun undoSoftDelete_delegates_to_repository() = runTest {
        val repository = mockk<CaptureRepository>()
        val useCase = UndoDeleteCaptureUseCase(repository)

        coEvery { repository.undoSoftDelete("cap-1") } just runs

        useCase("cap-1")

        coVerify(exactly = 1) { repository.undoSoftDelete("cap-1") }
    }

    @Test
    fun hardDelete_removes_derived_entities_then_capture() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val tagRepository = mockk<TagRepository>()

        val useCase = HardDeleteCaptureUseCase(
            captureRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            tagRepository
        )

        coEvery { todoRepository.deleteByCaptureId("cap-1") } just runs
        coEvery { scheduleRepository.deleteByCaptureId("cap-1") } just runs
        coEvery { noteRepository.deleteByCaptureId("cap-1") } just runs
        coEvery { tagRepository.deleteTagsByCaptureId("cap-1") } just runs
        coEvery { captureRepository.hardDelete("cap-1") } just runs

        useCase("cap-1")

        coVerifySequence {
            todoRepository.deleteByCaptureId("cap-1")
            scheduleRepository.deleteByCaptureId("cap-1")
            noteRepository.deleteByCaptureId("cap-1")
            tagRepository.deleteTagsByCaptureId("cap-1")
            captureRepository.hardDelete("cap-1")
        }
    }

    @Test
    fun submitCapture_rejects_blank_text() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val syncQueueRepository = mockk<SyncQueueRepository>()
        val workManager = mockk<WorkManager>()
        val useCase = SubmitCaptureUseCase(captureRepository, syncQueueRepository, workManager)

        val error = runCatching { useCase("   ") }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertEquals("캡처 내용이 비어있습니다", error?.message)
        coVerify(exactly = 0) { captureRepository.saveCapture(any()) }
        coVerify(exactly = 0) { syncQueueRepository.enqueue(any()) }
    }

    @Test
    fun submitCapture_persists_temp_capture_and_enqueues_classify_job() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val syncQueueRepository = mockk<SyncQueueRepository>()
        val workManager = mockk<WorkManager>(relaxed = true)
        val useCase = SubmitCaptureUseCase(captureRepository, syncQueueRepository, workManager)

        mockkObject(ClassifyCaptureWorker.Companion)

        coEvery { captureRepository.saveCapture(any()) } answers { firstArg() }
        coEvery { syncQueueRepository.enqueue(any()) } just runs
        every { ClassifyCaptureWorker.enqueue(workManager) } just runs

        val capture = useCase("Buy milk", CaptureSource.APP)

        assertEquals("Buy milk", capture.originalText)
        assertEquals(ClassifiedType.TEMP, capture.classifiedType)
        coVerify(exactly = 1) { captureRepository.saveCapture(any()) }
        coVerify(exactly = 1) {
            syncQueueRepository.enqueue(match {
                it.action == SyncAction.CLASSIFY && it.payload == capture.id
            })
        }
        io.mockk.verify(exactly = 1) { ClassifyCaptureWorker.enqueue(workManager) }
    }
}
