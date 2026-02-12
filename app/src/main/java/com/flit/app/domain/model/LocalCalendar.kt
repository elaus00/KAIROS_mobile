package com.flit.app.domain.model

data class LocalCalendar(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val color: Int,
    val isPrimary: Boolean
)
