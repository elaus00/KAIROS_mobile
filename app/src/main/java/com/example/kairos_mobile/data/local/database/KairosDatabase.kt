package com.example.kairos_mobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kairos_mobile.data.local.database.dao.BookmarkDao
import com.example.kairos_mobile.data.local.database.dao.CaptureQueueDao
import com.example.kairos_mobile.data.local.database.dao.NoteDao
import com.example.kairos_mobile.data.local.database.dao.NotificationDao
import com.example.kairos_mobile.data.local.database.dao.ScheduleDao
import com.example.kairos_mobile.data.local.database.dao.TodoDao
import com.example.kairos_mobile.data.local.database.entities.BookmarkEntity
import com.example.kairos_mobile.data.local.database.entities.CaptureQueueEntity
import com.example.kairos_mobile.data.local.database.entities.NoteEntity
import com.example.kairos_mobile.data.local.database.entities.NotificationEntity
import com.example.kairos_mobile.data.local.database.entities.ScheduleEntity
import com.example.kairos_mobile.data.local.database.entities.TodoEntity

/**
 * KAIROS 앱의 Room Database (PRD v4.0)
 */
@Database(
    entities = [
        CaptureQueueEntity::class,
        NotificationEntity::class,
        TodoEntity::class,
        ScheduleEntity::class,
        NoteEntity::class,
        BookmarkEntity::class
    ],
    version = 8,  // Insight → Capture 리네이밍 완료
    exportSchema = true
)
abstract class KairosDatabase : RoomDatabase() {

    /**
     * 캡처 큐 DAO
     */
    abstract fun captureQueueDao(): CaptureQueueDao

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
         * 멀티모달 캡처 지원을 위한 새 필드 추가
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN source TEXT NOT NULL DEFAULT 'TEXT'"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN image_uri TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN audio_uri TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN web_url TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN web_title TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN web_description TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN web_image_url TEXT"
                )
            }
        }

        /**
         * Database v2 → v3 마이그레이션
         * 스마트 처리 기능 및 외부 서비스 동기화 지원
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN summary TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN suggested_tags TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN applied_tags TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN google_calendar_synced INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN google_calendar_event_id TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN todoist_synced INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN todoist_task_id TEXT"
                )
                database.execSQL(
                    "ALTER TABLE capture_queue ADD COLUMN external_sync_time INTEGER"
                )
            }
        }

        /**
         * Database v3 → v4 마이그레이션
         * 알림 기능 추가
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
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
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notifications_timestamp ON notifications(timestamp)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notifications_is_read ON notifications(is_read)"
                )
            }
        }

        /**
         * Database v4 → v5 마이그레이션
         * 테이블명 capture_queue → insight_queue 변경 (이전 마이그레이션)
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE capture_queue RENAME TO insight_queue"
                )
            }
        }

        /**
         * Database v5 → v6 마이그레이션
         * Todo 테이블 추가 및 SCHEDULE → NOTE 타입 변환
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
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
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_todos_due_date ON todos(due_date)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_todos_is_completed ON todos(is_completed)"
                )
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
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_schedules_date ON schedules(date)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_schedules_category ON schedules(category)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_schedules_google_calendar_id ON schedules(google_calendar_id)"
                )
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
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notes_folder ON notes(folder)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notes_created_at ON notes(created_at)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notes_updated_at ON notes(updated_at)"
                )
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
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_bookmarks_created_at ON bookmarks(created_at)"
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_bookmarks_url ON bookmarks(url)"
                )
            }
        }

        /**
         * Database v7 → v8 마이그레이션
         * Insight → Capture 리네이밍 완료
         * - insight_queue → capture_queue
         * - source_insight_id → source_capture_id
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. insight_queue → capture_queue 리네이밍
                database.execSQL(
                    "ALTER TABLE insight_queue RENAME TO capture_queue"
                )

                // 2. todos 테이블에서 source_insight_id → source_capture_id 변경
                // SQLite는 직접 컬럼 리네이밍을 지원하지 않으므로 테이블 재생성 필요
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS todos_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        content TEXT NOT NULL,
                        title TEXT,
                        source_capture_id TEXT,
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
                database.execSQL(
                    """
                    INSERT INTO todos_new (id, content, title, source_capture_id, due_date, due_time,
                        priority, labels, manual_order, is_completed, completed_at, created_at, updated_at)
                    SELECT id, content, title, source_insight_id, due_date, due_time,
                        priority, labels, manual_order, is_completed, completed_at, created_at, updated_at
                    FROM todos
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE todos")
                database.execSQL("ALTER TABLE todos_new RENAME TO todos")
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_todos_due_date ON todos(due_date)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_todos_is_completed ON todos(is_completed)"
                )

                // 3. schedules 테이블에서 source_insight_id → source_capture_id 변경
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS schedules_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        time TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        location TEXT,
                        category TEXT NOT NULL,
                        google_calendar_id TEXT,
                        source_capture_id TEXT,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO schedules_new (id, title, time, date, location, category,
                        google_calendar_id, source_capture_id, created_at, updated_at)
                    SELECT id, title, time, date, location, category,
                        google_calendar_id, source_insight_id, created_at, updated_at
                    FROM schedules
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE schedules")
                database.execSQL("ALTER TABLE schedules_new RENAME TO schedules")
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_schedules_date ON schedules(date)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_schedules_category ON schedules(category)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_schedules_google_calendar_id ON schedules(google_calendar_id)"
                )

                // 4. notes 테이블에서 source_insight_id → source_capture_id 변경
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notes_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        folder TEXT NOT NULL DEFAULT 'INBOX',
                        tags TEXT,
                        source_capture_id TEXT,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO notes_new (id, title, content, folder, tags, source_capture_id, created_at, updated_at)
                    SELECT id, title, content, folder, tags, source_insight_id, created_at, updated_at
                    FROM notes
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE notes")
                database.execSQL("ALTER TABLE notes_new RENAME TO notes")
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notes_folder ON notes(folder)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notes_created_at ON notes(created_at)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notes_updated_at ON notes(updated_at)"
                )

                // 5. bookmarks 테이블에서 source_insight_id → source_capture_id 변경
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bookmarks_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        url TEXT NOT NULL,
                        summary TEXT,
                        tags TEXT,
                        favicon_url TEXT,
                        source_capture_id TEXT,
                        created_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO bookmarks_new (id, title, url, summary, tags, favicon_url, source_capture_id, created_at)
                    SELECT id, title, url, summary, tags, favicon_url, source_insight_id, created_at
                    FROM bookmarks
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE bookmarks")
                database.execSQL("ALTER TABLE bookmarks_new RENAME TO bookmarks")
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
