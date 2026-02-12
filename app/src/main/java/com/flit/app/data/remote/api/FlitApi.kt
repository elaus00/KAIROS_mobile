package com.flit.app.data.remote.api

import com.flit.app.data.remote.dto.v2.AnalyticsDashboardResponse
import com.flit.app.data.remote.dto.v2.AnalyticsEventsRequest
import com.flit.app.data.remote.dto.v2.AnalyticsEventsResponse
import com.flit.app.data.remote.dto.v2.ApiEnvelope
import com.flit.app.data.remote.dto.v2.AuthGoogleRequest
import com.flit.app.data.remote.dto.v2.AuthRefreshRequest
import com.flit.app.data.remote.dto.v2.AuthResponse
import com.flit.app.data.remote.dto.v2.ClassifyBatchRequest
import com.flit.app.data.remote.dto.v2.ClassifyBatchResponse
import com.flit.app.data.remote.dto.v2.ClassifyRequest
import com.flit.app.data.remote.dto.v2.ClassifyResponse
import com.flit.app.data.remote.dto.v2.InboxClassifyRequest
import com.flit.app.data.remote.dto.v2.InboxClassifyResponse
import com.flit.app.data.remote.dto.v2.NoteGroupRequest
import com.flit.app.data.remote.dto.v2.NoteGroupResponse
import com.flit.app.data.remote.dto.v2.NoteReorganizeRequest
import com.flit.app.data.remote.dto.v2.NoteReorganizeResponse
import com.flit.app.data.remote.dto.v2.OcrRequest
import com.flit.app.data.remote.dto.v2.OcrResponse
import com.flit.app.data.remote.dto.v2.SemanticSearchRequest
import com.flit.app.data.remote.dto.v2.SemanticSearchResponse
import com.flit.app.data.remote.dto.v2.SubscriptionResponse
import com.flit.app.data.remote.dto.v2.SubscriptionVerifyRequest
import com.flit.app.data.remote.dto.v2.SyncPullRequest
import com.flit.app.data.remote.dto.v2.SyncPullResponse
import com.flit.app.data.remote.dto.v2.SyncPushRequest
import com.flit.app.data.remote.dto.v2.SyncPushResponse
import com.flit.app.data.remote.dto.v2.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Flit API 인터페이스
 */
interface FlitApi {

    /** AI 텍스트 분류 */
    @POST("classify")
    suspend fun classify(
        @Body request: ClassifyRequest
    ): Response<ApiEnvelope<ClassifyResponse>>

    @POST("classify/batch")
    suspend fun classifyBatch(
        @Body request: ClassifyBatchRequest
    ): Response<ApiEnvelope<ClassifyBatchResponse>>

    /** 분석 이벤트 배치 전송 */
    @POST("analytics/events")
    suspend fun analyticsEvents(
        @Body request: AnalyticsEventsRequest
    ): Response<ApiEnvelope<AnalyticsEventsResponse>>

    // --- Auth ---

    /** Google OAuth 로그인 */
    @POST("auth/google")
    suspend fun authGoogle(@Body request: AuthGoogleRequest): Response<ApiEnvelope<AuthResponse>>

    /** JWT 토큰 갱신 */
    @POST("auth/refresh")
    suspend fun authRefresh(@Body request: AuthRefreshRequest): Response<ApiEnvelope<AuthResponse>>

    /** 현재 사용자 정보 조회 */
    @GET("auth/me")
    suspend fun authMe(): Response<ApiEnvelope<UserResponse>>

    // --- Sync ---

    @POST("sync/push")
    suspend fun syncPush(@Body request: SyncPushRequest): Response<ApiEnvelope<SyncPushResponse>>

    @POST("sync/pull")
    suspend fun syncPull(@Body request: SyncPullRequest): Response<ApiEnvelope<SyncPullResponse>>

    // --- Subscription ---

    /** 구독 정보 조회 */
    @GET("subscription")
    suspend fun getSubscription(): Response<ApiEnvelope<SubscriptionResponse>>

    /** 구독 구매 검증 */
    @POST("subscription/verify")
    suspend fun verifySubscription(@Body request: SubscriptionVerifyRequest): Response<ApiEnvelope<SubscriptionResponse>>

    // --- AI 노트 기능 ---

    /** AI 노트 그룹화 */
    @POST("notes/group")
    suspend fun groupNotes(@Body request: NoteGroupRequest): Response<ApiEnvelope<NoteGroupResponse>>

    /** Inbox 자동 분류 */
    @POST("notes/inbox-classify")
    suspend fun inboxClassify(@Body request: InboxClassifyRequest): Response<ApiEnvelope<InboxClassifyResponse>>

    /** 노트 재구성 */
    @POST("notes/reorganize")
    suspend fun reorganizeNotes(@Body request: NoteReorganizeRequest): Response<ApiEnvelope<NoteReorganizeResponse>>

    /** 시맨틱 검색 */
    @POST("notes/search-semantic")
    suspend fun semanticSearch(@Body request: SemanticSearchRequest): Response<ApiEnvelope<SemanticSearchResponse>>

    // --- Analytics Dashboard ---

    /** 분석 대시보드 조회 */
    @GET("analytics/dashboard")
    suspend fun getAnalyticsDashboard(): Response<ApiEnvelope<AnalyticsDashboardResponse>>

    // --- OCR ---

    /** 이미지 OCR */
    @POST("ocr/extract")
    suspend fun ocr(@Body request: OcrRequest): Response<ApiEnvelope<OcrResponse>>
}
