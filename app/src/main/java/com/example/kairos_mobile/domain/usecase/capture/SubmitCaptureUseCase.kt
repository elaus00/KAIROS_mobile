package com.example.kairos_mobile.domain.usecase.capture

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.SyncAction
import com.example.kairos_mobile.domain.model.SyncQueueItem

import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.SyncQueueRepository
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
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
    private val trackEventUseCase: TrackEventUseCase
) {

    suspend operator fun invoke(
        text: String,
        source: CaptureSource = CaptureSource.APP,
        imageUri: String? = null
    ): Capture {
        require(text.isNotBlank() || imageUri != null) { "캡처 내용이 비어있습니다" }

        // 1. TEMP 상태로 즉시 저장
        val capture = Capture(
            originalText = text,
            classifiedType = ClassifiedType.TEMP,
            source = source,
            imageUri = imageUri
        )
        captureRepository.saveCapture(capture)

        // 2. SyncQueue에 분류 작업 등록
        val syncItem = SyncQueueItem(
            id = UUID.randomUUID().toString(),
            action = SyncAction.CLASSIFY,
            payload = capture.id
        )
        syncQueueRepository.enqueue(syncItem)
        syncQueueRepository.triggerProcessing()

        // 3. 분석 이벤트 추적
        trackEventUseCase(
            eventType = "capture_created",
            eventData = "source=${source.name}"
        )

        return capture
    }
}
