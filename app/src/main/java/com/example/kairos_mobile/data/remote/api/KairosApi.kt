package com.example.kairos_mobile.data.remote.api

import com.example.kairos_mobile.data.remote.dto.AuthUrlResponse
import com.example.kairos_mobile.data.remote.dto.ClassificationRequest
import com.example.kairos_mobile.data.remote.dto.ClassificationResponse
import com.example.kairos_mobile.data.remote.dto.OAuthCallbackRequest
import com.example.kairos_mobile.data.remote.dto.OAuthCallbackResponse
import com.example.kairos_mobile.data.remote.dto.ObsidianCreateRequest
import com.example.kairos_mobile.data.remote.dto.ObsidianCreateResponse
import com.example.kairos_mobile.data.remote.dto.OcrResponse
import com.example.kairos_mobile.data.remote.dto.SttResponse
import com.example.kairos_mobile.data.remote.dto.SummarizeRequest
import com.example.kairos_mobile.data.remote.dto.SummarizeResponse
import com.example.kairos_mobile.data.remote.dto.SyncResponse
import com.example.kairos_mobile.data.remote.dto.SyncStatusResponse
import com.example.kairos_mobile.data.remote.dto.TagSuggestRequest
import com.example.kairos_mobile.data.remote.dto.TagSuggestResponse
import com.example.kairos_mobile.data.remote.dto.WebClipRequest
import com.example.kairos_mobile.data.remote.dto.WebClipResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * KAIROS API 인터페이스
 */
interface KairosApi {

    /**
     * 캡처 내용 분류
     * AI 추론 (1-3초)
     */
    @POST("/classify")
    suspend fun classifyCapture(
        @Body request: ClassificationRequest
    ): Response<ClassificationResponse>

    /**
     * Obsidian 노트 생성
     * 파일 I/O (빠름)
     */
    @POST("/notes")
    suspend fun createObsidianNote(
        @Body request: ObsidianCreateRequest
    ): Response<ObsidianCreateResponse>

    /**
     * 이미지 텍스트 추출 (OCR)
     * AI Vision (2-5초)
     */
    @Multipart
    @POST("/ocr")
    suspend fun extractTextFromImage(
        @Part image: MultipartBody.Part
    ): Response<OcrResponse>

    /**
     * 음성 텍스트 추출 (STT)
     * 오디오 파일을 서버로 업로드하여 텍스트로 변환
     * AI 음성인식 (2-5초)
     */
    @Multipart
    @POST("/stt")
    suspend fun extractTextFromAudio(
        @Part audioFile: MultipartBody.Part
    ): Response<SttResponse>

    /**
     * URL 크롤링 + AI 요약 (웹 클립)
     * 크롤링+AI (5-10초)
     */
    @POST("/webclip")
    suspend fun extractWebClip(
        @Body request: WebClipRequest
    ): Response<WebClipResponse>

    /**
     * 서버 상태 확인
     * 즉시 응답
     */
    @GET("/health")
    suspend fun healthCheck(): Response<Unit>

    // ========== Phase 3: 스마트 처리 기능 ==========

    /**
     * M09: AI 요약 생성
     * 긴 콘텐츠를 자동으로 요약
     * AI 추론 (2-5초)
     */
    @POST("/summarize")
    suspend fun generateSummary(
        @Body request: SummarizeRequest
    ): Response<SummarizeResponse>

    /**
     * M10: 스마트 태그 제안
     * 과거 패턴 기반 태그 자동 제안
     * AI 추론 (1-3초)
     */
    @POST("/tags/suggest")
    suspend fun suggestTags(
        @Body request: TagSuggestRequest
    ): Response<TagSuggestResponse>

    // ========== Phase 3: 외부 서비스 연동 ==========

    // ========== M11: Google Calendar 연동 ==========

    /**
     * Google OAuth 인증 URL 조회
     */
    @GET("/integrations/google/auth-url")
    suspend fun getGoogleAuthUrl(): Response<AuthUrlResponse>

    /**
     * Google OAuth 콜백 처리
     */
    @POST("/integrations/google/callback")
    suspend fun handleGoogleCallback(
        @Body request: OAuthCallbackRequest
    ): Response<OAuthCallbackResponse>

    /**
     * Google Calendar 동기화 상태 조회
     */
    @GET("/integrations/google/sync-status")
    suspend fun getGoogleSyncStatus(): Response<SyncStatusResponse>

    /**
     * Google Calendar 수동 동기화 트리거
     */
    @POST("/integrations/google/sync")
    suspend fun triggerGoogleSync(): Response<SyncResponse>

    /**
     * Google Calendar 연동 해제
     */
    @POST("/integrations/google/disconnect")
    suspend fun disconnectGoogle(): Response<OAuthCallbackResponse>

    // ========== M12: Todoist 연동 ==========

    /**
     * Todoist OAuth 인증 URL 조회
     */
    @GET("/integrations/todoist/auth-url")
    suspend fun getTodoistAuthUrl(): Response<AuthUrlResponse>

    /**
     * Todoist OAuth 콜백 처리
     */
    @POST("/integrations/todoist/callback")
    suspend fun handleTodoistCallback(
        @Body request: OAuthCallbackRequest
    ): Response<OAuthCallbackResponse>

    /**
     * Todoist 동기화 상태 조회
     */
    @GET("/integrations/todoist/sync-status")
    suspend fun getTodoistSyncStatus(): Response<SyncStatusResponse>

    /**
     * Todoist 수동 동기화 트리거
     */
    @POST("/integrations/todoist/sync")
    suspend fun triggerTodoistSync(): Response<SyncResponse>

    /**
     * Todoist 연동 해제
     */
    @POST("/integrations/todoist/disconnect")
    suspend fun disconnectTodoist(): Response<OAuthCallbackResponse>
}
