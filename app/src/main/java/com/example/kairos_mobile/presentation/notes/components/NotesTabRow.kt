package com.example.kairos_mobile.presentation.notes.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.presentation.notes.NotesTab
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * NotesTabRow 컴포넌트 (PRD v4.0)
 * 노트/북마크 탭 선택
 *
 * @param selectedTab 선택된 탭
 * @param noteCount 노트 개수
 * @param bookmarkCount 북마크 개수
 * @param onTabSelected 탭 선택 콜백
 */
@Composable
fun NotesTabRow(
    selectedTab: NotesTab,
    noteCount: Int,
    bookmarkCount: Int,
    onTabSelected: (NotesTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.chipBg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        NotesTab.entries.forEach { tab ->
            val count = when (tab) {
                NotesTab.NOTES -> noteCount
                NotesTab.BOOKMARKS -> bookmarkCount
            }

            TabItem(
                tab = tab,
                count = count,
                isSelected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 개별 탭 아이템
 */
@Composable
private fun TabItem(
    tab: NotesTab,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) colors.card else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "bgColor"
    )

    val textColor = if (isSelected) colors.text else colors.textMuted

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = tab.getDisplayName(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )

            // 개수 표시
            Text(
                text = count.toString(),
                color = textColor.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
