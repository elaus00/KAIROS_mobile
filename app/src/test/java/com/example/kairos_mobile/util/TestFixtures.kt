package com.example.kairos_mobile.util

import com.example.kairos_mobile.domain.model.AnalyticsEvent
import com.example.kairos_mobile.domain.model.CalendarSyncStatus
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.model.ClassificationLog
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.ConfidenceLevel
import com.example.kairos_mobile.domain.model.DeadlineSource
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.FolderType
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.model.Schedule
import com.example.kairos_mobile.domain.model.SortSource
import com.example.kairos_mobile.domain.model.SyncAction
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.SyncQueueItem
import com.example.kairos_mobile.domain.model.SyncQueueStatus
import com.example.kairos_mobile.domain.model.Todo

/**
 * 테스트용 Mock 데이터 팩토리
 */
object TestFixtures {

    fun capture(
        id: String = "cap-1",
        originalText: String = "테스트 캡처",
        aiTitle: String? = "테스트 제목",
        classifiedType: ClassifiedType = ClassifiedType.TEMP,
        noteSubType: NoteSubType? = null,
        confidence: ConfidenceLevel? = null,
        source: CaptureSource = CaptureSource.APP,
        isConfirmed: Boolean = false,
        isDeleted: Boolean = false,
        isTrashed: Boolean = false,
        trashedAt: Long? = null,
        imageUri: String? = null,
        parentCaptureId: String? = null,
        createdAt: Long = 1000L
    ) = Capture(
        id = id,
        originalText = originalText,
        aiTitle = aiTitle,
        classifiedType = classifiedType,
        noteSubType = noteSubType,
        confidence = confidence,
        source = source,
        isConfirmed = isConfirmed,
        isDeleted = isDeleted,
        isTrashed = isTrashed,
        trashedAt = trashedAt,
        imageUri = imageUri,
        parentCaptureId = parentCaptureId,
        createdAt = createdAt
    )

    fun folder(
        id: String = "folder-1",
        name: String = "테스트 폴더",
        type: FolderType = FolderType.USER,
        sortOrder: Int = 0
    ) = Folder(
        id = id,
        name = name,
        type = type,
        sortOrder = sortOrder
    )

    fun todo(
        id: String = "todo-1",
        captureId: String = "cap-1",
        deadline: Long? = null,
        isCompleted: Boolean = false,
        deadlineSource: DeadlineSource? = null,
        sortSource: SortSource = SortSource.DEFAULT
    ) = Todo(
        id = id,
        captureId = captureId,
        deadline = deadline,
        isCompleted = isCompleted,
        deadlineSource = deadlineSource,
        sortSource = sortSource
    )

    fun schedule(
        id: String = "sch-1",
        captureId: String = "cap-1",
        startTime: Long? = null,
        endTime: Long? = null,
        location: String? = null,
        isAllDay: Boolean = false,
        calendarSyncStatus: CalendarSyncStatus = CalendarSyncStatus.NOT_LINKED,
        googleEventId: String? = null
    ) = Schedule(
        id = id,
        captureId = captureId,
        startTime = startTime,
        endTime = endTime,
        location = location,
        isAllDay = isAllDay,
        calendarSyncStatus = calendarSyncStatus,
        googleEventId = googleEventId
    )

    fun classificationLog(
        id: String = "log-1",
        captureId: String = "cap-1",
        originalType: ClassifiedType = ClassifiedType.TEMP,
        newType: ClassifiedType = ClassifiedType.TODO
    ) = ClassificationLog(
        id = id,
        captureId = captureId,
        originalType = originalType,
        newType = newType
    )

    fun analyticsEvent(
        id: String = "event-1",
        eventType: String = "capture_created",
        eventData: String? = null
    ) = AnalyticsEvent(
        id = id,
        eventType = eventType,
        eventData = eventData
    )

    fun note(
        id: String = "note-1",
        captureId: String = "cap-1",
        folderId: String? = "system-inbox",
        body: String? = null,
        createdAt: Long = 1000L
    ) = Note(
        id = id,
        captureId = captureId,
        folderId = folderId,
        body = body,
        createdAt = createdAt
    )

    fun syncQueueItem(
        id: String = "sync-1",
        action: SyncAction = SyncAction.CLASSIFY,
        payload: String = "cap-1",
        retryCount: Int = 0,
        maxRetries: Int = 3,
        status: SyncQueueStatus = SyncQueueStatus.PENDING
    ) = SyncQueueItem(
        id = id,
        action = action,
        payload = payload,
        retryCount = retryCount,
        maxRetries = maxRetries,
        status = status
    )
}
