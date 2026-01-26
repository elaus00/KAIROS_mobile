package com.example.kairos_mobile.domain.usecase

import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.IntegrationsRepository
import com.example.kairos_mobile.domain.repository.SyncResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M12: Todoist 연동 UseCase
 * Todoist OAuth 인증 및 연동 관리
 */
@Singleton
class ConnectTodoistUseCase @Inject constructor(
    private val repository: IntegrationsRepository
) {
    /**
     * Todoist OAuth 인증 URL 조회
     * Chrome Custom Tab에서 열 URL 반환
     */
    suspend fun getAuthUrl(): Result<String> {
        return repository.getTodoistAuthUrl()
    }

    /**
     * OAuth 콜백 처리
     * 인증 완료 후 받은 code를 서버로 전달
     */
    suspend fun handleCallback(code: String, state: String?): Result<Unit> {
        return repository.handleTodoistCallback(code, state)
    }

    /**
     * Todoist 수동 동기화
     */
    suspend fun sync(): Result<SyncResult> {
        return repository.triggerTodoistSync()
    }

    /**
     * Todoist 연동 해제
     */
    suspend fun disconnect(): Result<Unit> {
        return repository.disconnectTodoist()
    }
}
