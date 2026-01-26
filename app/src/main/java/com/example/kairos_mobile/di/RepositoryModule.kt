package com.example.kairos_mobile.di

import com.example.kairos_mobile.data.repository.CaptureRepositoryImpl
import com.example.kairos_mobile.data.repository.ConfigRepositoryImpl
import com.example.kairos_mobile.data.repository.IntegrationsRepositoryImpl
import com.example.kairos_mobile.data.repository.NotificationRepositoryImpl
import com.example.kairos_mobile.data.repository.PreferencesRepositoryImpl
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ConfigRepository
import com.example.kairos_mobile.domain.repository.IntegrationsRepository
import com.example.kairos_mobile.domain.repository.NotificationRepository
import com.example.kairos_mobile.domain.repository.PreferencesRepository
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
     * IntegrationsRepository 바인딩
     * Phase 3: M11/M12 외부 서비스 연동 관리
     */
    @Binds
    @Singleton
    abstract fun bindIntegrationsRepository(
        impl: IntegrationsRepositoryImpl
    ): IntegrationsRepository

    /**
     * NotificationRepository 바인딩
     * Phase 3: 알림 관리
     */
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository
}
