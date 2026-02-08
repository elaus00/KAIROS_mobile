package com.example.kairos_mobile.presentation.components.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kairos_mobile.domain.model.ClassifiedType

/**
 * 분류 유형 필터 칩 Row
 * 전체/일정/할일/노트 칩 4개 표시, 선택 상태 관리
 * 2b-5 SearchScreen에서도 재사용 가능
 */
@Composable
fun FilterChipRow(
    selectedType: ClassifiedType?,
    onTypeSelected: (ClassifiedType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KairosChip(
            text = "전체",
            selected = selectedType == null,
            onClick = { onTypeSelected(null) }
        )
        KairosChip(
            text = "일정",
            selected = selectedType == ClassifiedType.SCHEDULE,
            onClick = { onTypeSelected(ClassifiedType.SCHEDULE) }
        )
        KairosChip(
            text = "할 일",
            selected = selectedType == ClassifiedType.TODO,
            onClick = { onTypeSelected(ClassifiedType.TODO) }
        )
        KairosChip(
            text = "노트",
            selected = selectedType == ClassifiedType.NOTES,
            onClick = { onTypeSelected(ClassifiedType.NOTES) }
        )
    }
}
