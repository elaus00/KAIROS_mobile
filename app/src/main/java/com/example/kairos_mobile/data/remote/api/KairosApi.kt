package com.example.kairos_mobile.data.remote.api

import com.example.kairos_mobile.data.remote.dto.v2.AnalyticsEventsRequest
import com.example.kairos_mobile.data.remote.dto.v2.AnalyticsEventsResponse
import com.example.kairos_mobile.data.remote.dto.v2.ApiEnvelope
import com.example.kairos_mobile.data.remote.dto.v2.CalendarEventRequest
import com.example.kairos_mobile.data.remote.dto.v2.CalendarEventDeleteResponse
import com.example.kairos_mobile.data.remote.dto.v2.CalendarEventsResponse
import com.example.kairos_mobile.data.remote.dto.v2.CalendarEventResponse
import com.example.kairos_mobile.data.remote.dto.v2.CalendarTokenExchangeRequest
import com.example.kairos_mobile.data.remote.dto.v2.CalendarTokenRequest
import com.example.kairos_mobile.data.remote.dto.v2.CalendarTokenResponse
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyBatchRequest
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyBatchResponse
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyRequest
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyResponse
import com.example.kairos_mobile.data.remote.dto.v2.HealthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * KAIROS API 인터페이스
 */
interface KairosApi {

    /**
     * AI 텍스트 분류
     */
    @POST("classify")
    suspend fun classify(
        @Body request: ClassifyRequest
    ): Response<ApiEnvelope<ClassifyResponse>>

    @POST("classify/batch")
    suspend fun classifyBatch(
        @Body request: ClassifyBatchRequest
    ): Response<ApiEnvelope<ClassifyBatchResponse>>

    /**
     * 서버 상태 확인
     */
    @GET("health")
    suspend fun health(): Response<ApiEnvelope<HealthResponse>>

    /**
     * 분석 이벤트 배치 전송
     */
    @POST("analytics/events")
    suspend fun analyticsEvents(
        @Body request: AnalyticsEventsRequest
    ): Response<ApiEnvelope<AnalyticsEventsResponse>>

    /**
     * Google Calendar 이벤트 생성
     */
    @POST("calendar/events")
    suspend fun createCalendarEvent(
        @Body request: CalendarEventRequest
    ): Response<ApiEnvelope<CalendarEventResponse>>

    @POST("calendar/token/exchange")
    suspend fun exchangeCalendarToken(
        @Body request: CalendarTokenExchangeRequest
    ): Response<ApiEnvelope<CalendarTokenResponse>>

    @POST("calendar/token")
    suspend fun saveCalendarToken(
        @Body request: CalendarTokenRequest
    ): Response<ApiEnvelope<CalendarTokenResponse>>

    @GET("calendar/events")
    suspend fun getCalendarEvents(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Response<ApiEnvelope<CalendarEventsResponse>>

    /**
     * Google Calendar 이벤트 삭제
     */
    @DELETE("calendar/events/{eventId}")
    suspend fun deleteCalendarEvent(
        @Path("eventId") eventId: String
    ): Response<ApiEnvelope<CalendarEventDeleteResponse>>
}
