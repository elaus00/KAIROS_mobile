package com.flit.app.di

import com.flit.app.data.notification.NotificationHelper
import com.flit.app.data.repository.AuthRepositoryImpl
import com.flit.app.data.repository.AnalyticsRepositoryImpl
import com.flit.app.data.repository.NoteAiRepositoryImpl
import com.flit.app.data.repository.SubscriptionRepositoryImpl
import com.flit.app.data.repository.CalendarRepositoryImpl
import com.flit.app.data.repository.ImageRepositoryImpl
import com.flit.app.data.repository.CaptureRepositoryImpl
import com.flit.app.data.repository.ClassificationLogRepositoryImpl
import com.flit.app.data.repository.ExtractedEntityRepositoryImpl
import com.flit.app.data.repository.FolderRepositoryImpl
import com.flit.app.data.repository.NoteRepositoryImpl
import com.flit.app.data.repository.ScheduleRepositoryImpl
import com.flit.app.data.repository.SyncQueueRepositoryImpl
import com.flit.app.data.repository.SyncRepositoryImpl
import com.flit.app.data.repository.TagRepositoryImpl
import com.flit.app.data.repository.TodoRepositoryImpl
import com.flit.app.data.repository.UserPreferenceRepositoryImpl
import com.flit.app.data.repository.RoomTransactionRunner
import com.flit.app.domain.repository.AuthRepository
import com.flit.app.domain.repository.AnalyticsRepository
import com.flit.app.domain.repository.NoteAiRepository
import com.flit.app.domain.repository.SubscriptionRepository
import com.flit.app.domain.repository.CalendarRepository
import com.flit.app.domain.repository.ImageRepository
import com.flit.app.domain.usecase.calendar.CalendarNotifier
import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.repository.ClassificationLogRepository
import com.flit.app.domain.repository.ExtractedEntityRepository
import com.flit.app.domain.repository.FolderRepository
import com.flit.app.domain.repository.NoteRepository
import com.flit.app.domain.repository.ScheduleRepository
import com.flit.app.domain.repository.SyncRepository
import com.flit.app.domain.repository.SyncQueueRepository
import com.flit.app.domain.repository.TagRepository
import com.flit.app.domain.repository.TodoRepository
import com.flit.app.domain.repository.TransactionRunner
import com.flit.app.domain.repository.UserPreferenceRepository
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

    @Binds
    @Singleton
    abstract fun bindTransactionRunner(
        impl: RoomTransactionRunner
    ): TransactionRunner
}
