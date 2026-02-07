package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.SyncAction
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ImageRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.repository.SyncQueueRepository
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
import io.mockk.unmockkAll
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
    fun hardDelete_removes_derived_entities_then_capture() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val todoRepository = mockk<TodoRepository>()
        val scheduleRepository = mockk<ScheduleRepository>()
        val noteRepository = mockk<NoteRepository>()
        val tagRepository = mockk<TagRepository>()
        val imageRepository = mockk<ImageRepository>()

        val useCase = HardDeleteCaptureUseCase(
            captureRepository,
            todoRepository,
            scheduleRepository,
            noteRepository,
            tagRepository,
            imageRepository
        )

        coEvery { todoRepository.deleteByCaptureId("cap-1") } just runs
        coEvery { scheduleRepository.deleteByCaptureId("cap-1") } just runs
        coEvery { noteRepository.deleteByCaptureId("cap-1") } just runs
        coEvery { tagRepository.deleteTagsByCaptureId("cap-1") } just runs
        coEvery { captureRepository.hardDelete("cap-1") } just runs
        coEvery {
            captureRepository.getCaptureById("cap-1")
        } returns Capture(
            id = "cap-1",
            originalText = "img",
            classifiedType = ClassifiedType.NOTES,
            source = CaptureSource.APP,
            imageUri = "file:///tmp/cap.jpg"
        )
        coEvery { imageRepository.deleteImage("file:///tmp/cap.jpg") } just runs

        useCase("cap-1")

        coVerifySequence {
            captureRepository.getCaptureById("cap-1")
            todoRepository.deleteByCaptureId("cap-1")
            scheduleRepository.deleteByCaptureId("cap-1")
            noteRepository.deleteByCaptureId("cap-1")
            tagRepository.deleteTagsByCaptureId("cap-1")
            captureRepository.hardDelete("cap-1")
            imageRepository.deleteImage("file:///tmp/cap.jpg")
        }
    }

    @Test
    fun submitCapture_rejects_blank_text() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val syncQueueRepository = mockk<SyncQueueRepository>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val useCase = SubmitCaptureUseCase(captureRepository, syncQueueRepository, trackEventUseCase)

        val error = runCatching { useCase("   ") }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertEquals("캡처 내용이 비어있습니다", error?.message)
        coVerify(exactly = 0) { captureRepository.saveCapture(any()) }
        coVerify(exactly = 0) { syncQueueRepository.enqueue(any()) }
        io.mockk.verify(exactly = 0) { syncQueueRepository.triggerProcessing() }
    }

    @Test
    fun submitCapture_persists_temp_capture_and_enqueues_classify_job() = runTest {
        val captureRepository = mockk<CaptureRepository>()
        val syncQueueRepository = mockk<SyncQueueRepository>()
        val trackEventUseCase = mockk<TrackEventUseCase>()
        val useCase = SubmitCaptureUseCase(captureRepository, syncQueueRepository, trackEventUseCase)

        coEvery { captureRepository.saveCapture(any()) } answers { firstArg() }
        coEvery { syncQueueRepository.enqueue(any()) } just runs
        every { syncQueueRepository.triggerProcessing() } just runs
        coEvery { trackEventUseCase(any(), any()) } just runs

        val capture = useCase("Buy milk", CaptureSource.APP)

        assertEquals("Buy milk", capture.originalText)
        assertEquals(ClassifiedType.TEMP, capture.classifiedType)
        coVerify(exactly = 1) { captureRepository.saveCapture(any()) }
        coVerify(exactly = 1) {
            syncQueueRepository.enqueue(match {
                it.action == SyncAction.CLASSIFY && it.payload == capture.id
            })
        }
        io.mockk.verify(exactly = 1) { syncQueueRepository.triggerProcessing() }
    }
}
