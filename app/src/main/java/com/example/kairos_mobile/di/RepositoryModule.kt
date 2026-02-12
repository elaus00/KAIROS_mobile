package com.example.kairos_mobile.di

import com.example.kairos_mobile.data.notification.NotificationHelper
import com.example.kairos_mobile.data.repository.AuthRepositoryImpl
import com.example.kairos_mobile.data.repository.AnalyticsRepositoryImpl
import com.example.kairos_mobile.data.repository.NoteAiRepositoryImpl
import com.example.kairos_mobile.data.repository.SubscriptionRepositoryImpl
import com.example.kairos_mobile.data.repository.CalendarRepositoryImpl
import com.example.kairos_mobile.data.repository.ImageRepositoryImpl
import com.example.kairos_mobile.data.repository.CaptureRepositoryImpl
import com.example.kairos_mobile.data.repository.ClassificationLogRepositoryImpl
import com.example.kairos_mobile.data.repository.ExtractedEntityRepositoryImpl
import com.example.kairos_mobile.data.repository.FolderRepositoryImpl
import com.example.kairos_mobile.data.repository.NoteRepositoryImpl
import com.example.kairos_mobile.data.repository.ScheduleRepositoryImpl
import com.example.kairos_mobile.data.repository.SyncQueueRepositoryImpl
import com.example.kairos_mobile.data.repository.SyncRepositoryImpl
import com.example.kairos_mobile.data.repository.TagRepositoryImpl
import com.example.kairos_mobile.data.repository.TodoRepositoryImpl
import com.example.kairos_mobile.data.repository.UserPreferenceRepositoryImpl
import com.example.kairos_mobile.domain.repository.AuthRepository
import com.example.kairos_mobile.domain.repository.AnalyticsRepository
import com.example.kairos_mobile.domain.repository.NoteAiRepository
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import com.example.kairos_mobile.domain.repository.CalendarRepository
import com.example.kairos_mobile.domain.repository.ImageRepository
import com.example.kairos_mobile.domain.usecase.calendar.CalendarNotifier
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ClassificationLogRepository
import com.example.kairos_mobile.domain.repository.ExtractedEntityRepository
import com.example.kairos_mobile.domain.repository.FolderRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.repository.SyncRepository
import com.example.kairos_mobile.domain.repository.SyncQueueRepository
import com.example.kairos_mobile.domain.repository.TagRepository
import com.example.kairos_mobile.domain.repository.TodoRepository
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository Hilt 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCaptureRepository(
        impl: CaptureRepositoryImpl
    ): CaptureRepository

    @Binds
    @Singleton
    abstract fun bindExtractedEntityRepository(
        impl: ExtractedEntityRepositoryImpl
    ): ExtractedEntityRepository

    @Binds
    @Singleton
    abstract fun bindTodoRepository(
        impl: TodoRepositoryImpl
    ): TodoRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(
        impl: ScheduleRepositoryImpl
    ): ScheduleRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository

    @Binds
    @Singleton
    abstract fun bindFolderRepository(
        impl: FolderRepositoryImpl
    ): FolderRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(
        impl: TagRepositoryImpl
    ): TagRepository

    @Binds
    @Singleton
    abstract fun bindSyncQueueRepository(
        impl: SyncQueueRepositoryImpl
    ): SyncQueueRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        impl: SyncRepositoryImpl
    ): SyncRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferenceRepository(
        impl: UserPreferenceRepositoryImpl
    ): UserPreferenceRepository

    @Binds
    @Singleton
    abstract fun bindClassificationLogRepository(
        impl: ClassificationLogRepositoryImpl
    ): ClassificationLogRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        impl: AnalyticsRepositoryImpl
    ): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(
        impl: ImageRepositoryImpl
    ): ImageRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(
        impl: CalendarRepositoryImpl
    ): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindCalendarNotifier(
        impl: NotificationHelper
    ): CalendarNotifier

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        impl: SubscriptionRepositoryImpl
    ): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindNoteAiRepository(
        impl: NoteAiRepositoryImpl
    ): NoteAiRepository
}
