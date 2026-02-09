package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.remote.ApiResponseHandler
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.v2.InboxClassifyRequest
import com.example.kairos_mobile.data.remote.dto.v2.NoteGroupRequest
import com.example.kairos_mobile.data.remote.dto.v2.NoteReorganizeRequest
import com.example.kairos_mobile.data.remote.dto.v2.SemanticSearchRequest
import com.example.kairos_mobile.domain.model.AnalyticsDashboard
import com.example.kairos_mobile.domain.model.NoteGroup
import com.example.kairos_mobile.domain.model.SemanticSearchResult
import com.example.kairos_mobile.domain.repository.NoteAiRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 노트 기능 Repository 구현체
 * 그룹화, Inbox 분류, 재정리, 시맨틱 검색
 */
@Singleton
class NoteAiRepositoryImpl @Inject constructor(
    private val api: KairosApi
) : NoteAiRepository {

    override suspend fun groupNotes(noteIds: List<String>): List<NoteGroup> {
        val response = ApiResponseHandler.safeCall {
            api.groupNotes(NoteGroupRequest(noteIds))
        }
        return response.groups.map {
            NoteGroup(groupName = it.groupName, noteIds = it.noteIds)
        }
    }

    override suspend fun inboxClassify(captureIds: List<String>): List<Triple<String, String, Double>> {
        val response = ApiResponseHandler.safeCall {
            api.inboxClassify(InboxClassifyRequest(captureIds))
        }
        return response.classifications.map {
            Triple(it.captureId, it.suggestedType, it.confidence)
        }
    }

    override suspend fun reorganize(folderId: String?): Pair<List<Pair<String, String>>, List<Pair<String, String>>> {
        val response = ApiResponseHandler.safeCall {
            api.reorganizeNotes(NoteReorganizeRequest(folderId))
        }
        val before = response.before.map { it.noteId to it.folderName }
        val after = response.after.map { it.noteId to it.folderName }
        return before to after
    }

    override suspend fun semanticSearch(query: String, limit: Int): List<SemanticSearchResult> {
        val response = ApiResponseHandler.safeCall {
            api.semanticSearch(SemanticSearchRequest(query, limit))
        }
        return response.results.map {
            SemanticSearchResult(
                captureId = it.captureId,
                score = it.score,
                snippet = it.snippet
            )
        }
    }

    override suspend fun getDashboard(): AnalyticsDashboard {
        val data = ApiResponseHandler.safeCall { api.getAnalyticsDashboard() }
        return AnalyticsDashboard(
            totalCaptures = data.totalCaptures,
            capturesByType = data.capturesByType,
            capturesByDay = data.capturesByDay,
            avgClassificationTimeMs = data.avgClassificationTimeMs,
            topTags = data.topTags.map { it.tag to it.count }
        )
    }
}
