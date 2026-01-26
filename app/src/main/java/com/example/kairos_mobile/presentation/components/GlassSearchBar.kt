package com.example.kairos_mobile.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.components.glassCard
import com.example.kairos_mobile.ui.theme.*

/**
 * 글래스모피즘 검색 바
 */
@Composable
fun GlassSearchBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    placeholder: String = "캡처 검색…",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassCard()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 검색 아이콘
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "검색",
            tint = TextTertiary,
            modifier = Modifier.size(20.dp)
        )

        // 검색 입력 필드
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.3.sp
            ),
            cursorBrush = SolidColor(PrimaryNavy.copy(alpha = 0.8f)),
            modifier = Modifier.weight(1f),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                color = TextTertiary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.3.sp
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )

        // 지우기 버튼
        if (text.isNotEmpty()) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "지우기",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 검색 버튼
        Button(
            onClick = onSearch,
            enabled = text.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryNavy,
                contentColor = TextPrimary,
                disabledContainerColor = PrimaryNavy.copy(alpha = 0.45f),
                disabledContentColor = TextPrimary.copy(alpha = 0.45f)
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(
                text = "검색",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )
        }
    }
}
