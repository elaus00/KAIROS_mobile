package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/** AI 노트 그룹화 요청 */
data class NoteGroupRequest(
    @SerializedName("note_ids") val noteIds: List<String>
)

/** AI 노트 그룹화 응답 */
data class NoteGroupResponse(
    @SerializedName("groups") val groups: List<NoteGroupDto>
)

data class NoteGroupDto(
    @SerializedName("group_name") val groupName: String,
    @SerializedName("note_ids") val noteIds: List<String>
)

/** Inbox 자동 분류 요청 */
data class InboxClassifyRequest(
    @SerializedName("capture_ids") val captureIds: List<String>
)

/** Inbox 자동 분류 응답 */
data class InboxClassifyResponse(
    @SerializedName("classifications") val classifications: List<InboxClassificationDto>
)

data class InboxClassificationDto(
    @SerializedName("capture_id") val captureId: String,
    @SerializedName("suggested_type") val suggestedType: String,
    @SerializedName("confidence") val confidence: Double
)

/** 노트 재구성 요청 */
data class NoteReorganizeRequest(
    @SerializedName("folder_id") val folderId: String? = null
)

/** 노트 재구성 응답 */
data class NoteReorganizeResponse(
    @SerializedName("before") val before: List<NoteReorganizeItemDto>,
    @SerializedName("after") val after: List<NoteReorganizeItemDto>
)

data class NoteReorganizeItemDto(
    @SerializedName("note_id") val noteId: String,
    @SerializedName("folder_name") val folderName: String
)

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

/** OCR 응답 */
data class OcrResponse(
    @SerializedName("extracted_text") val extractedText: String
)
