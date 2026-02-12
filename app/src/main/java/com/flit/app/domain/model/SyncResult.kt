package com.flit.app.domain.model

data class SyncResult(
    val success: Boolean,
    val pushedCount: Int = 0,
    val pulledCount: Int = 0,
    val skipped: Boolean = false,
    val accountSwitchRequired: Boolean = false,
    val message: String? = null
)
