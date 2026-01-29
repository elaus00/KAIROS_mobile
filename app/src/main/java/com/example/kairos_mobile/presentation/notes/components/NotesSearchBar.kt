package com.example.kairos_mobile.presentation.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * NotesSearchBar 컴포넌트 (PRD v4.0)
 * 노트/북마크 검색
 *
 * @param query 검색어
 * @param onQueryChange 검색어 변경 콜백
 * @param onClear 검색어 초기화 콜백
 */
@Composable
fun NotesSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.chipBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 검색 아이콘
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "검색",
            tint = colors.textMuted,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        // 검색 입력
        Box(
            modifier = Modifier.weight(1f)
        ) {
            // 플레이스홀더
            if (query.isEmpty()) {
                Text(
                    text = "노트, 북마크 검색...",
                    color = colors.placeholder,
                    fontSize = 15.sp
                )
            }

            // 입력 필드
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = colors.text,
                    fontSize = 15.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(colors.accent),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 초기화 버튼
        if (query.isNotEmpty()) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "지우기",
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
