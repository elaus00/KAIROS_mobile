package com.flit.app.data.remote.dto.v2

import com.google.gson.annotations.SerializedName

/**
 * FCM 토큰 등록 요청
 */
data class FcmTokenRequest(
    @SerializedName("fcm_token")
    val fcmToken: String
)
