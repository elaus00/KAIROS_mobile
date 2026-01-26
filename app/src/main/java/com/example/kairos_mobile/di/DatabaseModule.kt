package com.example.kairos_mobile.di

import android.content.Context
import androidx.room.Room
import com.example.kairos_mobile.data.local.database.KairosDatabase
import com.example.kairos_mobile.data.local.database.dao.CaptureQueueDao
import com.example.kairos_mobile.data.local.database.dao.NotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database Hilt 모듈
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
                KairosDatabase.MIGRATION_3_4   // Phase 3: 알림 기능
            )
            .fallbackToDestructiveMigration()  // 마이그레이션 실패 시 폴백
            .build()
    }

    /**
     * CaptureQueueDao 제공
     */
    @Provides
    @Singleton
    fun provideCaptureQueueDao(database: KairosDatabase): CaptureQueueDao {
        return database.captureQueueDao()
    }

    /**
     * NotificationDao 제공
     */
    @Provides
    @Singleton
    fun provideNotificationDao(database: KairosDatabase): NotificationDao {
        return database.notificationDao()
    }
}
