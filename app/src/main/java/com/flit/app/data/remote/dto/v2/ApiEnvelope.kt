package com.flit.app.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

data class ApiEnvelope<T>(
    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("error")
    val error: ApiErrorResponse? = null
)

data class ApiErrorResponse(
    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String
)
