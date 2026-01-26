package com.example.kairos_mobile.domain.usecase

import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.IntegrationsRepository
import com.example.kairos_mobile.domain.repository.SyncStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M11/M12: 외부 서비스 동기화 상태 조회 UseCase
 * Google Calendar 및 Todoist 연동 상태 확인
 */
@Singleton
class GetSyncStatusUseCase @Inject constructor(
    private val repository: IntegrationsRepository
) {
    /**
     * Google Calendar 동기화 상태 조회
     */
    suspend fun getGoogleSyncStatus(): Result<SyncStatus> {
        return repository.getGoogleSyncStatus()
    }

    /**
     * Todoist 동기화 상태 조회
     */
    suspend fun getTodoistSyncStatus(): Result<SyncStatus> {
        return repository.getTodoistSyncStatus()
    }
}
