package com.example.kairos_mobile.domain.repository

import com.example.kairos_mobile.domain.model.AnalyticsDashboard
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.InboxClassifyResult
import com.example.kairos_mobile.domain.model.NoteAiInput
import com.example.kairos_mobile.domain.model.NoteGroup
import com.example.kairos_mobile.domain.model.ProposedStructure
import com.example.kairos_mobile.domain.model.SemanticSearchResult

/** AI 노트 기능 Repository 인터페이스 */
interface NoteAiRepository {
    /** 노트 자동 그룹화 */
    suspend fun groupNotes(notes: List<NoteAiInput>, folders: List<Folder>): List<NoteGroup>

    /** 인박스 자동 분류 */
    suspend fun inboxClassify(notes: List<NoteAiInput>, folders: List<Folder>): InboxClassifyResult

    /** 노트 재구성 제안 */
    suspend fun reorganize(notes: List<NoteAiInput>, folders: List<Folder>): List<ProposedStructure>

    /** 시맨틱 검색 */
    suspend fun semanticSearch(query: String, limit: Int = 20): List<SemanticSearchResult>

    /** 분석 대시보드 조회 */
    suspend fun getDashboard(): AnalyticsDashboard
}
