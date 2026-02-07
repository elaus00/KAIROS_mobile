package com.example.kairos_mobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kairos_mobile.data.local.database.dao.CaptureDao
import com.example.kairos_mobile.data.local.database.dao.CaptureSearchDao
import com.example.kairos_mobile.data.local.database.dao.CaptureTagDao
import com.example.kairos_mobile.data.local.database.dao.ExtractedEntityDao
import com.example.kairos_mobile.data.local.database.dao.FolderDao
import com.example.kairos_mobile.data.local.database.dao.NoteDao
import com.example.kairos_mobile.data.local.database.dao.NotificationDao
import com.example.kairos_mobile.data.local.database.dao.ScheduleDao
import com.example.kairos_mobile.data.local.database.dao.SyncQueueDao
import com.example.kairos_mobile.data.local.database.dao.TagDao
import com.example.kairos_mobile.data.local.database.dao.TodoDao
import com.example.kairos_mobile.data.local.database.dao.UserPreferenceDao
import com.example.kairos_mobile.data.local.database.entities.CaptureEntity
import com.example.kairos_mobile.data.local.database.entities.CaptureSearchFts
import com.example.kairos_mobile.data.local.database.entities.CaptureTagEntity
import com.example.kairos_mobile.data.local.database.entities.ExtractedEntityEntity
import com.example.kairos_mobile.data.local.database.entities.FolderEntity
import com.example.kairos_mobile.data.local.database.entities.NoteEntity
import com.example.kairos_mobile.data.local.database.entities.NotificationEntity
import com.example.kairos_mobile.data.local.database.entities.ScheduleEntity
import com.example.kairos_mobile.data.local.database.entities.SyncQueueEntity
import com.example.kairos_mobile.data.local.database.entities.TagEntity
import com.example.kairos_mobile.data.local.database.entities.TodoEntity
import com.example.kairos_mobile.data.local.database.entities.UserPreferenceEntity

/**
 * KAIROS 앱의 Room Database (Data Model Spec v2.0)
 */
@Database(
    entities = [
        CaptureEntity::class,
        TodoEntity::class,
        ScheduleEntity::class,
        NoteEntity::class,
        FolderEntity::class,
        TagEntity::class,
        CaptureTagEntity::class,
        ExtractedEntityEntity::class,
        SyncQueueEntity::class,
        UserPreferenceEntity::class,
        NotificationEntity::class,
        CaptureSearchFts::class
    ],
    version = 11,
    exportSchema = true
)
abstract class KairosDatabase : RoomDatabase() {

    abstract fun captureDao(): CaptureDao
    abstract fun todoDao(): TodoDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao
    abstract fun captureTagDao(): CaptureTagDao
    abstract fun extractedEntityDao(): ExtractedEntityDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    abstract fun notificationDao(): NotificationDao
    abstract fun captureSearchDao(): CaptureSearchDao

    companion object {
        const val DATABASE_NAME = "kairos_database"

        /**
         * 시스템 폴더 시딩 Callback
         * DB 최초 생성 시 시스템 폴더(Inbox, Ideas, Bookmarks)를 삽입한다.
         */
        val SEED_SYSTEM_FOLDERS = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val now = System.currentTimeMillis()
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO folders (id, name, type, sort_order, created_at)
                    VALUES ('system-inbox', 'Inbox', 'INBOX', 0, $now)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO folders (id, name, type, sort_order, created_at)
                    VALUES ('system-ideas', 'Ideas', 'IDEAS', 1, $now)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO folders (id, name, type, sort_order, created_at)
                    VALUES ('system-bookmarks', 'Bookmarks', 'BOOKMARKS', 2, $now)
                    """.trimIndent()
                )
            }
        }
    }
}
