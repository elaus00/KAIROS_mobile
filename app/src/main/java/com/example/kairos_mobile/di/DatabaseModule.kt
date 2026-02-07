package com.example.kairos_mobile.di

import android.content.Context
import androidx.room.Room
import com.example.kairos_mobile.BuildConfig
import com.example.kairos_mobile.data.local.database.KairosDatabase
import com.example.kairos_mobile.data.local.database.dao.AnalyticsEventDao
import com.example.kairos_mobile.data.local.database.dao.CaptureDao
import com.example.kairos_mobile.data.local.database.dao.CaptureSearchDao
import com.example.kairos_mobile.data.local.database.dao.CaptureTagDao
import com.example.kairos_mobile.data.local.database.dao.ClassificationLogDao
import com.example.kairos_mobile.data.local.database.dao.ExtractedEntityDao
import com.example.kairos_mobile.data.local.database.dao.FolderDao
import com.example.kairos_mobile.data.local.database.dao.NoteDao
import com.example.kairos_mobile.data.local.database.dao.NotificationDao
import com.example.kairos_mobile.data.local.database.dao.ScheduleDao
import com.example.kairos_mobile.data.local.database.dao.SyncQueueDao
import com.example.kairos_mobile.data.local.database.dao.TagDao
import com.example.kairos_mobile.data.local.database.dao.TodoDao
import com.example.kairos_mobile.data.local.database.dao.UserPreferenceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database Hilt 모듈 (Data Model Spec v2.0)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * KairosDatabase 제공
     */
    @Provides
    @Singleton
    fun provideKairosDatabase(
        @ApplicationContext context: Context
    ): KairosDatabase {
        val builder = Room.databaseBuilder(
            context,
            KairosDatabase::class.java,
            KairosDatabase.DATABASE_NAME
        )
            .addCallback(KairosDatabase.SEED_SYSTEM_FOLDERS)

        if (BuildConfig.ALLOW_DESTRUCTIVE_MIGRATION) {
            builder.fallbackToDestructiveMigration()
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideCaptureDao(database: KairosDatabase): CaptureDao {
        return database.captureDao()
    }

    @Provides
    @Singleton
    fun provideTodoDao(database: KairosDatabase): TodoDao {
        return database.todoDao()
    }

    @Provides
    @Singleton
    fun provideScheduleDao(database: KairosDatabase): ScheduleDao {
        return database.scheduleDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: KairosDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideFolderDao(database: KairosDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    @Singleton
    fun provideTagDao(database: KairosDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideCaptureTagDao(database: KairosDatabase): CaptureTagDao {
        return database.captureTagDao()
    }

    @Provides
    @Singleton
    fun provideExtractedEntityDao(database: KairosDatabase): ExtractedEntityDao {
        return database.extractedEntityDao()
    }

    @Provides
    @Singleton
    fun provideSyncQueueDao(database: KairosDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferenceDao(database: KairosDatabase): UserPreferenceDao {
        return database.userPreferenceDao()
    }

    @Provides
    @Singleton
    fun provideNotificationDao(database: KairosDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    @Singleton
    fun provideCaptureSearchDao(database: KairosDatabase): CaptureSearchDao {
        return database.captureSearchDao()
    }

    @Provides
    @Singleton
    fun provideClassificationLogDao(database: KairosDatabase): ClassificationLogDao {
        return database.classificationLogDao()
    }

    @Provides
    @Singleton
    fun provideAnalyticsEventDao(database: KairosDatabase): AnalyticsEventDao {
        return database.analyticsEventDao()
    }
}
