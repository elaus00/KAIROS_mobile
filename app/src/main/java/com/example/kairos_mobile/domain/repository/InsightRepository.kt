package com.example.kairos_mobile.domain.repository

import android.net.Uri
import com.example.kairos_mobile.data.remote.dto.ai.SuggestedTag
import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.SearchQuery
import com.example.kairos_mobile.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * 인사이트 Repository 인터페이스
 */
interface InsightRepository {

    /**
     * 인사이트를 제출하고 분류 및 Obsidian 노트 생성을 수행
     * 네트워크가 없으면 로컬에 저장
     */
    suspend fun submitInsight(content: String): Result<Insight>

    /**
     * M05: 이미지 인사이트 + OCR
     * 이미지를 OCR로 처리하여 텍스트 추출 후 Insight 제출
     */
    suspend fun submitImageInsight(imageUri: Uri): Result<Insight>

    /**
     * M06: 음성 입력
     * 음성 인식 결과 텍스트로 Insight 제출
     */
    suspend fun submitVoiceInsight(audioText: String, audioUri: Uri? = null): Result<Insight>

    /**
     * M08: 웹 클립
     * URL에서 메타데이터를 추출하여 Insight 제출
     */
    suspend fun submitWebClip(url: String): Result<Insight>

    /**
     * AI 분류 수행
     */
    suspend fun classifyInsight(content: String): Result<Classification>

    /**
     * Obsidian 노트 생성
     */
    suspend fun createObsidianNote(
        classification: Classification,
        content: String
    ): Result<Boolean>

    /**
     * 오프라인 큐에서 대기중인 인사이트 조회
     */
    fun getPendingInsights(): Flow<List<Insight>>

    /**
     * 오프라인 큐 동기화
     */
    suspend fun syncOfflineQueue(): Result<Int>  // 동기화된 항목 수 반환

    /**
     * 특정 인사이트의 동기화 상태 업데이트
     */
    suspend fun updateInsightStatus(
        insightId: String,
        status: SyncStatus,
        error: String? = null
    )

    // ========== Phase 3: 스마트 처리 기능 ==========

    /**
     * M09: AI 요약 생성
     * 긴 콘텐츠를 자동으로 요약
     *
     * @param insightId 인사이트 ID
     * @param content 요약할 콘텐츠
     * @return 요약된 텍스트
     */
    suspend fun generateSummary(insightId: String, content: String): Result<String>

    /**
     * M10: 스마트 태그 제안
     * 과거 패턴 기반 태그 자동 제안
     *
     * @param content 분석할 콘텐츠
     * @param classification 분류 타입 (선택)
     * @return 제안된 태그 리스트
     */
    suspend fun suggestTags(content: String, classification: String?): Result<List<SuggestedTag>>

    // ========== Phase 3: 검색 및 히스토리 기능 ==========

    /**
     * 검색 쿼리로 인사이트 항목 조회
     *
     * @param query 검색 조건
     * @param offset 페이징 시작 위치
     * @param limit 페이지 크기
     * @return 검색된 인사이트 리스트
     */
    suspend fun searchInsights(
        query: SearchQuery,
        offset: Int = 0,
        limit: Int = 20
    ): Result<List<Insight>>

    /**
     * 모든 인사이트 항목 조회 (페이징 지원)
     *
     * @param offset 페이징 시작 위치
     * @param limit 페이지 크기
     * @return 인사이트 리스트 Flow
     */
    fun getAllInsights(
        offset: Int = 0,
        limit: Int = 20
    ): Flow<List<Insight>>

    /**
     * 날짜별로 그룹화된 인사이트 조회
     * Archive 화면에서 사용
     *
     * @return 날짜 키(예: "Today", "2026-01-25")와 인사이트 리스트 맵
     */
    fun getInsightsGroupedByDate(): Flow<Map<String, List<Insight>>>

    /**
     * 특정 ID의 인사이트 조회
     */
    suspend fun getInsightById(id: String): Result<Insight?>

    /**
     * 전체 인사이트 개수 조회
     */
    fun getTotalCount(): Flow<Int>
}
