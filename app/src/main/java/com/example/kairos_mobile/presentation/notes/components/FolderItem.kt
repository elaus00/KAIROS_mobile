package com.example.kairos_mobile.presentation.notes.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.FolderType
import com.example.kairos_mobile.presentation.notes.FolderWithCount
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * 폴더 아이템 행
 * 폴더명 + 노트 수 + 화살표
 * 사용자 폴더: 롱프레스 시 이름 변경/삭제 옵션
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderItem(
    folderWithCount: FolderWithCount,
    onClick: () -> Unit,
    onRename: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val folder = folderWithCount.folder
    val isUserFolder = folder.type == FolderType.USER
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .combinedClickable(
                onClick = onClick,
                onLongClick = if (isUserFolder) {
                    { showMenu = true }
                } else null
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 폴더 아이콘
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = colors.icon,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 폴더명
        Text(
            text = folder.name,
            color = colors.text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // 노트 수
        Text(
            text = "${folderWithCount.noteCount}",
            color = colors.textMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 화살표
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(20.dp)
        )

        // 사용자 폴더 컨텍스트 메뉴
        if (isUserFolder) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("이름 변경") },
                    onClick = {
                        showMenu = false
                        onRename?.invoke()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            "삭제",
                            color = colors.danger
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDelete?.invoke()
                    }
                )
            }
        }
    }
}
