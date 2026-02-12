package com.example.kairos_mobile.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

data class SyncPushRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("changes") val changes: List<SyncPushItem>
)

data class SyncPushItem(
    @SerializedName("entity_type") val entityType: String,
    @SerializedName("operation") val operation: String,
    @SerializedName("client_id") val clientId: String,
    @SerializedName("data") val data: Map<String, Any?>,
    @SerializedName("client_updated_at") val clientUpdatedAt: String
)

data class SyncPushResponse(
    @SerializedName("acknowledged") val acknowledged: Int = 0,
    @SerializedName("server_timestamp") val serverTimestamp: String? = null
)

data class SyncPullRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("cursor") val cursor: String? = null
)

data class SyncPullResponse(
    @SerializedName("changes") val changes: List<SyncPullItem> = emptyList(),
    @SerializedName("next_cursor") val nextCursor: String? = null
)

data class SyncPullItem(
    @SerializedName("entity_type") val entityType: String,
    @SerializedName("operation") val operation: String,
    @SerializedName("server_id") val serverId: String? = null,
    @SerializedName("data") val data: Map<String, Any?> = emptyMap(),
    @SerializedName("server_updated_at") val serverUpdatedAt: String? = null
)
