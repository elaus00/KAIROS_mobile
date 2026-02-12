package com.flit.app.data.repository

import com.flit.app.data.local.database.dao.CaptureDao
import com.flit.app.data.local.database.dao.FolderDao
import com.flit.app.data.local.database.dao.NoteDao
import com.flit.app.data.local.database.dao.ScheduleDao
import com.flit.app.data.local.database.dao.TagDao
import com.flit.app.data.local.database.dao.TodoDao
import com.flit.app.data.remote.ApiResponseHandler
import com.flit.app.data.remote.DeviceIdProvider
import com.flit.app.data.remote.api.FlitApi
import com.flit.app.data.remote.dto.v2.SyncPullRequest
import com.flit.app.data.remote.dto.v2.SyncPushItem
import com.flit.app.data.remote.dto.v2.SyncPushRequest
import com.flit.app.domain.model.SyncResult
import com.flit.app.domain.repository.SyncRepository
import com.flit.app.domain.repository.UserPreferenceRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val api: FlitApi,
    private val deviceIdProvider: DeviceIdProvider,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val captureDao: CaptureDao,
    private val todoDao: TodoDao,
    private val scheduleDao: ScheduleDao,
    private val noteDao: NoteDao,
    private val tagDao: TagDao,
    private val folderDao: FolderDao
) : SyncRepository {

    companion object {
        private const val KEY_LAST_SYNC_AT = "sync_last_sync_at"
        private const val KEY_LAST_SYNC_CURSOR = "sync_last_sync_cursor"
        private const val KEY_LAST_SYNC_USER_ID = "sync_last_sync_user_id"
    }

    override suspend fun pushLocalData(currentUserId: String): SyncResult {
        if (isAccountSwitched(currentUserId)) {
            return SyncResult(
                success = false,
                skipped = true,
                accountSwitchRequired = true,
                message = "다른 계정으로 마지막 동기화가 수행되어 자동 push를 건너뜁니다."
            )
        }

        val lastSyncAt = getLastSyncAt()
        val isFirstPush = lastSyncAt == null

        val captureChanges = captureDao.getAllForSync().map { capture ->
            val operation = when {
                capture.isDeleted -> "delete"
                isFirstPush -> "create"
                else -> "update"
            }
            SyncPushItem(
                entityType = "captures",
                operation = operation,
                clientId = capture.id,
                data = mapOf(
                    "id" to capture.id,
                    "original_text" to capture.originalText,
                    "ai_title" to capture.aiTitle,
                    "classified_type" to capture.classifiedType,
                    "note_sub_type" to capture.noteSubType,
                    "confidence" to capture.confidence,
                    "source" to capture.source,
                    "is_confirmed" to capture.isConfirmed,
                    "confirmed_at" to capture.confirmedAt,
                    "is_deleted" to capture.isDeleted,
                    "deleted_at" to capture.deletedAt,
                    "is_trashed" to capture.isTrashed,
                    "trashed_at" to capture.trashedAt,
                    "image_uri" to capture.imageUri,
                    "parent_capture_id" to capture.parentCaptureId,
                    "created_at" to capture.createdAt,
                    "updated_at" to capture.updatedAt
                ),
                clientUpdatedAt = epochMsToIso(capture.updatedAt)
            )
        }

        val todoChanges = todoDao.getAllForSync().map { todo ->
            SyncPushItem(
                entityType = "tasks",
                operation = if (isFirstPush) "create" else "update",
                clientId = todo.id,
                data = mapOf(
                    "id" to todo.id,
                    "capture_id" to todo.captureId,
                    "deadline" to todo.deadline,
                    "is_completed" to todo.isCompleted,
                    "completed_at" to todo.completedAt,
                    "sort_order" to todo.sortOrder,
                    "deadline_source" to todo.deadlineSource,
                    "sort_source" to todo.sortSource,
                    "created_at" to todo.createdAt,
                    "updated_at" to todo.updatedAt
                ),
                clientUpdatedAt = epochMsToIso(todo.updatedAt)
            )
        }

        val scheduleChanges = scheduleDao.getAllForSync().map { schedule ->
            SyncPushItem(
                entityType = "events",
                operation = if (isFirstPush) "create" else "update",
                clientId = schedule.id,
                data = mapOf(
                    "id" to schedule.id,
                    "capture_id" to schedule.captureId,
                    "start_time" to schedule.startTime,
                    "end_time" to schedule.endTime,
                    "location" to schedule.location,
                    "is_all_day" to schedule.isAllDay,
                    "confidence" to schedule.confidence,
                    "calendar_sync_status" to schedule.calendarSyncStatus,
                    "calendar_event_id" to schedule.calendarEventId,
                    "created_at" to schedule.createdAt,
                    "updated_at" to schedule.updatedAt
                ),
                clientUpdatedAt = epochMsToIso(schedule.updatedAt)
            )
        }

        val noteChanges = noteDao.getAllForSync().map { note ->
            SyncPushItem(
                entityType = "notes",
                operation = if (isFirstPush) "create" else "update",
                clientId = note.id,
                data = mapOf(
                    "id" to note.id,
                    "capture_id" to note.captureId,
                    "folder_id" to note.folderId,
                    "body" to note.body,
                    "created_at" to note.createdAt,
                    "updated_at" to note.updatedAt
                ),
                clientUpdatedAt = epochMsToIso(note.updatedAt)
            )
        }

        val tagChanges = tagDao.getAllForSync().map { tag ->
            SyncPushItem(
                entityType = "tags",
                operation = if (isFirstPush) "create" else "update",
                clientId = tag.id,
                data = mapOf(
                    "id" to tag.id,
                    "name" to tag.name,
                    "created_at" to tag.createdAt
                ),
                clientUpdatedAt = epochMsToIso(tag.createdAt)
            )
        }

        val folderChanges = folderDao.getAllForSync().map { folder ->
            SyncPushItem(
                entityType = "folders",
                operation = if (isFirstPush) "create" else "update",
                clientId = folder.id,
                data = mapOf(
                    "id" to folder.id,
                    "name" to folder.name,
                    "type" to folder.type,
                    "sort_order" to folder.sortOrder,
                    "created_at" to folder.createdAt
                ),
                clientUpdatedAt = epochMsToIso(folder.createdAt)
            )
        }

        val changes = captureChanges + todoChanges + scheduleChanges + noteChanges + tagChanges + folderChanges
        if (changes.isEmpty()) {
            val now = Instant.now().toString()
            storeSyncState(currentUserId = currentUserId, lastSyncAt = now, cursor = now)
            return SyncResult(success = true, pushedCount = 0, message = "동기화할 로컬 데이터가 없습니다.")
        }

        val response = ApiResponseHandler.safeCall {
            api.syncPush(
                SyncPushRequest(
                    deviceId = deviceIdProvider.getOrCreateDeviceId(),
                    changes = changes
                )
            )
        }

        val syncedAt = response.serverTimestamp ?: Instant.now().toString()
        storeSyncState(currentUserId = currentUserId, lastSyncAt = syncedAt, cursor = syncedAt)

        return SyncResult(
            success = true,
            pushedCount = response.acknowledged.takeIf { it > 0 } ?: changes.size,
            message = "로컬 데이터를 서버로 동기화했습니다."
        )
    }

    override suspend fun pullServerData(currentUserId: String): SyncResult {
        if (isAccountSwitched(currentUserId)) {
            return SyncResult(
                success = false,
                skipped = true,
                accountSwitchRequired = true,
                message = "다른 계정으로 마지막 동기화가 수행되어 자동 pull을 건너뜁니다."
            )
        }

        val cursor = userPreferenceRepository.getString(KEY_LAST_SYNC_CURSOR, "").ifBlank { null }
        val response = ApiResponseHandler.safeCall {
            api.syncPull(
                SyncPullRequest(
                    deviceId = deviceIdProvider.getOrCreateDeviceId(),
                    cursor = cursor
                )
            )
        }

        val nextCursor = response.nextCursor ?: Instant.now().toString()
        storeSyncState(currentUserId = currentUserId, lastSyncAt = nextCursor, cursor = nextCursor)

        // TODO: 서버 변경 사항의 로컬 반영은 서버 스키마 확정 후 적용
        return SyncResult(
            success = true,
            pulledCount = response.changes.size,
            message = if (response.changes.isEmpty()) {
                "서버에서 가져올 변경 사항이 없습니다."
            } else {
                "서버 변경 사항 ${response.changes.size}건을 확인했습니다."
            }
        )
    }

    override suspend fun getLastSyncAt(): String? {
        return userPreferenceRepository.getString(KEY_LAST_SYNC_AT, "").ifBlank { null }
    }

    private suspend fun isAccountSwitched(currentUserId: String): Boolean {
        val lastUserId = userPreferenceRepository.getString(KEY_LAST_SYNC_USER_ID, "").ifBlank { null }
        return lastUserId != null && lastUserId != currentUserId
    }

    private suspend fun storeSyncState(currentUserId: String, lastSyncAt: String, cursor: String) {
        userPreferenceRepository.setString(KEY_LAST_SYNC_AT, lastSyncAt)
        userPreferenceRepository.setString(KEY_LAST_SYNC_CURSOR, cursor)
        userPreferenceRepository.setString(KEY_LAST_SYNC_USER_ID, currentUserId)
    }

    private fun epochMsToIso(epochMs: Long): String {
        return Instant.ofEpochMilli(epochMs).toString()
    }
}
