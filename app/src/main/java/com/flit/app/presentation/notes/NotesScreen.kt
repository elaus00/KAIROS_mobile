package com.flit.app.presentation.notes

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOpen
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flit.app.domain.model.Folder
import com.flit.app.domain.model.FolderType
import com.flit.app.domain.model.NoteSubType
import com.flit.app.domain.model.NoteViewType
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
        onShareNote = viewModel::shareNote,
        onSetNoteViewType = viewModel::setNoteViewType,
        modifier = modifier
    )
    }
}

/**
 * 노트 화면 내부 구현
 * 노트 우선 뷰: 헤더 → 폴더 필터 칩 → 노트 리스트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotesContentInternal(
    uiState: NotesUiState,
    onEvent: (NotesEvent) -> Unit,
    onSearchClick: () -> Unit,
    onNoteClick: (String) -> Unit,
    onTrashClick: () -> Unit = {},
    onReorganizeClick: () -> Unit = {},
    onShareNote: (NoteWithCapture) -> Unit = {},
    onSetNoteViewType: (NoteViewType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 폴더 선택 바텀시트 상태
    var folderSheetNoteId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onEvent(NotesEvent.DismissError)
        }
    }

    // 공유 텍스트가 설정되면 ShareSheet 실행
    LaunchedEffect(uiState.shareText) {
        uiState.shareText?.let { text ->
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, null))
            onEvent(NotesEvent.DismissShareText)
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
                showReorganize = uiState.isPremium,
                currentViewType = uiState.viewType,
                onSetNoteViewType = onSetNoteViewType
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
                    when (uiState.viewType) {
                        NoteViewType.LIST -> {
                            NotesListView(
                                notes = uiState.notes,
                                expandedNoteId = uiState.expandedNoteId,
                                onToggleExpand = { onEvent(NotesEvent.ToggleNoteExpand(it)) },
                                onNoteClick = onNoteClick,
                                onShareNote = onShareNote,
                                onDeleteNote = { onEvent(NotesEvent.DeleteNote(it)) },
                                onMoveFolderNote = { noteId -> folderSheetNoteId = noteId },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        NoteViewType.GRID -> {
                            NotesGridView(
                                notes = uiState.notes,
                                onNoteClick = onNoteClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        NoteViewType.COMPACT -> {
                            NotesCompactView(
                                notes = uiState.notes,
                                expandedNoteId = uiState.expandedNoteId,
                                onToggleExpand = { onEvent(NotesEvent.ToggleNoteExpand(it)) },
                                onNoteClick = onNoteClick,
                                onShareNote = onShareNote,
                                onDeleteNote = { onEvent(NotesEvent.DeleteNote(it)) },
                                onMoveFolderNote = { noteId -> folderSheetNoteId = noteId },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
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

    // 폴더 선택 바텀시트
    folderSheetNoteId?.let { noteId ->
        FolderSelectSheet(
            folders = uiState.folders.map { it.folder },
            onFolderSelected = { folderId ->
                onEvent(NotesEvent.MoveNoteToFolder(noteId, folderId))
                folderSheetNoteId = null
            },
            onDismiss = { folderSheetNoteId = null }
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
    showReorganize: Boolean = false,
    currentViewType: NoteViewType = NoteViewType.LIST,
    onSetNoteViewType: (NoteViewType) -> Unit = {}
) {
    val colors = FlitTheme.colors
    var showViewTypeMenu by remember { mutableStateOf(false) }

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
            fontSize = 20.sp,
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
            // 노트 보기 유형 메뉴
            Box {
                IconButton(onClick = { showViewTypeMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "보기 설정",
                        tint = colors.icon
                    )
                }
                DropdownMenu(
                    expanded = showViewTypeMenu,
                    onDismissRequest = { showViewTypeMenu = false }
                ) {
                    NoteViewType.entries.forEach { type ->
                        val label = when (type) {
                            NoteViewType.LIST -> "리스트"
                            NoteViewType.GRID -> "그리드"
                            NoteViewType.COMPACT -> "컴팩트"
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    color = colors.text,
                                    fontWeight = if (type == currentViewType) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSetNoteViewType(type)
                                showViewTypeMenu = false
                            }
                        )
                    }
                }
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

// ──────────────────────────────────────────
// 리스트 뷰 (확장 가능 카드)
// ──────────────────────────────────────────

/**
 * 리스트 뷰 — 확장 가능 카드
 */
@Composable
private fun NotesListView(
    notes: List<NoteWithCapture>,
    expandedNoteId: String?,
    onToggleExpand: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onShareNote: (NoteWithCapture) -> Unit,
    onDeleteNote: (String) -> Unit,
    onMoveFolderNote: (String) -> Unit,
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
                isExpanded = expandedNoteId == note.noteId,
                onToggleExpand = { onToggleExpand(note.noteId) },
                onEdit = { onNoteClick(note.noteId) },
                onShare = { onShareNote(note) },
                onDelete = { onDeleteNote(note.captureId) },
                onMoveFolder = { onMoveFolderNote(note.noteId) }
            )
        }
    }
}

/**
 * 개별 노트 아이템 (확장 가능)
 * 축소: 제목 1줄 + 본문 미리보기 2줄 + 날짜 + 타입칩 + 폴더
 * 확장: 제목 전문 + 본문 전문 (최대 10줄) + 폴더 + 액션 Row
 */
@Composable
private fun NoteListItem(
    note: NoteWithCapture,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onMoveFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    val title = note.aiTitle ?: note.originalText.take(40)

    // 본문 미리보기
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
            .clickable(onClick = onToggleExpand)
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
                maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = dateText,
                color = colors.textMuted,
                fontSize = 12.sp
            )
        }

        // 본문 미리보기 / 전문
        if (preview != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = preview,
                color = colors.textSecondary,
                fontSize = 13.sp,
                maxLines = if (isExpanded) 10 else 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
        }

        // 분류 칩 + 폴더 태그
        val subTypeLabel = noteSubTypeLabel(note.noteSubType)
        if (subTypeLabel != null || note.folderName != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

        // 확장 시 액션 Row
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            NoteActionRow(
                onShare = onShare,
                onMoveFolder = onMoveFolder,
                onDelete = onDelete,
                onEdit = onEdit,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

/**
 * 노트 액션 Row — 공유 | 폴더 | 삭제 | 편집
 */
@Composable
private fun NoteActionRow(
    onShare: () -> Unit,
    onMoveFolder: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = onShare) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "공유",
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onMoveFolder) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = "폴더 이동",
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "삭제",
                tint = colors.danger,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "편집",
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ──────────────────────────────────────────
// 그리드 뷰 (2열 카드)
// ──────────────────────────────────────────

/**
 * 그리드 뷰 — 2열 카드형, 탭 → NoteDetailScreen
 */
@Composable
private fun NotesGridView(
    notes: List<NoteWithCapture>,
    onNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.testTag("notes_grid"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = notes,
            key = { it.noteId }
        ) { note ->
            NoteGridItem(
                note = note,
                onClick = { onNoteClick(note.noteId) }
            )
        }
    }
}

/**
 * 그리드 노트 카드
 */
@Composable
private fun NoteGridItem(
    note: NoteWithCapture,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    val title = note.aiTitle ?: note.originalText.take(40)

    val preview = when {
        !note.body.isNullOrBlank() -> note.body
        note.aiTitle != null && note.originalText != note.aiTitle -> note.originalText
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(0.5.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            color = colors.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (preview != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = preview,
                color = colors.textSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
        }

        val subTypeLabel = noteSubTypeLabel(note.noteSubType)
        if (subTypeLabel != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subTypeLabel,
                color = colors.accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.accentBg)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            )
        }
    }
}

// ──────────────────────────────────────────
// 컴팩트 뷰 (제목만, 확장 가능)
// ──────────────────────────────────────────

/**
 * 컴팩트 뷰 — 제목 + 날짜만, 확장 시 본문 + 액션
 */
@Composable
private fun NotesCompactView(
    notes: List<NoteWithCapture>,
    expandedNoteId: String?,
    onToggleExpand: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onShareNote: (NoteWithCapture) -> Unit,
    onDeleteNote: (String) -> Unit,
    onMoveFolderNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.testTag("notes_compact"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(
            items = notes,
            key = { _, note -> note.noteId }
        ) { _, note ->
            NoteCompactItem(
                note = note,
                isExpanded = expandedNoteId == note.noteId,
                onToggleExpand = { onToggleExpand(note.noteId) },
                onEdit = { onNoteClick(note.noteId) },
                onShare = { onShareNote(note) },
                onDelete = { onDeleteNote(note.captureId) },
                onMoveFolder = { onMoveFolderNote(note.noteId) }
            )
        }
    }
}

/**
 * 컴팩트 노트 아이템
 * 축소: 제목 1줄 + 날짜
 * 확장: 본문 + 액션 Row
 */
@Composable
private fun NoteCompactItem(
    note: NoteWithCapture,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onMoveFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    val title = note.aiTitle ?: note.originalText.take(40)
    val dateFormat = remember { SimpleDateFormat("M/d", Locale.getDefault()) }
    val dateText = dateFormat.format(Date(note.createdAt))

    val preview = when {
        !note.body.isNullOrBlank() -> note.body
        note.aiTitle != null && note.originalText != note.aiTitle -> note.originalText
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(0.5.dp, colors.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onToggleExpand)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = colors.text,
                fontSize = 14.sp,
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

        // 확장 영역
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                if (preview != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = preview,
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }

                NoteActionRow(
                    onShare = onShare,
                    onMoveFolder = onMoveFolder,
                    onDelete = onDelete,
                    onEdit = onEdit,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// ──────────────────────────────────────────
// 공통 컴포넌트
// ──────────────────────────────────────────

/**
 * 폴더 선택 바텀시트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderSelectSheet(
    folders: List<Folder>,
    onFolderSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = FlitTheme.colors
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "폴더 선택",
                color = colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            folders.forEach { folder ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onFolderSelected(folder.id) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = folder.name,
                        color = colors.text,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
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
                text = if (hasFilter) "이 폴더에 노트가 없어요"
                else "아직 노트가 없어요",
                color = colors.textMuted,
                fontSize = 15.sp
            )
            if (!hasFilter) {
                Text(
                    text = "캡처하면 노트로 분류된 항목이 여기에 나타나요",
                    color = colors.placeholder,
                    fontSize = 13.sp
                )
            }
        }
    }
}
