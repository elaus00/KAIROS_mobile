package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.AnalyticsEvent

/**
 * 분석 이벤트 Repository 인터페이스
 */
interface AnalyticsRepository {

    /** 이벤트 삽입 */
    suspend fun insert(event: AnalyticsEvent)

    /** 동기화되지 않은 이벤트 조회 */
    suspend fun getUnsynced(limit: Int = 50): List<AnalyticsEvent>

    /** 동기화 완료 표시 */
    suspend fun markSynced(ids: List<String>)

    /** 오래된 동기화 완료 이벤트 삭제 */
    suspend fun deleteOld(threshold: Long)
}
