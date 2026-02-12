package com.flit.app.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * AI 분류 요청 DTO
 */
data class ClassifyRequest(
    @SerializedName("text")
    val text: String,

    @SerializedName("source")
    val source: String,

    @SerializedName("device_id")
    val deviceId: String,

    @SerializedName("user_context")
    val userContext: UserContextDto? = null
)

data class UserContextDto(
    @SerializedName("modification_history")
    val modificationHistory: Map<String, Any?>? = null,

    @SerializedName("preset_id")
    val presetId: String? = null,

    @SerializedName("custom_instruction")
    val customInstruction: String? = null
)

data class ClassifyBatchRequest(
    @SerializedName("items")
    val items: List<ClassifyBatchItemDto>,

    @SerializedName("device_id")
    val deviceId: String
)

data class ClassifyBatchItemDto(
    @SerializedName("capture_id")
    val captureId: String,

    @SerializedName("text")
    val text: String
)
