package com.flit.app.domain.repository

import com.flit.app.domain.model.Capture
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.ConfidenceLevel
import com.flit.app.domain.model.NoteSubType
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

    /** 휴지통으로 이동 */
    suspend fun moveToTrash(captureId: String)

    /** 휴지통에서 복원 */
    suspend fun restoreFromTrash(captureId: String)

    /** 휴지통 항목 조회 */
    fun getTrashedItems(): Flow<List<Capture>>

    /** 보존 기간 초과 항목 조회 */
    suspend fun getTrashedOverdue(thresholdMs: Long): List<Capture>

    /** 필터링된 캡처 조회 (분류 유형 + 날짜 범위, 페이징) */
    suspend fun getFilteredCaptures(
        type: ClassifiedType?,
        startDate: Long?,
        endDate: Long?,
        limit: Int,
        offset: Int
    ): List<Capture>

    /** FTS 검색 + 필터 (분류 유형 + 날짜 범위) */
    suspend fun searchCapturesFiltered(
        query: String,
        type: ClassifiedType? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        limit: Int = 50
    ): List<Capture>
}
