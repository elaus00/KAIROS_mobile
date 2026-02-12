package com.flit.app.data.repository

import androidx.room.withTransaction
import com.flit.app.data.local.database.FlitDatabase
import com.flit.app.data.local.database.dao.CaptureDao
import com.flit.app.data.local.database.dao.FolderDao
import com.flit.app.data.local.database.dao.NoteDao
import com.flit.app.data.local.database.dao.ScheduleDao
import com.flit.app.data.local.database.dao.TagDao
import com.flit.app.data.local.database.dao.TodoDao
import com.flit.app.data.local.database.entities.CaptureEntity
import com.flit.app.data.local.database.entities.FolderEntity
import com.flit.app.data.local.database.entities.NoteEntity
import com.flit.app.data.local.database.entities.ScheduleEntity
import com.flit.app.data.local.database.entities.TagEntity
import com.flit.app.data.local.database.entities.TodoEntity
import com.flit.app.data.remote.ApiResponseHandler
import com.flit.app.data.remote.DeviceIdProvider
import com.flit.app.data.remote.api.FlitApi
import com.flit.app.data.remote.dto.v2.SyncPullItem
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
    private val database: FlitDatabase,
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

        if (response.changes.isEmpty()) {
            val nextCursor = response.nextCursor ?: Instant.now().toString()
            storeSyncState(currentUserId = currentUserId, lastSyncAt = nextCursor, cursor = nextCursor)
            return SyncResult(
                success = true,
                pulledCount = 0,
                message = "서버에서 가져올 변경 사항이 없습니다."
            )
        }

        val applyResult = runCatching { applyServerChanges(response.changes) }
        if (applyResult.isFailure) {
            return SyncResult(
                success = false,
                skipped = true,
                pulledCount = response.changes.size,
                message = applyResult.exceptionOrNull()?.message
                    ?: "서버 변경 사항 반영에 실패했습니다."
            )
        }
        val applyStats = applyResult.getOrThrow()

        val nextCursor = response.nextCursor ?: Instant.now().toString()
        storeSyncState(currentUserId = currentUserId, lastSyncAt = nextCursor, cursor = nextCursor)

        return SyncResult(
            success = true,
            pulledCount = applyStats.appliedCount,
            message = if (applyStats.skippedCount > 0) {
                "서버 변경 ${applyStats.totalCount}건 중 ${applyStats.appliedCount}건 반영, ${applyStats.skippedCount}건 스킵"
            } else {
                "서버 변경 사항 ${applyStats.appliedCount}건을 반영했습니다."
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

    private data class ApplyStats(
        val totalCount: Int,
        val appliedCount: Int,
        val skippedCount: Int
    )

    private suspend fun applyServerChanges(changes: List<SyncPullItem>): ApplyStats {
        var appliedCount = 0
        var skippedCount = 0
        database.withTransaction {
            val deletions = changes.filter { it.operation.equals("delete", ignoreCase = true) }
            val upserts = changes.filterNot { it.operation.equals("delete", ignoreCase = true) }

            // Delete는 FK 제약을 고려해 자식부터 처리.
            deletions.forEach { item ->
                val id = item.resolveId()
                if (id == null) {
                    skippedCount++
                    return@forEach
                }
                when (item.normalizedEntityType()) {
                    "tasks" -> {
                        todoDao.deleteById(id)
                        appliedCount++
                    }
                    "events" -> {
                        scheduleDao.deleteById(id)
                        appliedCount++
                    }
                    "notes" -> {
                        noteDao.deleteById(id)
                        appliedCount++
                    }
                    "captures" -> {
                        captureDao.hardDelete(id)
                        appliedCount++
                    }
                    "tags" -> {
                        tagDao.deleteById(id)
                        appliedCount++
                    }
                    "folders" -> {
                        folderDao.deleteByIdForSync(id)
                        appliedCount++
                    }
                    else -> skippedCount++
                }
            }

            // Upsert는 부모 엔티티를 먼저 반영.
            upserts.filterEntity("folders").forEach {
                if (upsertFolder(it)) appliedCount++ else skippedCount++
            }
            upserts.filterEntity("tags").forEach {
                if (upsertTag(it)) appliedCount++ else skippedCount++
            }
            upserts.filterEntity("captures").forEach {
                if (upsertCapture(it)) appliedCount++ else skippedCount++
            }
            upserts.filterEntity("tasks").forEach {
                if (upsertTodo(it)) appliedCount++ else skippedCount++
            }
            upserts.filterEntity("events").forEach {
                if (upsertSchedule(it)) appliedCount++ else skippedCount++
            }
            upserts.filterEntity("notes").forEach {
                if (upsertNote(it)) appliedCount++ else skippedCount++
            }

            val knownEntities = setOf("folders", "tags", "captures", "tasks", "events", "notes")
            skippedCount += upserts.count { it.normalizedEntityType() !in knownEntities }
        }
        return ApplyStats(
            totalCount = changes.size,
            appliedCount = appliedCount,
            skippedCount = skippedCount
        )
    }

    private suspend fun upsertCapture(item: SyncPullItem): Boolean {
        val data = item.data
        val now = System.currentTimeMillis()
        val id = item.resolveId() ?: return false
        val createdAt = data.long("created_at") ?: now
        val entity = CaptureEntity(
            id = id,
            originalText = data.string("original_text") ?: "",
            aiTitle = data.string("ai_title"),
            classifiedType = data.string("classified_type") ?: "TEMP",
            noteSubType = data.string("note_sub_type"),
            confidence = data.string("confidence"),
            source = data.string("source") ?: "APP",
            isConfirmed = data.bool("is_confirmed") ?: false,
            confirmedAt = data.long("confirmed_at"),
            isDeleted = data.bool("is_deleted") ?: false,
            deletedAt = data.long("deleted_at"),
            createdAt = createdAt,
            updatedAt = data.long("updated_at") ?: createdAt,
            classificationCompletedAt = data.long("classification_completed_at"),
            isTrashed = data.bool("is_trashed") ?: false,
            trashedAt = data.long("trashed_at"),
            imageUri = data.string("image_uri"),
            parentCaptureId = data.string("parent_capture_id")
        )
        if (captureDao.getById(id) == null) {
            captureDao.insert(entity)
        } else {
            captureDao.update(entity)
        }
        return true
    }

    private suspend fun upsertTodo(item: SyncPullItem): Boolean {
        val data = item.data
        val now = System.currentTimeMillis()
        val id = item.resolveId() ?: return false
        val captureId = data.string("capture_id") ?: return false
        val createdAt = data.long("created_at") ?: now
        val entity = TodoEntity(
            id = id,
            captureId = captureId,
            deadline = data.long("deadline"),
            isCompleted = data.bool("is_completed") ?: false,
            completedAt = data.long("completed_at"),
            sortOrder = data.int("sort_order") ?: 0,
            createdAt = createdAt,
            deadlineSource = data.string("deadline_source"),
            sortSource = data.string("sort_source") ?: "DEFAULT",
            updatedAt = data.long("updated_at") ?: createdAt
        )
        if (todoDao.getById(id) == null) {
            todoDao.insert(entity)
        } else {
            todoDao.update(entity)
        }
        return true
    }

    private suspend fun upsertSchedule(item: SyncPullItem): Boolean {
        val data = item.data
        val now = System.currentTimeMillis()
        val id = item.resolveId() ?: return false
        val captureId = data.string("capture_id") ?: return false
        val createdAt = data.long("created_at") ?: now
        val entity = ScheduleEntity(
            id = id,
            captureId = captureId,
            startTime = data.long("start_time"),
            endTime = data.long("end_time"),
            location = data.string("location"),
            isAllDay = data.bool("is_all_day") ?: false,
            confidence = data.string("confidence") ?: "MEDIUM",
            calendarSyncStatus = data.string("calendar_sync_status") ?: "NOT_LINKED",
            calendarEventId = data.string("calendar_event_id"),
            createdAt = createdAt,
            updatedAt = data.long("updated_at") ?: createdAt
        )
        if (scheduleDao.getById(id) == null) {
            scheduleDao.insert(entity)
        } else {
            scheduleDao.update(entity)
        }
        return true
    }

    private suspend fun upsertNote(item: SyncPullItem): Boolean {
        val data = item.data
        val now = System.currentTimeMillis()
        val id = item.resolveId() ?: return false
        val captureId = data.string("capture_id") ?: return false
        val createdAt = data.long("created_at") ?: now
        val entity = NoteEntity(
            id = id,
            captureId = captureId,
            folderId = data.string("folder_id"),
            createdAt = createdAt,
            updatedAt = data.long("updated_at") ?: createdAt,
            body = data.string("body")
        )
        if (noteDao.getById(id) == null) {
            noteDao.insert(entity)
        } else {
            noteDao.update(entity)
        }
        return true
    }

    private suspend fun upsertTag(item: SyncPullItem): Boolean {
        val data = item.data
        val now = System.currentTimeMillis()
        val id = item.resolveId() ?: return false
        val name = data.string("name") ?: id
        val entity = TagEntity(
            id = id,
            name = name,
            createdAt = data.long("created_at") ?: now
        )
        if (tagDao.getById(id) == null) {
            val existingByName = tagDao.getByName(name)
            if (existingByName != null && existingByName.id != id) {
                return false
            }
            tagDao.insert(entity)
            return tagDao.getById(id) != null
        } else {
            tagDao.update(entity)
        }
        return true
    }

    private suspend fun upsertFolder(item: SyncPullItem): Boolean {
        val data = item.data
        val now = System.currentTimeMillis()
        val id = item.resolveId() ?: return false
        val entity = FolderEntity(
            id = id,
            name = data.string("name") ?: "Untitled",
            type = data.string("type") ?: "USER",
            sortOrder = data.int("sort_order") ?: 0,
            createdAt = data.long("created_at") ?: now
        )
        if (folderDao.getById(id) == null) {
            folderDao.insert(entity)
        } else {
            folderDao.update(entity)
        }
        return true
    }

    private fun SyncPullItem.resolveId(): String? {
        return data.string("id")
            ?: serverId?.takeIf { it.isNotBlank() }
    }

    private fun SyncPullItem.normalizedEntityType(): String {
        return entityType.trim().lowercase()
    }

    private fun List<SyncPullItem>.filterEntity(type: String): List<SyncPullItem> {
        return filter { it.normalizedEntityType() == type }
    }

    private fun Map<String, Any?>.string(key: String): String? {
        val value = this[key] ?: return null
        return when (value) {
            is String -> value.takeIf { it.isNotBlank() }
            else -> value.toString().takeIf { it.isNotBlank() && it != "null" }
        }
    }

    private fun Map<String, Any?>.long(key: String): Long? {
        val value = this[key] ?: return null
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: value.toDoubleOrNull()?.toLong()
            else -> null
        }
    }

    private fun Map<String, Any?>.int(key: String): Int? {
        val value = this[key] ?: return null
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: value.toDoubleOrNull()?.toInt()
            else -> null
        }
    }

    private fun Map<String, Any?>.bool(key: String): Boolean? {
        val value = this[key] ?: return null
        return when (value) {
            is Boolean -> value
            is Number -> value.toInt() != 0
            is String -> {
                when (value.lowercase()) {
                    "true", "1", "yes" -> true
                    "false", "0", "no" -> false
                    else -> null
                }
            }
            else -> null
        }
    }
}
