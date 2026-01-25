package com.example.kairos_mobile.domain.model

import java.util.UUID

/**
 * 캡처 도메인 모델
 */
data class Capture(
    val id: String = UUID.randomUUID().toString(),       // 고유 ID
    val content: String,                                  // 캡처된 내용
    val source: CaptureSource = CaptureSource.TEXT,      // Phase 2: 캡처 소스 타입
    val timestamp: Long = System.currentTimeMillis(),    // 생성 시간
    val syncStatus: SyncStatus = SyncStatus.PENDING,     // 동기화 상태
    val classification: Classification? = null,          // AI 분류 결과
    val error: String? = null,                           // 에러 메시지 (실패 시)

    // Phase 2: 멀티모달 캡처 추가 데이터
    val imageUri: String? = null,                        // M05: 이미지 URI (OCR 소스)
    val audioUri: String? = null,                        // M06: 오디오 URI (STT 소스)
    val webMetadata: WebMetadata? = null,                // M08: 웹 클립 메타데이터

    // Phase 3: 스마트 처리 기능
    val summary: String? = null,                         // M09: AI 생성 요약
    val suggestedTags: List<String> = emptyList(),       // M10: AI 제안 태그
    val appliedTags: List<String> = emptyList()          // M10: 사용자가 선택한 태그
)
