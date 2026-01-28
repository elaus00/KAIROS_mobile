package com.example.kairos_mobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kairos_mobile.data.local.database.dao.BookmarkDao
import com.example.kairos_mobile.data.local.database.dao.InsightQueueDao
import com.example.kairos_mobile.data.local.database.dao.NoteDao
import com.example.kairos_mobile.data.local.database.dao.NotificationDao
import com.example.kairos_mobile.data.local.database.dao.ScheduleDao
import com.example.kairos_mobile.data.local.database.dao.TodoDao
import com.example.kairos_mobile.data.local.database.entities.BookmarkEntity
import com.example.kairos_mobile.data.local.database.entities.InsightQueueEntity
import com.example.kairos_mobile.data.local.database.entities.NoteEntity
import com.example.kairos_mobile.data.local.database.entities.NotificationEntity
import com.example.kairos_mobile.data.local.database.entities.ScheduleEntity
import com.example.kairos_mobile.data.local.database.entities.TodoEntity

/**
 * KAIROS 앱의 Room Database (PRD v4.0)
 */
@Database(
    entities = [
        InsightQueueEntity::class,
        NotificationEntity::class,
        TodoEntity::class,
        ScheduleEntity::class,
        NoteEntity::class,
        BookmarkEntity::class
    ],
    version = 7,  // PRD v4.0: Schedule, Note, Bookmark 테이블 추가
    exportSchema = true
)
abstract class KairosDatabase : RoomDatabase() {

    /**
     * 인사이트 큐 DAO
     */
    abstract fun insightQueueDao(): InsightQueueDao

    /**
     * 알림 DAO
     */
    abstract fun notificationDao(): NotificationDao

    /**
     * 투두 DAO
     */
    abstract fun todoDao(): TodoDao

    /**
     * 일정 DAO (PRD v4.0)
     */
    abstract fun scheduleDao(): ScheduleDao

    /**
     * 노트 DAO (PRD v4.0)
     */
    abstract fun noteDao(): NoteDao

    /**
     * 북마크 DAO (PRD v4.0)
     */
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val DATABASE_NAME = "kairos_database"

        /**
         * Database v1 → v2 마이그레이션
         * Phase 2: 멀티모달 캡처 지원을 위한 새 필드 추가
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // source: 캡처 소스 타입 (TEXT, IMAGE, VOICE, SHARE, WEB_CLIP)
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN source TEXT NOT NULL DEFAULT 'TEXT'"
                )

                // imageUri: 이미지 캡처 시 이미지 URI
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN image_uri TEXT"
                )

                // audioUri: 음성 캡처 시 오디오 URI
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN audio_uri TEXT"
                )

                // webUrl: 웹 클립 시 원본 URL
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN web_url TEXT"
                )

                // webTitle: 웹 페이지 제목
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN web_title TEXT"
                )

                // webDescription: 웹 페이지 설명
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN web_description TEXT"
                )

                // webImageUrl: 웹 페이지 대표 이미지 URL
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN web_image_url TEXT"
                )
            }
        }

        /**
         * Database v2 → v3 마이그레이션
         * Phase 3: 스마트 처리 기능 및 외부 서비스 동기화 지원
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // M09: AI 요약
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN summary TEXT"
                )

                // M10: 스마트 태그 제안
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN suggested_tags TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN applied_tags TEXT"
                )

                // M11: Google Calendar 동기화 상태
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN google_calendar_synced INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN google_calendar_event_id TEXT"
                )

                // M12: Todoist 동기화 상태
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN todoist_synced INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN todoist_task_id TEXT"
                )

                // 외부 서비스 동기화 시간
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN external_sync_time INTEGER"
                )
            }
        }

        /**
         * Database v3 → v4 마이그레이션
         * Phase 3: 알림 기능 추가
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 알림 테이블 생성
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notifications (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        is_read INTEGER NOT NULL DEFAULT 0,
                        related_capture_id TEXT,
                        type TEXT NOT NULL DEFAULT 'INFO'
                    )
                    """.trimIndent()
                )

                // 알림 timestamp 인덱스 생성 (성능 최적화)
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notifications_timestamp ON notifications(timestamp)"
                )

                // 알림 읽음 상태 인덱스 생성 (읽지 않은 알림 빠른 조회)
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notifications_is_read ON notifications(is_read)"
                )
            }
        }

        /**
         * Database v4 → v5 마이그레이션
         * Phase 4: Capture → Insight 리네이밍
         * 테이블명 capture_queue → insight_queue 변경
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 테이블명 변경
                database.execSQL(
                    "ALTER TABLE capture_queue RENAME TO insight_queue"
                )
            }
        }

        /**
         * Database v5 → v6 마이그레이션
         * Phase 5: Todo 테이블 추가 및 SCHEDULE → NOTE 타입 변환
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. todos 테이블 생성
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS todos (
                        id TEXT PRIMARY KEY NOT NULL,
                        content TEXT NOT NULL,
                        title TEXT,
                        source_insight_id TEXT,
                        due_date INTEGER,
                        due_time TEXT,
                        priority INTEGER NOT NULL DEFAULT 0,
                        labels TEXT,
                        manual_order INTEGER NOT NULL DEFAULT 0,
                        is_completed INTEGER NOT NULL DEFAULT 0,
                        completed_at INTEGER,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // 2. todos 인덱스 생성 (마감일 조회 성능 최적화)
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_todos_due_date ON todos(due_date)"
                )

                // 3. todos 완료 상태 인덱스 생성
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_todos_is_completed ON todos(is_completed)"
                )

                // 4. SCHEDULE 타입을 NOTE로 변환
                database.execSQL(
                    """
                    UPDATE insight_queue
                    SET classification_type = 'NOTE'
                    WHERE classification_type = 'SCHEDULE'
                    """.trimIndent()
                )
            }
        }

        /**
         * Database v6 → v7 마이그레이션
         * PRD v4.0: Schedule, Note, Bookmark 테이블 추가
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. schedules 테이블 생성
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS schedules (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        time TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        location TEXT,
                        category TEXT NOT NULL,
                        google_calendar_id TEXT,
                        source_insight_id TEXT,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // schedules 인덱스 생성
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_schedules_date ON schedules(date)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_schedules_category ON schedules(category)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_schedules_google_calendar_id ON schedules(google_calendar_id)"
                )

                // 2. notes 테이블 생성
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notes (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        folder TEXT NOT NULL DEFAULT 'INBOX',
                        tags TEXT,
                        source_insight_id TEXT,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // notes 인덱스 생성
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notes_folder ON notes(folder)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notes_created_at ON notes(created_at)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notes_updated_at ON notes(updated_at)"
                )

                // 3. bookmarks 테이블 생성
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bookmarks (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        url TEXT NOT NULL,
                        summary TEXT,
                        tags TEXT,
                        favicon_url TEXT,
                        source_insight_id TEXT,
                        created_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // bookmarks 인덱스 생성
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_bookmarks_created_at ON bookmarks(created_at)"
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_bookmarks_url ON bookmarks(url)"
                )
            }
        }
    }
}
