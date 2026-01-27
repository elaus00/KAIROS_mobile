package com.example.kairos_mobile.data.remote.api

import com.example.kairos_mobile.data.remote.dto.ai.ClassificationRequest
import com.example.kairos_mobile.data.remote.dto.ai.ClassificationResponse
import com.example.kairos_mobile.data.remote.dto.obsidian.ObsidianCreateRequest
import com.example.kairos_mobile.data.remote.dto.obsidian.ObsidianCreateResponse
import com.example.kairos_mobile.data.remote.dto.insight.OcrResponse
import com.example.kairos_mobile.data.remote.dto.insight.SttResponse
import com.example.kairos_mobile.data.remote.dto.ai.SummarizeRequest
import com.example.kairos_mobile.data.remote.dto.ai.SummarizeResponse
import com.example.kairos_mobile.data.remote.dto.ai.TagSuggestRequest
import com.example.kairos_mobile.data.remote.dto.ai.TagSuggestResponse
import com.example.kairos_mobile.data.remote.dto.insight.WebClipRequest
import com.example.kairos_mobile.data.remote.dto.insight.WebClipResponse
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
}
