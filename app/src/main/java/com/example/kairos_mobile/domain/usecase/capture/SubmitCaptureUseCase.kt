package com.example.kairos_mobile.domain.usecase.capture

import androidx.work.WorkManager
import com.example.kairos_mobile.data.worker.ClassifyCaptureWorker
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.SyncAction
import com.example.kairos_mobile.domain.model.SyncQueueItem

import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.SyncQueueRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캡처 제출 UseCase
 * 1. TEMP 상태로 로컬 저장
 * 2. SyncQueue에 CLASSIFY 작업 등록
 */
@Singleton
class SubmitCaptureUseCase @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val syncQueueRepository: SyncQueueRepository,
    private val workManager: WorkManager
) {
    suspend operator fun invoke(
        text: String,
        source: CaptureSource = CaptureSource.APP
    ): Capture {
        require(text.isNotBlank()) { "캡처 내용이 비어있습니다" }

        // 1. TEMP 상태로 즉시 저장
        val capture = Capture(
            originalText = text,
            classifiedType = ClassifiedType.TEMP,
            source = source
        )
        captureRepository.saveCapture(capture)

        // 2. SyncQueue에 분류 작업 등록
        val syncItem = SyncQueueItem(
            id = UUID.randomUUID().toString(),
            action = SyncAction.CLASSIFY,
            payload = capture.id
        )
        syncQueueRepository.enqueue(syncItem)
        // 큐 적재 직후 즉시 분류 워커를 실행한다.
        runCatching { ClassifyCaptureWorker.enqueue(workManager) }

        return capture
    }
}
