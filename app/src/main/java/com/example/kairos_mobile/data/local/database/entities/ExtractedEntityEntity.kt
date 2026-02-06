package com.example.kairos_mobile.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 추출 엔티티 Room Entity
 * AI가 캡처 텍스트에서 추출한 핵심 개체 (NER).
 */
@Entity(
    tableName = "entities",
    foreignKeys = [
        ForeignKey(
            entity = CaptureEntity::class,
            parentColumns = ["id"],
            childColumns = ["capture_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["capture_id"]),
        Index(value = ["type"])
    ]
)
data class ExtractedEntityEntity(
    @PrimaryKey
    val id: String,

    // FK → captures.id
    @ColumnInfo(name = "capture_id")
    val captureId: String,

    // 엔티티 유형: PERSON, PLACE, DATE, TIME, AMOUNT, OTHER
    val type: String,

    // 원문 표현 ("금요일", "강남역")
    val value: String,

    // 정규화된 값 ("2026-02-13", "강남역")
    @ColumnInfo(name = "normalized_value")
    val normalizedValue: String? = null
)
