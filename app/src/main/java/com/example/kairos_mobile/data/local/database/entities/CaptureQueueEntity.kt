package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 캡처 큐 Room Entity
 * 오프라인에서 캡처된 항목들을 저장
 */
@Entity(tableName = "capture_queue")
data class CaptureQueueEntity(
    @PrimaryKey
    val id: String,

    val content: String,

    val timestamp: Long,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String,  // SyncStatus.name

    @ColumnInfo(name = "classification_type")
    val classificationType: String?,  // CaptureType.name

    @ColumnInfo(name = "destination_path")
    val destinationPath: String?,

    val title: String?,

    val tags: String?,  // JSON 문자열 (List<String>)

    val confidence: Float?,

    val metadata: String?,  // JSON 문자열 (Map<String, String>)

    @ColumnInfo(name = "error_message")
    val errorMessage: String?,

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,

    @ColumnInfo(name = "last_retry_timestamp")
    val lastRetryTimestamp: Long? = null,

    // 멀티모달 캡처 지원
    @ColumnInfo(name = "source")
    val source: String = "TEXT",  // CaptureSource.name (TEXT, IMAGE, VOICE, SHARE, WEB_CLIP)

    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null,  // 이미지 URI

    @ColumnInfo(name = "audio_uri")
    val audioUri: String? = null,  // 음성 URI

    @ColumnInfo(name = "web_url")
    val webUrl: String? = null,  // 웹 클립 URL

    @ColumnInfo(name = "web_title")
    val webTitle: String? = null,  // 웹 페이지 제목

    @ColumnInfo(name = "web_description")
    val webDescription: String? = null,  // 웹 페이지 설명

    @ColumnInfo(name = "web_image_url")
    val webImageUrl: String? = null,  // 웹 페이지 이미지 URL

    // 스마트 처리 기능
    @ColumnInfo(name = "summary")
    val summary: String? = null,  // AI 생성 요약

    @ColumnInfo(name = "suggested_tags")
    val suggestedTags: String? = null,  // 제안된 태그 (JSON 배열)

    @ColumnInfo(name = "applied_tags")
    val appliedTags: String? = null,  // 사용자가 선택한 태그 (JSON 배열)

    // 외부 서비스 동기화 상태
    @ColumnInfo(name = "google_calendar_synced", defaultValue = "0")
    val googleCalendarSynced: Boolean = false,

    @ColumnInfo(name = "google_calendar_event_id")
    val googleCalendarEventId: String? = null,

    @ColumnInfo(name = "todoist_synced", defaultValue = "0")
    val todoistSynced: Boolean = false,

    @ColumnInfo(name = "todoist_task_id")
    val todoistTaskId: String? = null,

    @ColumnInfo(name = "external_sync_time")
    val externalSyncTime: Long? = null  // 마지막 외부 서비스 동기화 시간
)
