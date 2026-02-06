package com.example.kairos_mobile.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 사용자 설정 Room Entity (Key-Value)
 */
@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey
    val key: String,

    // 설정 값
    val value: String
)
