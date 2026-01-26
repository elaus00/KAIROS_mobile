package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.OAuthCallbackRequest
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.IntegrationsRepository
import com.example.kairos_mobile.domain.repository.SyncResult
import com.example.kairos_mobile.domain.repository.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 외부 서비스 연동 Repository 구현체
 * Phase 3: M11 (Google Calendar), M12 (Todoist) 연동 관리
 */
@Singleton
class IntegrationsRepositoryImpl @Inject constructor(
    private val api: KairosApi
) : IntegrationsRepository {

    // ========== M11: Google Calendar 연동 ==========

    override suspend fun getGoogleAuthUrl(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGoogleAuthUrl()

            if (response.isSuccessful && response.body()?.success == true) {
                val authUrl = response.body()?.authUrl
                if (authUrl != null) {
                    Result.Success(authUrl)
                } else {
                    Result.Error(Exception("인증 URL을 받지 못했습니다"))
                }
            } else {
                val errorMessage = response.body()?.error ?: "Google 인증 URL 조회 실패"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun handleGoogleCallback(code: String, state: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val request = OAuthCallbackRequest(code, state)
                val response = api.handleGoogleCallback(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    Result.Success(Unit)
                } else {
                    val errorMessage = response.body()?.error ?: "Google 인증 콜백 처리 실패"
                    Result.Error(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getGoogleSyncStatus(): Result<SyncStatus> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGoogleSyncStatus()

            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                Result.Success(
                    SyncStatus(
                        isConnected = body.isConnected,
                        lastSyncTime = body.lastSyncTime,
                        syncedCount = body.syncedCount
                    )
                )
            } else {
                val errorMessage = response.body()?.error ?: "Google 동기화 상태 조회 실패"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun triggerGoogleSync(): Result<SyncResult> = withContext(Dispatchers.IO) {
        try {
            val response = api.triggerGoogleSync()

            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                Result.Success(
                    SyncResult(
                        success = body.success,
                        syncedCount = body.syncedCount,
                        message = body.message
                    )
                )
            } else {
                val errorMessage = response.body()?.error ?: "Google Calendar 동기화 실패"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun disconnectGoogle(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.disconnectGoogle()

            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                val errorMessage = response.body()?.error ?: "Google 연동 해제 실패"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // ========== M12: Todoist 연동 ==========

    override suspend fun getTodoistAuthUrl(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTodoistAuthUrl()

            if (response.isSuccessful && response.body()?.success == true) {
                val authUrl = response.body()?.authUrl
                if (authUrl != null) {
                    Result.Success(authUrl)
                } else {
                    Result.Error(Exception("인증 URL을 받지 못했습니다"))
                }
            } else {
                val errorMessage = response.body()?.error ?: "Todoist 인증 URL 조회 실패"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun handleTodoistCallback(code: String, state: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val request = OAuthCallbackRequest(code, state)
                val response = api.handleTodoistCallback(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    Result.Success(Unit)
                } else {
                    val errorMessage = response.body()?.error ?: "Todoist 인증 콜백 처리 실패"
                    Result.Error(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getTodoistSyncStatus(): Result<SyncStatus> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTodoistSyncStatus()

            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                Result.Success(
                    SyncStatus(
                        isConnected = body.isConnected,
                        lastSyncTime = body.lastSyncTime,
                        syncedCount = body.syncedCount
                    )
                )
            } else {
                val errorMessage = response.body()?.error ?: "Todoist 동기화 상태 조회 실패"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun triggerTodoistSync(): Result<SyncResult> = withContext(Dispatchers.IO) {
        try {
            val response = api.triggerTodoistSync()

            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                Result.Success(
                    SyncResult(
                        success = body.success,
                        syncedCount = body.syncedCount,
                        message = body.message
                    )
                )
            } else {
                val errorMessage = response.body()?.error ?: "Todoist 동기화 실패"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun disconnectTodoist(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.disconnectTodoist()

            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(Unit)
            } else {
                val errorMessage = response.body()?.error ?: "Todoist 연동 해제 실패"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
