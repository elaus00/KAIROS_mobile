package com.example.kairos_mobile.data.remote.api

import com.example.kairos_mobile.data.remote.dto.v2.ClassifyRequest
import com.example.kairos_mobile.data.remote.dto.v2.ClassifyResponse
import com.example.kairos_mobile.data.remote.dto.v2.ClipRequest
import com.example.kairos_mobile.data.remote.dto.v2.ClipResponse
import com.example.kairos_mobile.data.remote.dto.v2.HealthResponse
import com.example.kairos_mobile.data.remote.dto.v2.NoteCreateRequest
import com.example.kairos_mobile.data.remote.dto.v2.NoteCreateResponse
import com.example.kairos_mobile.data.remote.dto.v2.OcrRequest
import com.example.kairos_mobile.data.remote.dto.v2.OcrResponse
import com.example.kairos_mobile.data.remote.dto.v2.SttRequest
import com.example.kairos_mobile.data.remote.dto.v2.SttResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * KAIROS API 인터페이스 (API v2.1)
 * Base URL: http://localhost:8000 (에뮬레이터: http://10.0.2.2:8000)
 */
interface KairosApi {

    /**
     * 콘텐츠 분류
     * AI가 입력 내용을 분석하여 타입과 목적지를 결정
     */
    @POST("/classify")
    suspend fun classify(
        @Body request: ClassifyRequest
    ): Response<ClassifyResponse>

    /**
     * Obsidian 노트 생성
     * 분류된 콘텐츠를 Obsidian vault에 저장
     */
    @POST("/notes")
    suspend fun createNote(
        @Body request: NoteCreateRequest
    ): Response<NoteCreateResponse>

    /**
     * 이미지 텍스트 추출 (OCR)
     * Base64 인코딩된 이미지에서 텍스트 추출
     */
    @POST("/ocr")
    suspend fun ocr(
        @Body request: OcrRequest
    ): Response<OcrResponse>

    /**
     * 음성 텍스트 추출 (STT)
     * Base64 인코딩된 오디오에서 텍스트 추출
     */
    @POST("/stt")
    suspend fun stt(
        @Body request: SttRequest
    ): Response<SttResponse>

    /**
     * 웹 클립
     * URL에서 콘텐츠와 메타데이터 추출
     */
    @POST("/clip")
    suspend fun clip(
        @Body request: ClipRequest
    ): Response<ClipResponse>

    /**
     * 서버 상태 확인
     */
    @GET("/health")
    suspend fun health(): Response<HealthResponse>
}
