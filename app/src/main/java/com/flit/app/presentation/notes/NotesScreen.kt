package com.flit.app.presentation.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flit.app.domain.model.FolderType
import com.flit.app.domain.model.NoteSubType
import com.flit.app.presentation.components.common.AppFontScaleProvider
import com.flit.app.presentation.components.common.FlitChip
import com.flit.app.ui.theme.FlitTheme
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
    onReorganizeClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    AppFontScaleProvider {
    val uiState by viewModel.uiState.collectAsState()

    NotesContentInternal(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onSearchClick = onSearchClick,
        onNoteClick = onNoteClick,
        onTrashClick = onTrashClick,
        onReorganizeClick = onReorganizeClick,
        modifier = modifier
    )
    }
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
    onReorganizeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onEvent(NotesEvent.DismissError)
        }
    }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            // 상단 헤더
            NotesHeader(
                onTrashClick = onTrashClick,
                onSearchClick = onSearchClick,
                onReorganizeClick = onReorganizeClick,
                showReorganize = uiState.isPremium
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

/**
 * 상단 헤더: Notes 제목 + 휴지통/검색 아이콘
 */
@Composable
private fun NotesHeader(
    onTrashClick: () -> Unit,
    onSearchClick: () -> Unit,
    onReorganizeClick: () -> Unit = {},
    showReorganize: Boolean = false
) {
    val colors = FlitTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "노트",
            color = colors.text,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row {
            // AI 재구성 버튼 — 프리미엄 구독자에게만 표시
            if (showReorganize) {
                IconButton(onClick = onReorganizeClick) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "AI 재구성",
                        tint = colors.icon
                    )
                }
            }
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
    onRenameFolder: (com.flit.app.domain.model.Folder) -> Unit,
    onDeleteFolder: (String) -> Unit
) {
    val colors = FlitTheme.colors

    // 수평 스크롤이 부모 HorizontalPager에 전파되지 않도록 차단
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // 남은 수평 스크롤을 부모에 전파하지 않음
                return available.copy(y = 0f)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return available.copy(y = 0f)
            }
        }
    }

    // overscroll 효과 비활성화로 스크롤 범위 초과 방지
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(nestedScrollConnection)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "전체" 칩
            FlitChip(
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
                        FlitChip(
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
                    FlitChip(
                        text = chipText,
                        selected = selectedFolderId == folder.id,
                        onClick = { onFilterSelect(folder.id) }
                    )
                }
            }

            // 새 폴더 추가 버튼
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.chipBg)
                    .clickable { onCreateFolder() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "새 폴더",
                    tint = colors.chipText,
                    modifier = Modifier.size(20.dp)
                )
            }
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
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(
            items = notes,
            key = { _, note -> note.noteId }
        ) { _, note ->
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
    val colors = FlitTheme.colors
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
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(0.5.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
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
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = preview,
                color = colors.textSecondary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
        }

        // 분류 칩 + 폴더 태그 (§4.3 분류 칩 항상 표시)
        val subTypeLabel = noteSubTypeLabel(note.noteSubType)
        if (subTypeLabel != null || note.folderName != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 서브타입 칩 (IDEA, BOOKMARK 등)
                if (subTypeLabel != null) {
                    Text(
                        text = subTypeLabel,
                        color = colors.accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.accentBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                // 폴더 이름
                if (note.folderName != null) {
                    Text(
                        text = note.folderName,
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * NoteSubType → 표시 라벨 변환
 * INBOX/USER_FOLDER는 폴더 이름으로 이미 표시되므로 칩 생략
 */
private fun noteSubTypeLabel(subType: String?): String? {
    return when (subType) {
        NoteSubType.IDEA.name -> "Idea"
        NoteSubType.BOOKMARK.name -> "Bookmark"
        else -> null
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
    val colors = FlitTheme.colors

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
