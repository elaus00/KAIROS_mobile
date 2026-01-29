package com.example.kairos_mobile.presentation.notes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.presentation.components.common.SwipeableCard
import com.example.kairos_mobile.ui.components.kairosCard
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * NoteCard 컴포넌트 (PRD v4.0)
 * 노트 목록 아이템
 *
 * @param note 노트 데이터
 * @param onClick 클릭 콜백
 * @param onDelete 삭제 콜백
 */
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    // 날짜 포맷터
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("M월 d일")
    }

    SwipeableCard(
        onDismiss = onDelete,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .kairosCard()
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 상단: 제목 + 날짜
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // 제목
                    Text(
                        text = note.title,
                        color = colors.text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // 날짜
                    Text(
                        text = note.updatedAt
                            .atZone(ZoneId.systemDefault())
                            .format(dateFormatter),
                        color = colors.textMuted,
                        fontSize = 12.sp
                    )
                }

                // 내용 미리보기
                if (note.content.isNotBlank()) {
                    Text(
                        text = note.getPreview(120),
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }

                // 하단: 폴더 + 태그
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 폴더 chip
                    FolderBadge(folderName = note.folder.getDisplayName())

                    // 태그 (최대 2개)
                    note.tags.take(2).forEach { tag ->
                        TagBadge(tag = tag)
                    }

                    // 추가 태그 개수
                    if (note.tags.size > 2) {
                        Text(
                            text = "+${note.tags.size - 2}",
                            color = colors.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 폴더 뱃지
 */
@Composable
private fun FolderBadge(
    folderName: String,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Text(
        text = folderName,
        color = colors.textMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

/**
 * 태그 뱃지
 */
@Composable
private fun TagBadge(
    tag: String,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Text(
        text = "#$tag",
        color = colors.accent,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

/**
 * 노트 목록 빈 상태
 */
@Composable
fun NotesEmptyState(
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "노트가 없습니다",
                color = colors.textMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "캡처에서 노트를 추가해보세요",
                color = colors.textMuted.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}
