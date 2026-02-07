package com.example.kairos_mobile.data.remote.api

import com.example.kairos_mobile.data.remote.dto.v2.AnalyticsEventDto
import com.example.kairos_mobile.data.remote.dto.v2.CalendarEventRequest
import com.example.kairos_mobile.data.remote.dto.v2.CalendarEventResponse
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyRequest
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyResponse
import com.example.kairos_mobile.data.remote.dto.v2.HealthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * KAIROS API 인터페이스
 */
interface KairosApi {

    /**
     * AI 텍스트 분류
     */
    @POST("/classify")
    suspend fun classify(
        @Body request: ClassifyRequest
    ): Response<ClassifyResponse>

    /**
     * 서버 상태 확인
     */
    @GET("/health")
    suspend fun health(): Response<HealthResponse>

    /**
     * 분석 이벤트 배치 전송
     */
    @POST("/analytics/events")
    suspend fun analyticsEvents(
        @Body events: List<AnalyticsEventDto>
    ): Response<Unit>

    /**
     * Google Calendar 이벤트 생성
     */
    @POST("api/v2/calendar/events")
    suspend fun createCalendarEvent(
        @Body request: CalendarEventRequest
    ): Response<CalendarEventResponse>

    /**
     * Google Calendar 이벤트 삭제
     */
    @DELETE("api/v2/calendar/events/{eventId}")
    suspend fun deleteCalendarEvent(
        @Path("eventId") eventId: String
    ): Response<Unit>
}
