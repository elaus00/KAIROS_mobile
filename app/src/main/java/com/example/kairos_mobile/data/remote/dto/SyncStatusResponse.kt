package com.example.kairos_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 외부 서비스 동기화 상태 응답 DTO
 * Google Calendar, Todoist 연동 상태 조회 결과
 */
data class SyncStatusResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("isConnected")
    val isConnected: Boolean,

    @SerializedName("lastSyncTime")
    val lastSyncTime: Long?,

    @SerializedName("syncedCount")
    val syncedCount: Int,

    @SerializedName("error")
    val error: String?
)
