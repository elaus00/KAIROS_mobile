package com.flit.app.presentation.components.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flit.app.domain.model.ClassifiedType

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
        FlitChip(
            text = "전체",
            selected = selectedType == null,
            compact = true,
            onClick = { onTypeSelected(null) }
        )
        FlitChip(
            text = "일정",
            selected = selectedType == ClassifiedType.SCHEDULE,
            compact = true,
            onClick = { onTypeSelected(ClassifiedType.SCHEDULE) }
        )
        FlitChip(
            text = "할 일",
            selected = selectedType == ClassifiedType.TODO,
            compact = true,
            onClick = { onTypeSelected(ClassifiedType.TODO) }
        )
        FlitChip(
            text = "노트",
            selected = selectedType == ClassifiedType.NOTES,
            compact = true,
            onClick = { onTypeSelected(ClassifiedType.NOTES) }
        )
    }
}
