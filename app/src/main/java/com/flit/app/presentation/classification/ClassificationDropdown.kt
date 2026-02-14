package com.flit.app.presentation.classification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.NoteSubType
import com.flit.app.ui.theme.FlitTheme

/**
 * 분류 유형 선택 드롭다운
 * 4옵션: 일정 / 할 일 / 노트 / 아이디어
 */
@Composable
fun ClassificationDropdown(
    currentType: ClassifiedType,
    currentSubType: NoteSubType?,
    onTypeSelected: (ClassifiedType, NoteSubType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // 현재 분류 칩
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 36.dp)  // 36dp 높이
                .clip(RoundedCornerShape(8.dp))
                .background(colors.chipBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),  // FlitChip과 일관성
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = getDisplayName(currentType, currentSubType),
                color = colors.chipText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.chipText,
                modifier = Modifier.size(14.dp)
            )
        }

        // 드롭다운 메뉴
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.card)
        ) {
            classificationOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.displayName,
                            color = colors.text,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onTypeSelected(option.type, option.subType)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 분류 옵션 정의
 */
private data class ClassificationOption(
    val type: ClassifiedType,
    val subType: NoteSubType?,
    val displayName: String
)

/**
 * 4가지 분류 옵션
 */
private val classificationOptions = listOf(
    ClassificationOption(ClassifiedType.SCHEDULE, null, "일정"),
    ClassificationOption(ClassifiedType.TODO, null, "할 일"),
    ClassificationOption(ClassifiedType.NOTES, NoteSubType.INBOX, "노트"),
    ClassificationOption(ClassifiedType.NOTES, NoteSubType.IDEA, "아이디어")
)

/**
 * 분류 유형의 표시 이름 반환
 */
fun getDisplayName(type: ClassifiedType, subType: NoteSubType?): String {
    return when (type) {
        ClassifiedType.SCHEDULE -> "일정"
        ClassifiedType.TODO -> "할 일"
        ClassifiedType.NOTES -> when (subType) {
            NoteSubType.IDEA -> "아이디어"
            NoteSubType.BOOKMARK -> "북마크"
            else -> "노트"
        }
        ClassifiedType.TEMP -> "미분류"
    }
}
