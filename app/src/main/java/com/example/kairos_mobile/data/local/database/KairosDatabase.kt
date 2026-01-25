package com.example.kairos_mobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kairos_mobile.data.local.database.dao.CaptureQueueDao
import com.example.kairos_mobile.data.local.database.entities.CaptureQueueEntity

/**
 * KAIROS 앱의 Room Database
 */
@Database(
    entities = [CaptureQueueEntity::class],
    version = 3,  // Phase 3: 스마트 처리 기능 지원으로 버전 업
    exportSchema = true
)
abstract class KairosDatabase : RoomDatabase() {

    /**
     * 캡처 큐 DAO
     */
    abstract fun captureQueueDao(): CaptureQueueDao

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
    }
}
