package com.example.kairos_mobile.data.remote.api

import com.example.kairos_mobile.data.remote.dto.v2.ClassifyRequest
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyResponse
import com.example.kairos_mobile.data.remote.dto.v2.HealthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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
}
