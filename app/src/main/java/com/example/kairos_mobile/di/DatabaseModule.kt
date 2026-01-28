package com.example.kairos_mobile.di

import android.content.Context
import androidx.room.Room
import com.example.kairos_mobile.data.local.database.KairosDatabase
import com.example.kairos_mobile.data.local.database.dao.BookmarkDao
import com.example.kairos_mobile.data.local.database.dao.InsightQueueDao
import com.example.kairos_mobile.data.local.database.dao.NoteDao
import com.example.kairos_mobile.data.local.database.dao.NotificationDao
import com.example.kairos_mobile.data.local.database.dao.ScheduleDao
import com.example.kairos_mobile.data.local.database.dao.TodoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database Hilt 모듈 (PRD v4.0)
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
        return Room.databaseBuilder(
            context,
            KairosDatabase::class.java,
            KairosDatabase.DATABASE_NAME
        )
            .addMigrations(
                KairosDatabase.MIGRATION_1_2,  // Phase 2: 멀티모달 캡처
                KairosDatabase.MIGRATION_2_3,  // Phase 3: 스마트 처리 기능
                KairosDatabase.MIGRATION_3_4,  // Phase 3: 알림 기능
                KairosDatabase.MIGRATION_4_5,  // Phase 4: Capture → Insight 리네이밍
                KairosDatabase.MIGRATION_5_6,  // Phase 5: Todo 테이블, SCHEDULE → NOTE
                KairosDatabase.MIGRATION_6_7   // PRD v4.0: Schedule, Note, Bookmark 테이블
            )
            .fallbackToDestructiveMigration()  // 마이그레이션 실패 시 폴백
            .build()
    }

    /**
     * InsightQueueDao 제공
     */
    @Provides
    @Singleton
    fun provideInsightQueueDao(database: KairosDatabase): InsightQueueDao {
        return database.insightQueueDao()
    }

    /**
     * NotificationDao 제공
     */
    @Provides
    @Singleton
    fun provideNotificationDao(database: KairosDatabase): NotificationDao {
        return database.notificationDao()
    }

    /**
     * TodoDao 제공
     */
    @Provides
    @Singleton
    fun provideTodoDao(database: KairosDatabase): TodoDao {
        return database.todoDao()
    }

    /**
     * ScheduleDao 제공 (PRD v4.0)
     */
    @Provides
    @Singleton
    fun provideScheduleDao(database: KairosDatabase): ScheduleDao {
        return database.scheduleDao()
    }

    /**
     * NoteDao 제공 (PRD v4.0)
     */
    @Provides
    @Singleton
    fun provideNoteDao(database: KairosDatabase): NoteDao {
        return database.noteDao()
    }

    /**
     * BookmarkDao 제공 (PRD v4.0)
     */
    @Provides
    @Singleton
    fun provideBookmarkDao(database: KairosDatabase): BookmarkDao {
        return database.bookmarkDao()
    }
}
