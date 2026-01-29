package com.example.kairos_mobile.presentation.notes.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.kairos_mobile.domain.model.NoteFolder
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * FolderChips 컴포넌트 (PRD v4.0)
 * 전체/인박스/아이디어/레퍼런스 폴더 선택
 *
 * @param selectedFolder 선택된 폴더 (null = 전체)
 * @param noteCountByFolder 폴더별 노트 개수
 * @param totalCount 전체 노트 개수
 * @param onFolderSelected 폴더 선택 콜백
 */
@Composable
fun FolderChips(
    selectedFolder: NoteFolder?,
    noteCountByFolder: Map<NoteFolder, Int>,
    totalCount: Int,
    onFolderSelected: (NoteFolder?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 전체 chip
        FolderChip(
            label = "전체",
            count = totalCount,
            isSelected = selectedFolder == null,
            onClick = { onFolderSelected(null) }
        )

        // 폴더별 chip
        NoteFolder.entries.forEach { folder ->
            FolderChip(
                label = folder.getDisplayName(),
                count = noteCountByFolder[folder] ?: 0,
                isSelected = selectedFolder == folder,
                onClick = { onFolderSelected(folder) }
            )
        }
    }
}

/**
 * 개별 폴더 Chip
 */
@Composable
private fun FolderChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) colors.accent else colors.chipBg,
        animationSpec = tween(durationMillis = 200),
        label = "bgColor"
    )

    val textColor = if (isSelected) {
        if (colors.isDark) colors.background else Color.White
    } else {
        colors.chipText
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )

        // 개수 표시
        Text(
            text = count.toString(),
            color = textColor.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
