package com.flit.app.domain.model

/** 사용자 정보 */
data class User(
    val id: String,
    val email: String,
    val subscriptionTier: String,
    val googleCalendarConnected: Boolean = false
)
