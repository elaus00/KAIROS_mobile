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
private val filterItems = listOf(
    "전체" to null,
    "일정" to ClassifiedType.SCHEDULE,
    "할 일" to ClassifiedType.TODO,
    "노트" to ClassifiedType.NOTES
)

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
        filterItems.forEach { (label, type) ->
            FlitChip(
                text = label,
                selected = selectedType == type,
                onClick = { onTypeSelected(type) }
            )
        }
    }
}
