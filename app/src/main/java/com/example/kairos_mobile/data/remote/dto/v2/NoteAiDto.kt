package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

// ── 공통 요청 DTO ──

/** AI 분석 대상 노트 아이템 */
data class NoteAiItemDto(
    @SerializedName("capture_id") val captureId: String,
    @SerializedName("ai_title") val aiTitle: String,
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("note_sub_type") val noteSubType: String? = null,
    @SerializedName("folder_id") val folderId: String? = null
)

/** 기존 폴더 정보 */
data class FolderItemDto(
    @SerializedName("folder_id") val folderId: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String
)

// ── Group ──

/** AI 노트 그룹화 요청 */
data class NoteGroupRequest(
    @SerializedName("notes") val notes: List<NoteAiItemDto>,
    @SerializedName("existing_folders") val existingFolders: List<FolderItemDto>
)

/** AI 노트 그룹화 응답 */
data class NoteGroupResponse(
    @SerializedName("groups") val groups: List<NoteGroupDto>
)

data class NoteGroupDto(
    @SerializedName("folder_name") val folderName: String,
    @SerializedName("folder_type") val folderType: String,
    @SerializedName("capture_ids") val captureIds: List<String>
)

// ── Inbox Classify ──

/** Inbox 자동 분류 요청 */
data class InboxClassifyRequest(
    @SerializedName("notes") val notes: List<NoteAiItemDto>,
    @SerializedName("existing_folders") val existingFolders: List<FolderItemDto>
)

/** Inbox 자동 분류 응답 */
data class InboxClassifyResponse(
    @SerializedName("assignments") val assignments: List<InboxAssignmentDto>,
    @SerializedName("new_folders") val newFolders: List<NewFolderDto> = emptyList()
)

data class InboxAssignmentDto(
    @SerializedName("capture_id") val captureId: String,
    @SerializedName("target_folder_id") val targetFolderId: String,
    @SerializedName("target_folder_name") val targetFolderName: String,
    @SerializedName("new_note_sub_type") val newNoteSubType: String
)

data class NewFolderDto(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String
)

// ── Reorganize ──

/** 노트 재구성 요청 */
data class NoteReorganizeRequest(
    @SerializedName("notes") val notes: List<NoteAiItemDto>,
    @SerializedName("existing_folders") val existingFolders: List<FolderItemDto>
)

/** 노트 재구성 응답 */
data class NoteReorganizeResponse(
    @SerializedName("proposed_structure") val proposedStructure: List<ProposedStructureDto>
)

data class ProposedStructureDto(
    @SerializedName("folder_name") val folderName: String,
    @SerializedName("folder_type") val folderType: String,
    @SerializedName("action") val action: String? = null,
    @SerializedName("capture_ids") val captureIds: List<String>
)

// ── Semantic Search ──

/** 시맨틱 검색 요청 */
data class SemanticSearchRequest(
    @SerializedName("query") val query: String,
    @SerializedName("limit") val limit: Int = 20
)

/** 시맨틱 검색 응답 */
data class SemanticSearchResponse(
    @SerializedName("results") val results: List<SemanticSearchResultDto>
)

data class SemanticSearchResultDto(
    @SerializedName("capture_id") val captureId: String,
    @SerializedName("score") val score: Double,
    @SerializedName("snippet") val snippet: String
)

// ── Analytics Dashboard ──

/** 분석 대시보드 응답 */
data class AnalyticsDashboardResponse(
    @SerializedName("total_captures") val totalCaptures: Int,
    @SerializedName("captures_by_type") val capturesByType: Map<String, Int>,
    @SerializedName("captures_by_day") val capturesByDay: Map<String, Int>,
    @SerializedName("avg_classification_time_ms") val avgClassificationTimeMs: Long,
    @SerializedName("top_tags") val topTags: List<TagCountDto>
)

data class TagCountDto(
    @SerializedName("tag") val tag: String,
    @SerializedName("count") val count: Int
)

// ── OCR ──

/** OCR 추출 요청 */
data class OcrRequest(
    @SerializedName("image_data") val imageData: String,
    @SerializedName("image_type") val imageType: String = "jpeg",
    @SerializedName("language_hint") val languageHint: String? = null,
    @SerializedName("extract_structure") val extractStructure: Boolean = false
)

/** OCR 추출 응답 */
data class OcrResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("text") val text: String,
    @SerializedName("confidence") val confidence: Double = 0.0,
    @SerializedName("language") val language: String? = null,
    @SerializedName("word_count") val wordCount: Int = 0,
    @SerializedName("has_structure") val hasStructure: Boolean = false,
    @SerializedName("structured_content") val structuredContent: Map<String, Any>? = null
)
