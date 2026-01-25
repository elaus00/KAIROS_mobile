package com.example.kairos_mobile.presentation.capture

import android.net.Uri
import com.example.kairos_mobile.data.remote.dto.SuggestedTag

/**
 * Phase 2: 캡처 모드
 */
enum class CaptureMode {
    TEXT,       // 텍스트 입력 (Phase 1)
    IMAGE,      // 이미지 + OCR (Phase 2 - M05)
    VOICE,      // 음성 입력 + STT (Phase 2 - M06)
    WEB_CLIP    // 웹 클립 + 메타데이터 추출 (Phase 2 - M08)
}

/**
 * Capture 화면 UI 상태
 */
data class CaptureUiState(
    val inputText: String = "",                  // 입력 텍스트
    val captureMode: CaptureMode = CaptureMode.TEXT,  // Phase 2: 캡처 모드
    val selectedImageUri: Uri? = null,           // Phase 2: 선택된 이미지 URI
    val isRecording: Boolean = false,            // Phase 2: 음성 녹음 중 여부
    val isLoading: Boolean = false,              // 로딩 중 여부
    val showSuccessFeedback: Boolean = false,    // 성공 피드백 표시 여부
    val errorMessage: String? = null,            // 에러 메시지
    val pendingCount: Int = 0,                   // 오프라인 큐 대기 개수
    val isOffline: Boolean = false,              // 오프라인 모드 여부
    val showPermissionRationale: Boolean = false, // Phase 2: 권한 설명 표시 여부

    // Phase 3: 스마트 처리 기능
    val latestSummary: String? = null,           // M09: 최근 생성된 요약
    val suggestedTags: List<SuggestedTag> = emptyList(),  // M10: AI 제안 태그
    val isSummaryExpanded: Boolean = false       // M09: 요약 펼침 상태
)
