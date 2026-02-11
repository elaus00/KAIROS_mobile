package com.example.kairos_mobile.presentation.detail

import com.example.kairos_mobile.domain.model.CalendarSyncStatus
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.NoteSubType

/**
 * 캡처 상세 화면 UI 상태
 */
data class CaptureDetailUiState(
    val isLoading: Boolean = false,
    val captureId: String = "",
    val originalText: String = "",
    val aiTitle: String? = null,
    val classifiedType: ClassifiedType = ClassifiedType.TEMP,
    val noteSubType: NoteSubType? = null,
    val imageUri: String? = null,
    val createdAt: Long = 0L,
    val errorMessage: String? = null,
    /** 연관된 Schedule ID (SCHEDULE 타입일 때) */
    val scheduleId: String? = null,
    /** 일정 시작 시각 (Google Calendar 열기용) */
    val scheduleStartTime: Long? = null,
    /** Google Calendar 동기화 상태 (SCHEDULE 타입일 때만 의미) */
    val calendarSyncStatus: CalendarSyncStatus? = null,
    /** 공유용 텍스트 (한 번 소비 후 null 초기화) */
    val shareText: String? = null,
    /** AI 자동 생성 태그 목록 */
    val tags: List<String> = emptyList()
)
