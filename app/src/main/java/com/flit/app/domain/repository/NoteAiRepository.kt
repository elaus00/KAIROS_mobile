package com.flit.app.domain.repository

import com.flit.app.domain.model.AnalyticsDashboard
import com.flit.app.domain.model.Folder
import com.flit.app.domain.model.InboxClassifyResult
import com.flit.app.domain.model.NoteAiInput
import com.flit.app.domain.model.NoteGroup
import com.flit.app.domain.model.ProposedStructure
import com.flit.app.domain.model.SemanticSearchResult

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
