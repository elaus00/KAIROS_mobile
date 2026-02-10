package com.example.kairos_mobile.data.repository

import com.example.kairos_mobile.data.remote.ApiResponseHandler
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.v2.FolderItemDto
import com.example.kairos_mobile.data.remote.dto.v2.InboxClassifyRequest
import com.example.kairos_mobile.data.remote.dto.v2.NoteAiItemDto
import com.example.kairos_mobile.data.remote.dto.v2.NoteGroupRequest
import com.example.kairos_mobile.data.remote.dto.v2.NoteReorganizeRequest
import com.example.kairos_mobile.data.remote.dto.v2.SemanticSearchRequest
import com.example.kairos_mobile.domain.model.AnalyticsDashboard
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.InboxAssignment
import com.example.kairos_mobile.domain.model.InboxClassifyResult
import com.example.kairos_mobile.domain.model.NewFolderSuggestion
import com.example.kairos_mobile.domain.model.NoteAiInput
import com.example.kairos_mobile.domain.model.NoteGroup
import com.example.kairos_mobile.domain.model.ProposedStructure
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

    override suspend fun groupNotes(notes: List<NoteAiInput>, folders: List<Folder>): List<NoteGroup> {
        val response = ApiResponseHandler.safeCall {
            api.groupNotes(NoteGroupRequest(
                notes = notes.map { it.toDto() },
                existingFolders = folders.map { it.toDto() }
            ))
        }
        return response.groups.map {
            NoteGroup(folderName = it.folderName, folderType = it.folderType, captureIds = it.captureIds)
        }
    }

    override suspend fun inboxClassify(notes: List<NoteAiInput>, folders: List<Folder>): InboxClassifyResult {
        val response = ApiResponseHandler.safeCall {
            api.inboxClassify(InboxClassifyRequest(
                notes = notes.map { it.toDto() },
                existingFolders = folders.map { it.toDto() }
            ))
        }
        return InboxClassifyResult(
            assignments = response.assignments.map {
                InboxAssignment(
                    captureId = it.captureId,
                    targetFolderId = it.targetFolderId,
                    targetFolderName = it.targetFolderName,
                    newNoteSubType = it.newNoteSubType
                )
            },
            newFolders = response.newFolders.map {
                NewFolderSuggestion(name = it.name, type = it.type)
            }
        )
    }

    override suspend fun reorganize(notes: List<NoteAiInput>, folders: List<Folder>): List<ProposedStructure> {
        val response = ApiResponseHandler.safeCall {
            api.reorganizeNotes(NoteReorganizeRequest(
                notes = notes.map { it.toDto() },
                existingFolders = folders.map { it.toDto() }
            ))
        }
        return response.proposedStructure.map {
            ProposedStructure(
                folderName = it.folderName,
                folderType = it.folderType,
                action = it.action,
                captureIds = it.captureIds
            )
        }
    }

    override suspend fun semanticSearch(query: String, limit: Int): List<SemanticSearchResult> {
        val response = ApiResponseHandler.safeCall {
            api.semanticSearch(SemanticSearchRequest(query, limit))
        }
        return response.results.map {
            SemanticSearchResult(captureId = it.captureId, score = it.score, snippet = it.snippet)
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

    /** NoteAiInput → DTO 변환 */
    private fun NoteAiInput.toDto() = NoteAiItemDto(
        captureId = captureId,
        aiTitle = aiTitle,
        tags = tags,
        noteSubType = noteSubType,
        folderId = folderId
    )

    /** Folder → DTO 변환 */
    private fun Folder.toDto() = FolderItemDto(
        folderId = id,
        name = name,
        type = type.name
    )
}
