package com.example.kairos_mobile.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
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
import com.example.kairos_mobile.data.local.database.dao.ScheduleDao
import com.example.kairos_mobile.data.local.database.dao.SyncQueueDao
import com.example.kairos_mobile.data.local.database.dao.TagDao
import com.example.kairos_mobile.data.local.database.dao.TodoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
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

    /**
     * 암호화된 SharedPreferences 제공
     * 사용자 설정값(테마, 온보딩, 임시저장 등)을 암호화하여 저장
     */
    @Provides
    @Singleton
    @Named("encrypted_prefs")
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "kairos_encrypted_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
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
