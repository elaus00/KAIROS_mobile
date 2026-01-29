package com.example.kairos_mobile.presentation.notes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Bookmark
import com.example.kairos_mobile.presentation.components.common.SwipeableCard
import com.example.kairos_mobile.ui.components.kairosCard
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * BookmarkCard 컴포넌트 (PRD v4.0)
 * 북마크 목록 아이템
 *
 * @param bookmark 북마크 데이터
 * @param onClick 클릭 콜백
 * @param onDelete 삭제 콜백
 */
@Composable
fun BookmarkCard(
    bookmark: Bookmark,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 링크 아이콘
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )

                // 내용
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 제목
                    Text(
                        text = bookmark.title,
                        color = colors.text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 도메인 + 날짜
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bookmark.getDomain(),
                            color = colors.accent,
                            fontSize = 12.sp
                        )

                        Text(
                            text = "·",
                            color = colors.textMuted,
                            fontSize = 12.sp
                        )

                        Text(
                            text = bookmark.createdAt
                                .atZone(ZoneId.systemDefault())
                                .format(dateFormatter),
                            color = colors.textMuted,
                            fontSize = 12.sp
                        )
                    }

                    // 요약 (있는 경우)
                    bookmark.getSummaryPreview(100)?.let { summary ->
                        Text(
                            text = summary,
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }

                    // 태그
                    if (bookmark.tags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            bookmark.tags.take(3).forEach { tag ->
                                Text(
                                    text = "#$tag",
                                    color = colors.textMuted,
                                    fontSize = 11.sp
                                )
                            }

                            if (bookmark.tags.size > 3) {
                                Text(
                                    text = "+${bookmark.tags.size - 3}",
                                    color = colors.textMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 북마크 목록 빈 상태
 */
@Composable
fun BookmarksEmptyState(
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
                text = "북마크가 없습니다",
                color = colors.textMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "링크를 캡처하면 북마크로 저장됩니다",
                color = colors.textMuted.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}
