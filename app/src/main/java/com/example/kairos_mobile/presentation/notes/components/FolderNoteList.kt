package com.example.kairos_mobile.presentation.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.presentation.notes.NoteWithCapture
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 폴더 내 노트 목록 화면
 * 뒤로가기 + 폴더명 + 노트 리스트
 */
@Composable
fun FolderNoteList(
    folderName: String,
    notes: List<NoteWithCapture>,
    onBackClick: () -> Unit,
    onNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Column(modifier = modifier.fillMaxSize()) {
        // 헤더: 뒤로가기 + 폴더명
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = colors.text
                )
            }
            Text(
                text = folderName,
                color = colors.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 노트 목록
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "노트가 없습니다",
                    color = colors.textMuted,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("notes_item_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = notes,
                    key = { it.noteId }
                ) { note ->
                    NoteItem(
                        note = note,
                        onClick = { onNoteClick(note.captureId) }
                    )
                }
            }
        }
    }
}

/**
 * 개별 노트 아이템
 */
@Composable
private fun NoteItem(
    note: NoteWithCapture,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val title = note.aiTitle ?: note.originalText.take(30)
    val preview = if (note.aiTitle != null && note.aiTitle != note.originalText) {
        note.originalText
    } else null
    val dateFormat = SimpleDateFormat("M/d", Locale.getDefault())
    val dateText = dateFormat.format(Date(note.createdAt))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // 제목
            Text(
                text = title,
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 미리보기 (ai_title != original_text일 때만)
            if (preview != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = preview,
                    color = colors.textMuted,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 날짜
        Text(
            text = dateText,
            color = colors.textMuted,
            fontSize = 12.sp
        )
    }
}
