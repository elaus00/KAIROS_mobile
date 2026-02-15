@file:Suppress("DEPRECATION")

package com.flit.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.flit.app.BuildConfig
import com.flit.app.data.local.database.FlitDatabase
import com.flit.app.data.local.database.dao.AnalyticsEventDao
import com.flit.app.data.local.database.dao.CaptureDao
import com.flit.app.data.local.database.dao.CaptureSearchDao
import com.flit.app.data.local.database.dao.CaptureTagDao
import com.flit.app.data.local.database.dao.ClassificationLogDao
import com.flit.app.data.local.database.dao.ExtractedEntityDao
import com.flit.app.data.local.database.dao.FolderDao
import com.flit.app.data.local.database.dao.NoteDao
import com.flit.app.data.local.database.dao.ScheduleDao
import com.flit.app.data.local.database.dao.SyncQueueDao
import com.flit.app.data.local.database.dao.TagDao
import com.flit.app.data.local.database.dao.TodoDao
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
     * FlitDatabase 제공
     */
    @Provides
    @Singleton
    fun provideFlitDatabase(
        @ApplicationContext context: Context
    ): FlitDatabase {
        val builder = Room.databaseBuilder(
            context,
            FlitDatabase::class.java,
            FlitDatabase.DATABASE_NAME
        )
            .addCallback(FlitDatabase.SEED_SYSTEM_FOLDERS)

        if (BuildConfig.ALLOW_DESTRUCTIVE_MIGRATION) {
            builder.fallbackToDestructiveMigration(dropAllTables = true)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideCaptureDao(database: FlitDatabase): CaptureDao {
        return database.captureDao()
    }

    @Provides
    @Singleton
    fun provideTodoDao(database: FlitDatabase): TodoDao {
        return database.todoDao()
    }

    @Provides
    @Singleton
    fun provideScheduleDao(database: FlitDatabase): ScheduleDao {
        return database.scheduleDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: FlitDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideFolderDao(database: FlitDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    @Singleton
    fun provideTagDao(database: FlitDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideCaptureTagDao(database: FlitDatabase): CaptureTagDao {
        return database.captureTagDao()
    }

    @Provides
    @Singleton
    fun provideExtractedEntityDao(database: FlitDatabase): ExtractedEntityDao {
        return database.extractedEntityDao()
    }

    @Provides
    @Singleton
    fun provideSyncQueueDao(database: FlitDatabase): SyncQueueDao {
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
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "flit_encrypted_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun provideCaptureSearchDao(database: FlitDatabase): CaptureSearchDao {
        return database.captureSearchDao()
    }

    @Provides
    @Singleton
    fun provideClassificationLogDao(database: FlitDatabase): ClassificationLogDao {
        return database.classificationLogDao()
    }

    @Provides
    @Singleton
    fun provideAnalyticsEventDao(database: FlitDatabase): AnalyticsEventDao {
        return database.analyticsEventDao()
    }
}
