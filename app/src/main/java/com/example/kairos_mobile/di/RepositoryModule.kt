package com.example.kairos_mobile.di

import com.example.kairos_mobile.data.repository.BookmarkRepositoryImpl
import com.example.kairos_mobile.data.repository.ConfigRepositoryImpl
import com.example.kairos_mobile.data.repository.CaptureRepositoryImpl
import com.example.kairos_mobile.data.repository.NoteRepositoryImpl
import com.example.kairos_mobile.data.repository.NotificationRepositoryImpl
import com.example.kairos_mobile.data.repository.PreferencesRepositoryImpl
import com.example.kairos_mobile.data.repository.ScheduleRepositoryImpl
import com.example.kairos_mobile.data.repository.TodoRepositoryImpl
import com.example.kairos_mobile.domain.repository.BookmarkRepository
import com.example.kairos_mobile.domain.repository.ConfigRepository
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.repository.NotificationRepository
import com.example.kairos_mobile.domain.repository.PreferencesRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.repository.TodoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository Hilt 모듈 (PRD v4.0)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * CaptureRepository 바인딩
     */
    @Binds
    @Singleton
    abstract fun bindCaptureRepository(
        impl: CaptureRepositoryImpl
    ): CaptureRepository

    /**
     * ConfigRepository 바인딩
     */
    @Binds
    @Singleton
    abstract fun bindConfigRepository(
        impl: ConfigRepositoryImpl
    ): ConfigRepository

    /**
     * PreferencesRepository 바인딩
     * Phase 3: 스마트 처리 기능 설정 관리
     */
    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository

    /**
     * NotificationRepository 바인딩
     * Phase 3: 알림 관리
     */
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

    /**
     * TodoRepository 바인딩
     * Phase 5: 투두 관리
     */
    @Binds
    @Singleton
    abstract fun bindTodoRepository(
        impl: TodoRepositoryImpl
    ): TodoRepository

    /**
     * ScheduleRepository 바인딩 (PRD v4.0)
     * 일정 관리
     */
    @Binds
    @Singleton
    abstract fun bindScheduleRepository(
        impl: ScheduleRepositoryImpl
    ): ScheduleRepository

    /**
     * NoteRepository 바인딩 (PRD v4.0)
     * 노트 관리
     */
    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository

    /**
     * BookmarkRepository 바인딩 (PRD v4.0)
     * 북마크 관리
     */
    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(
        impl: BookmarkRepositoryImpl
    ): BookmarkRepository
}
