package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.Tag

/**
 * 태그 Repository 인터페이스
 */
interface TagRepository {

    /** 태그 생성 (이미 존재하면 기존 반환) */
    suspend fun getOrCreate(name: String): Tag

    /** 캡처에 태그 연결 */
    suspend fun linkTagToCapture(captureId: String, tagId: String)

    /** 캡처의 태그 전체 삭제 */
    suspend fun deleteTagsByCaptureId(captureId: String)

    /** 캡처에 연결된 태그 이름 목록 조회 */
    suspend fun getTagsForCapture(captureId: String): List<String>
}
