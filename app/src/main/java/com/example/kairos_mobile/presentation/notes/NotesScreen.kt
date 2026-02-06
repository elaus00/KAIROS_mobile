package com.example.kairos_mobile.presentation.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.presentation.notes.components.FolderItem
import com.example.kairos_mobile.presentation.notes.components.FolderNoteList
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * 노트 탭 메인 화면 (Scaffold 포함)
 * 폴더 리스트 → 폴더 내 노트 목록 네비게이션
 */
@Composable
fun NotesScreen(
    onNavigate: (String) -> Unit,
    onSearchClick: () -> Unit = {},
    onNoteClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NotesContentInternal(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onSearchClick = onSearchClick,
        onNoteClick = onNoteClick,
        modifier = modifier
    )
}

/**
 * Notes 화면 내용 (Scaffold 없이)
 * MainScreen의 HorizontalPager에서 사용
 */
@Composable
fun NotesContent(
    onSearchClick: () -> Unit = {},
    onNoteClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NotesContentInternal(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onSearchClick = onSearchClick,
        onNoteClick = onNoteClick,
        modifier = modifier
    )
}

/**
 * 노트 화면 내부 구현
 */
@Composable
private fun NotesContentInternal(
    uiState: NotesUiState,
    onEvent: (NotesEvent) -> Unit,
    onSearchClick: () -> Unit,
    onNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    // 다이얼로그
    if (uiState.showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { onEvent(NotesEvent.DismissCreateFolderDialog) },
            onCreate = { onEvent(NotesEvent.CreateFolder(it)) }
        )
    }

    uiState.renamingFolder?.let { folder ->
        RenameFolderDialog(
            currentName = folder.name,
            onDismiss = { onEvent(NotesEvent.DismissRenameFolderDialog) },
            onRename = { onEvent(NotesEvent.RenameFolder(folder.id, it)) }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        if (uiState.selectedFolder != null) {
            // 폴더 내 노트 목록
            FolderNoteList(
                folderName = uiState.selectedFolder.name,
                notes = uiState.notes,
                onBackClick = { onEvent(NotesEvent.BackToFolders) },
                onNoteClick = onNoteClick
            )
        } else {
            // 폴더 리스트 화면
            FolderListView(
                uiState = uiState,
                onEvent = onEvent,
                onSearchClick = onSearchClick
            )
        }
    }
}

/**
 * 폴더 리스트 화면
 */
@Composable
private fun ColumnScope.FolderListView(
    uiState: NotesUiState,
    onEvent: (NotesEvent) -> Unit,
    onSearchClick: () -> Unit
) {
    val colors = KairosTheme.colors

    // 상단 헤더: 제목 + 검색 아이콘
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Notes",
            color = colors.text,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )

        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색",
                tint = colors.icon
            )
        }
    }

    // 로딩 상태
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = colors.accent,
                modifier = Modifier.size(32.dp)
            )
        }
    } else if (uiState.folders.isEmpty()) {
        // 빈 상태
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "폴더가 없습니다",
                color = colors.textMuted,
                fontSize = 15.sp
            )
        }
    } else {
        // 폴더 리스트
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = uiState.folders,
                key = { it.folder.id }
            ) { folderWithCount ->
                FolderItem(
                    folderWithCount = folderWithCount,
                    onClick = { onEvent(NotesEvent.SelectFolder(folderWithCount.folder)) },
                    onRename = {
                        onEvent(NotesEvent.ShowRenameFolderDialog(folderWithCount.folder))
                    },
                    onDelete = {
                        onEvent(NotesEvent.DeleteFolder(folderWithCount.folder.id))
                    }
                )
            }
        }
    }

    // 새 폴더 버튼
    TextButton(
        onClick = { onEvent(NotesEvent.ShowCreateFolderDialog) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "새 폴더",
            color = colors.accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
