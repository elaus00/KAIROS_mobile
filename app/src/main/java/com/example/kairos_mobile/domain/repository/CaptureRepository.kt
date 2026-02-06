package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.ConfidenceLevel
import com.example.kairos_mobile.domain.model.NoteSubType
import kotlinx.coroutines.flow.Flow

/**
 * 캡처 Repository 인터페이스
 */
interface CaptureRepository {

    /** 캡처 저장 (classified_type=TEMP) */
    suspend fun saveCapture(capture: Capture): Capture

    /** 캡처 조회 */
    suspend fun getCaptureById(id: String): Capture?

    /** AI 분류 결과 업데이트 */
    suspend fun updateClassification(
        captureId: String,
        classifiedType: ClassifiedType,
        noteSubType: NoteSubType?,
        aiTitle: String,
        confidence: ConfidenceLevel
    )

    /** 분류 유형 변경 (사용자 수동 수정) */
    suspend fun updateClassifiedType(
        captureId: String,
        classifiedType: ClassifiedType,
        noteSubType: NoteSubType?
    )

    /** AI 분류 확인 처리 */
    suspend fun confirmClassification(captureId: String)

    /** 전체 미확인 분류 일괄 확인 */
    suspend fun confirmAllClassifications()

    /** 소프트 삭제 (Snackbar 실행 취소용) */
    suspend fun softDelete(captureId: String)

    /** 소프트 삭제 실행 취소 */
    suspend fun undoSoftDelete(captureId: String)

    /** 하드 삭제 (DB 완전 삭제 + 파생 엔티티 삭제) */
    suspend fun hardDelete(captureId: String)

    /** 미확인 AI 분류 항목 조회 (24시간 이내) */
    fun getUnconfirmedClassifications(): Flow<List<Capture>>

    /** 미확인 AI 분류 항목 수 */
    fun getUnconfirmedCount(): Flow<Int>

    /** 전체 캡처 조회 (페이지네이션, 삭제 제외) */
    fun getAllCaptures(offset: Int = 0, limit: Int = 20): Flow<List<Capture>>

    /** TEMP 상태 캡처 조회 (재분류 대상) */
    suspend fun getTempCaptures(): List<Capture>

    /** FTS 검색 */
    fun searchCaptures(query: String): Flow<List<Capture>>
}
