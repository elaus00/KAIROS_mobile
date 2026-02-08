package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.ClassificationLog

/**
 * 분류 수정 로그 Repository 인터페이스
 */
interface ClassificationLogRepository {

    /** 로그 삽입 */
    suspend fun insert(log: ClassificationLog)

    /** 특정 캡처의 분류 로그 조회 */
    suspend fun getByCaptureId(captureId: String): List<ClassificationLog>
}
