package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.AnalyticsDashboard
import com.example.kairos_mobile.domain.model.NoteGroup
import com.example.kairos_mobile.domain.model.SemanticSearchResult

/** AI 노트 기능 Repository 인터페이스 */
interface NoteAiRepository {
    /** 노트 자동 그룹화 */
    suspend fun groupNotes(noteIds: List<String>): List<NoteGroup>

    /** 인박스 자동 분류 (captureId, type, confidence) */
    suspend fun inboxClassify(captureIds: List<String>): List<Triple<String, String, Double>>

    /** 노트 재정리 (이동 목록, 새 폴더 목록) */
    suspend fun reorganize(folderId: String? = null): Pair<List<Pair<String, String>>, List<Pair<String, String>>>

    /** 시맨틱 검색 */
    suspend fun semanticSearch(query: String, limit: Int = 20): List<SemanticSearchResult>

    /** 분석 대시보드 조회 */
    suspend fun getDashboard(): AnalyticsDashboard
}
