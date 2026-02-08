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
    /** Google Calendar 동기화 상태 (SCHEDULE 타입일 때만 의미) */
    val calendarSyncStatus: CalendarSyncStatus? = null
)
