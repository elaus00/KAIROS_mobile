package com.example.kairos_mobile.domain.repository

import android.net.Uri
import com.example.kairos_mobile.data.remote.dto.ai.SuggestedTag
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.SearchQuery
import com.example.kairos_mobile.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * 캡처 Repository 인터페이스
 */
interface CaptureRepository {

    /**
     * 캡처를 제출하고 분류 및 Obsidian 노트 생성을 수행
     * 네트워크가 없으면 로컬에 저장
     */
    suspend fun submitCapture(content: String): Result<Capture>

    /**
     * 이미지 캡처 + OCR
     * 이미지를 OCR로 처리하여 텍스트 추출 후 Capture 제출
     */
    suspend fun submitImageCapture(imageUri: Uri): Result<Capture>

    /**
     * 음성 입력
     * 음성 인식 결과 텍스트로 Capture 제출
     */
    suspend fun submitVoiceCapture(audioText: String, audioUri: Uri? = null): Result<Capture>

    /**
     * 웹 클립
     * URL에서 메타데이터를 추출하여 Capture 제출
     */
    suspend fun submitWebClip(url: String): Result<Capture>

    /**
     * AI 분류 수행
     */
    suspend fun classifyCapture(content: String): Result<Classification>

    /**
     * Obsidian 노트 생성
     */
    suspend fun createObsidianNote(
        classification: Classification,
        content: String
    ): Result<Boolean>

    /**
     * 오프라인 큐에서 대기중인 캡처 조회
     */
    fun getPendingCaptures(): Flow<List<Capture>>

    /**
     * 오프라인 큐 동기화
     */
    suspend fun syncOfflineQueue(): Result<Int>  // 동기화된 항목 수 반환

    /**
     * 특정 캡처의 동기화 상태 업데이트
     */
    suspend fun updateCaptureStatus(
        captureId: String,
        status: SyncStatus,
        error: String? = null
    )

    // ========== 스마트 처리 기능 ==========

    /**
     * AI 요약 생성
     * 긴 콘텐츠를 자동으로 요약
     *
     * @param captureId 캡처 ID
     * @param content 요약할 콘텐츠
     * @return 요약된 텍스트
     */
    suspend fun generateSummary(captureId: String, content: String): Result<String>

    /**
     * 스마트 태그 제안
     * 과거 패턴 기반 태그 자동 제안
     *
     * @param content 분석할 콘텐츠
     * @param classification 분류 타입 (선택)
     * @return 제안된 태그 리스트
     */
    suspend fun suggestTags(content: String, classification: String?): Result<List<SuggestedTag>>

    // ========== 검색 및 히스토리 기능 ==========

    /**
     * 검색 쿼리로 캡처 항목 조회
     *
     * @param query 검색 조건
     * @param offset 페이징 시작 위치
     * @param limit 페이지 크기
     * @return 검색된 캡처 리스트
     */
    suspend fun searchCaptures(
        query: SearchQuery,
        offset: Int = 0,
        limit: Int = 20
    ): Result<List<Capture>>

    /**
     * 모든 캡처 항목 조회 (페이징 지원)
     *
     * @param offset 페이징 시작 위치
     * @param limit 페이지 크기
     * @return 캡처 리스트 Flow
     */
    fun getAllCaptures(
        offset: Int = 0,
        limit: Int = 20
    ): Flow<List<Capture>>

    /**
     * 날짜별로 그룹화된 캡처 조회
     * Archive 화면에서 사용
     *
     * @return 날짜 키(예: "Today", "2026-01-25")와 캡처 리스트 맵
     */
    fun getCapturesGroupedByDate(): Flow<Map<String, List<Capture>>>

    /**
     * 특정 ID의 캡처 조회
     */
    suspend fun getCaptureById(id: String): Result<Capture?>

    /**
     * 전체 캡처 개수 조회
     */
    fun getTotalCount(): Flow<Int>

    // ========== PRD v4.0: 추가 기능 ==========

    /**
     * 최근 캡처 조회 (Home 화면용)
     *
     * @param limit 최대 개수
     * @return 최근 캡처 리스트
     */
    fun getRecentCaptures(limit: Int = 6): Flow<List<Capture>>

    /**
     * 캡처 저장 (타입 자동 분류)
     *
     * @param content 캡처 내용
     * @return 저장된 캡처
     */
    suspend fun saveCapture(content: String): Result<Capture>

    /**
     * 캡처 저장 (타입 지정)
     *
     * @param content 캡처 내용
     * @param type 캡처 타입
     * @return 저장된 캡처
     */
    suspend fun saveCaptureWithType(content: String, type: CaptureType): Result<Capture>
}
