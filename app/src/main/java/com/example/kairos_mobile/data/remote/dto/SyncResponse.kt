package com.example.kairos_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 동기화 트리거 응답 DTO
 * 수동 동기화 요청 결과
 */
data class SyncResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("syncedCount")
    val syncedCount: Int,

    @SerializedName("message")
    val message: String?,

    @SerializedName("error")
    val error: String?
)
