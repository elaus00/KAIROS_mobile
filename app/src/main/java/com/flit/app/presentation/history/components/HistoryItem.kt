package com.flit.app.presentation.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flit.app.domain.model.Capture
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.NoteSubType
import com.flit.app.presentation.classification.ClassificationDropdown
import com.flit.app.ui.theme.FlitTheme

/**
 * 전체 기록 아이템
 * ai_title + 상대시간 + 프리뷰 + 분류칩 + 태그
 */
@Composable
fun HistoryItem(
    capture: Capture,
    onChangeType: (ClassifiedType, NoteSubType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1행: 제목 + 상대시간
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 제목
            Text(
                text = capture.aiTitle ?: capture.originalText.take(30),
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 상대 시간
            Text(
                text = formatRelativeTime(capture.createdAt),
                color = colors.textMuted,
                fontSize = 12.sp
            )
        }

        // 2행: 프리뷰 (ai_title이 있고 original_text와 다를 때만)
        if (capture.aiTitle != null && capture.aiTitle != capture.originalText) {
            Text(
                text = capture.originalText,
                color = colors.textSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 3행: 분류칩 + 분류 중 표시
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (capture.classifiedType == ClassifiedType.TEMP) {
                // TEMP 상태: 분류 중 인디케이터
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = colors.textMuted
                    )
                    Text(
                        text = "분류 중",
                        color = colors.textMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                // 분류 완료: 유형 변경 가능 드롭다운
                ClassificationDropdown(
                    currentType = capture.classifiedType,
                    currentSubType = capture.noteSubType,
                    onTypeSelected = onChangeType
                )
            }
        }
    }
}

/**
 * 상대 시간 포맷
 */
private fun formatRelativeTime(createdAt: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - createdAt
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "방금"
        minutes < 60 -> "${minutes}분 전"
        hours < 24 -> "${hours}시간 전"
        days < 2 -> "어제"
        days < 7 -> "${days}일 전"
        else -> {
            val calendar = java.util.Calendar.getInstance().apply { timeInMillis = createdAt }
            "${calendar.get(java.util.Calendar.MONTH) + 1}/${calendar.get(java.util.Calendar.DAY_OF_MONTH)}"
        }
    }
}
