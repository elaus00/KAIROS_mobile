package com.example.kairos_mobile.domain.repository

import android.net.Uri
import com.example.kairos_mobile.data.remote.dto.SuggestedTag
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.Result
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
     * M05: 이미지 캡처 + OCR
     * 이미지를 OCR로 처리하여 텍스트 추출 후 Capture 제출
     */
    suspend fun submitImageCapture(imageUri: Uri): Result<Capture>

    /**
     * M06: 음성 입력
     * 음성 인식 결과 텍스트로 Capture 제출
     */
    suspend fun submitVoiceCapture(audioText: String, audioUri: Uri? = null): Result<Capture>

    /**
     * M08: 웹 클립
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

    // ========== Phase 3: 스마트 처리 기능 ==========

    /**
     * M09: AI 요약 생성
     * 긴 콘텐츠를 자동으로 요약
     *
     * @param captureId 캡처 ID
     * @param content 요약할 콘텐츠
     * @return 요약된 텍스트
     */
    suspend fun generateSummary(captureId: String, content: String): Result<String>

    /**
     * M10: 스마트 태그 제안
     * 과거 패턴 기반 태그 자동 제안
     *
     * @param content 분석할 콘텐츠
     * @param classification 분류 타입 (선택)
     * @return 제안된 태그 리스트
     */
    suspend fun suggestTags(content: String, classification: String?): Result<List<SuggestedTag>>
}
