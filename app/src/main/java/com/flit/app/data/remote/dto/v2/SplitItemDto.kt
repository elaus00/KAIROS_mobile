package com.flit.app.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * 다중 의도 분리 항목 DTO
 * 서버에서 split_items로 반환되는 각 분리 항목.
 */
data class SplitItemDto(
    @SerializedName("split_text")
    val splitText: String,

    @SerializedName("classified_type")
    val classifiedType: String,

    @SerializedName("note_sub_type")
    val noteSubType: String? = null,

    @SerializedName("confidence")
    val confidence: String,

    @SerializedName("ai_title")
    val aiTitle: String,

    @SerializedName("tags")
    val tags: List<String> = emptyList(),

    @SerializedName("entities")
    val entities: List<EntityDto> = emptyList(),

    @SerializedName("schedule_info")
    val scheduleInfo: ScheduleInfoDto? = null,

    @SerializedName("todo_info")
    val todoInfo: TodoInfoDto? = null
)
