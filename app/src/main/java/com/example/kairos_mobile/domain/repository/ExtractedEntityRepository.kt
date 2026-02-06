package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.ExtractedEntity

/**
 * 추출 엔티티 Repository 인터페이스
 */
interface ExtractedEntityRepository {

    /**
     * 특정 캡처의 추출 엔티티를 전체 교체한다.
     * 기존 엔티티를 삭제 후 새 엔티티 목록을 저장한다.
     */
    suspend fun replaceForCapture(captureId: String, entities: List<ExtractedEntity>)
}

