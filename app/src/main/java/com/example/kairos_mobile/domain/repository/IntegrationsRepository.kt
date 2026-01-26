package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.Result

/**
 * 외부 서비스 연동 Repository 인터페이스
 * Phase 3: M11 (Google Calendar), M12 (Todoist) 연동 관리
 */
interface IntegrationsRepository {

    // ========== M11: Google Calendar 연동 ==========

    /**
     * Google OAuth 인증 URL 조회
     * 서버에서 생성한 OAuth 2.0 인증 URL 반환
     */
    suspend fun getGoogleAuthUrl(): Result<String>

    /**
     * Google OAuth 콜백 처리
     * OAuth 인증 완료 후 받은 code를 서버로 전달하여 토큰 교환
     */
    suspend fun handleGoogleCallback(code: String, state: String?): Result<Unit>

    /**
     * Google Calendar 동기화 상태 조회
     */
    suspend fun getGoogleSyncStatus(): Result<SyncStatus>

    /**
     * Google Calendar 수동 동기화 트리거
     */
    suspend fun triggerGoogleSync(): Result<SyncResult>

    /**
     * Google Calendar 연동 해제
     */
    suspend fun disconnectGoogle(): Result<Unit>

    // ========== M12: Todoist 연동 ==========

    /**
     * Todoist OAuth 인증 URL 조회
     */
    suspend fun getTodoistAuthUrl(): Result<String>

    /**
     * Todoist OAuth 콜백 처리
     */
    suspend fun handleTodoistCallback(code: String, state: String?): Result<Unit>

    /**
     * Todoist 동기화 상태 조회
     */
    suspend fun getTodoistSyncStatus(): Result<SyncStatus>

    /**
     * Todoist 수동 동기화 트리거
     */
    suspend fun triggerTodoistSync(): Result<SyncResult>

    /**
     * Todoist 연동 해제
     */
    suspend fun disconnectTodoist(): Result<Unit>
}

/**
 * 외부 서비스 동기화 상태
 */
data class SyncStatus(
    val isConnected: Boolean,
    val lastSyncTime: Long?,
    val syncedCount: Int
)

/**
 * 동기화 결과
 */
data class SyncResult(
    val success: Boolean,
    val syncedCount: Int,
    val message: String?
)
