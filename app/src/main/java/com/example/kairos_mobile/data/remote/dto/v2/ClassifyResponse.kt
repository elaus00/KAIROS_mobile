package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * AI 분류 응답 DTO
 */
data class ClassifyResponse(
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

/**
 * 추출된 엔티티 DTO
 */
data class EntityDto(
    @SerializedName("type")
    val type: String,

    @SerializedName("value")
    val value: String,

    @SerializedName("normalized_value")
    val normalizedValue: String? = null
)

/**
 * 일정 정보 DTO
 */
data class ScheduleInfoDto(
    @SerializedName("start_time")
    val startTime: Long,

    @SerializedName("end_time")
    val endTime: Long? = null,

    @SerializedName("location")
    val location: String? = null,

    @SerializedName("is_all_day")
    val isAllDay: Boolean = false
)

/**
 * 할 일 정보 DTO
 */
data class TodoInfoDto(
    @SerializedName("deadline")
    val deadline: Long? = null,

    @SerializedName("deadline_source")
    val deadlineSource: String? = null
)
