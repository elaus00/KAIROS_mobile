package com.example.kairos_mobile.presentation.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.domain.model.FolderType
import com.example.kairos_mobile.presentation.components.common.KairosChip
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Notes 화면 내용 (Scaffold 없이)
 * MainScreen의 HorizontalPager에서 사용
 */
@Composable
fun NotesContent(
    onSearchClick: () -> Unit = {},
    onNoteClick: (String) -> Unit = {},
    onTrashClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NotesContentInternal(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onSearchClick = onSearchClick,
        onNoteClick = onNoteClick,
        onTrashClick = onTrashClick,
        modifier = modifier
    )
}

/**
 * 노트 화면 내부 구현
 * 노트 우선 뷰: 헤더 → 폴더 필터 칩 → 노트 리스트
 */
@Composable
private fun NotesContentInternal(
    uiState: NotesUiState,
    onEvent: (NotesEvent) -> Unit,
    onSearchClick: () -> Unit,
    onNoteClick: (String) -> Unit,
    onTrashClick: () -> Unit = {},
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
        // 상단 헤더
        NotesHeader(
            onTrashClick = onTrashClick,
            onSearchClick = onSearchClick
        )

        // 폴더 필터 칩
        FolderFilterChips(
            folders = uiState.folders,
            selectedFolderId = uiState.selectedFilterFolderId,
            onFilterSelect = { onEvent(NotesEvent.SelectFilter(it)) },
            onCreateFolder = { onEvent(NotesEvent.ShowCreateFolderDialog) },
            onRenameFolder = { onEvent(NotesEvent.ShowRenameFolderDialog(it)) },
            onDeleteFolder = { onEvent(NotesEvent.DeleteFolder(it)) }
        )

        // 노트 리스트
        when {
            uiState.isLoading -> {
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
            }

            uiState.notes.isEmpty() -> {
                EmptyNotesView(
                    hasFilter = uiState.selectedFilterFolderId != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            else -> {
                NotesList(
                    notes = uiState.notes,
                    onNoteClick = onNoteClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 상단 헤더: Notes 제목 + 휴지통/검색 아이콘
 */
@Composable
private fun NotesHeader(
    onTrashClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val colors = KairosTheme.colors

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

        Row {
            IconButton(onClick = onTrashClick) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "휴지통",
                    tint = colors.icon
                )
            }
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "검색",
                    tint = colors.icon
                )
            }
        }
    }
}

/**
 * 폴더 필터 칩 Row
 * 전체 | Ideas | Bookmarks | Inbox | 사용자폴더 | + 버튼
 * 사용자 폴더 칩 롱프레스 시 이름 변경/삭제 옵션
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderFilterChips(
    folders: List<FolderWithCount>,
    selectedFolderId: String?,
    onFilterSelect: (String?) -> Unit,
    onCreateFolder: () -> Unit,
    onRenameFolder: (com.example.kairos_mobile.domain.model.Folder) -> Unit,
    onDeleteFolder: (String) -> Unit
) {
    val colors = KairosTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "전체" 칩
        KairosChip(
            text = "전체",
            selected = selectedFolderId == null,
            onClick = { onFilterSelect(null) }
        )

        // 폴더 칩
        folders.forEach { folderWithCount ->
            val folder = folderWithCount.folder
            val isUserFolder = folder.type == FolderType.USER
            val chipText = if (folderWithCount.noteCount > 0) {
                "${folder.name} ${folderWithCount.noteCount}"
            } else {
                folder.name
            }

            if (isUserFolder) {
                // 사용자 폴더: 롱프레스 시 메뉴
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    KairosChip(
                        text = chipText,
                        selected = selectedFolderId == folder.id,
                        modifier = Modifier.combinedClickable(
                            onClick = { onFilterSelect(folder.id) },
                            onLongClick = { showMenu = true }
                        )
                    )
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("이름 변경") },
                            onClick = {
                                showMenu = false
                                onRenameFolder(folder)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("삭제", color = colors.danger) },
                            onClick = {
                                showMenu = false
                                onDeleteFolder(folder.id)
                            }
                        )
                    }
                }
            } else {
                // 시스템 폴더
                KairosChip(
                    text = chipText,
                    selected = selectedFolderId == folder.id,
                    onClick = { onFilterSelect(folder.id) }
                )
            }
        }

        // 새 폴더 추가 버튼
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.chipBg)
                .clickable { onCreateFolder() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "새 폴더",
                tint = colors.chipText,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 노트 리스트
 */
@Composable
private fun NotesList(
    notes: List<NoteWithCapture>,
    onNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.testTag("notes_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(
            items = notes,
            key = { it.noteId }
        ) { note ->
            NoteListItem(
                note = note,
                onClick = { onNoteClick(note.noteId) }
            )
        }
    }
}

/**
 * 개별 노트 아이템
 * 제목 + 본문 미리보기 + 폴더 태그 + 날짜
 */
@Composable
private fun NoteListItem(
    note: NoteWithCapture,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val title = note.aiTitle ?: note.originalText.take(40)

    // 본문 미리보기: body가 있으면 body, 없으면 originalText (제목과 다를 때만)
    val preview = when {
        !note.body.isNullOrBlank() -> note.body
        note.aiTitle != null && note.originalText != note.aiTitle -> note.originalText
        else -> null
    }

    val dateFormat = remember { SimpleDateFormat("M/d", Locale.getDefault()) }
    val dateText = dateFormat.format(Date(note.createdAt))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        // 첫 줄: 제목 + 날짜
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = title,
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = dateText,
                color = colors.textMuted,
                fontSize = 12.sp
            )
        }

        // 본문 미리보기
        if (preview != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = preview,
                color = colors.textSecondary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
        }

        // 폴더 태그
        if (note.folderName != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note.folderName,
                color = colors.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // 구분선 (아이템 하단)
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(
            color = colors.divider,
            thickness = 0.5.dp
        )
    }
}

/**
 * 빈 상태 뷰
 */
@Composable
private fun EmptyNotesView(
    hasFilter: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.StickyNote2,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = if (hasFilter) "이 폴더에 노트가 없습니다"
                else "아직 노트가 없습니다",
                color = colors.textMuted,
                fontSize = 15.sp
            )
            if (!hasFilter) {
                Text(
                    text = "캡처한 내용 중 노트로 분류된 항목이 여기에 표시됩니다",
                    color = colors.placeholder,
                    fontSize = 13.sp
                )
            }
        }
    }
}
