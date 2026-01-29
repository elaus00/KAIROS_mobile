package com.example.kairos_mobile.domain.model

import java.util.UUID

/**
 * 캡처 도메인 모델
 */
data class Capture(
    val id: String = UUID.randomUUID().toString(),       // 고유 ID
    val content: String,                                  // 캡처된 내용
    val source: CaptureSource = CaptureSource.TEXT,      // 캡처 소스 타입
    val timestamp: Long = System.currentTimeMillis(),    // 생성 시간
    val syncStatus: SyncStatus = SyncStatus.PENDING,     // 동기화 상태
    val classification: Classification? = null,          // AI 분류 결과
    val error: String? = null,                           // 에러 메시지 (실패 시)

    // 멀티모달 캡처 추가 데이터
    val imageUri: String? = null,                        // 이미지 URI (OCR 소스)
    val audioUri: String? = null,                        // 오디오 URI (STT 소스)
    val webMetadata: WebMetadata? = null,                // 웹 클립 메타데이터

    // 스마트 처리 기능
    val summary: String? = null,                         // AI 생성 요약
    val suggestedTags: List<String> = emptyList(),       // AI 제안 태그
    val appliedTags: List<String> = emptyList()          // 사용자가 선택한 태그
)
