package com.example.kairos_mobile.presentation.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.domain.model.Bookmark
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.presentation.components.common.KairosBottomNav
import com.example.kairos_mobile.presentation.components.common.KairosTab
import com.example.kairos_mobile.presentation.notes.components.*
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * NotesScreen (PRD v4.0)
 * 노트 + 북마크 통합 화면
 */
@Composable
fun NotesScreen(
    onNavigate: (String) -> Unit,
    onNoteClick: (Note) -> Unit = {},
    onBookmarkClick: (Bookmark) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors

    Scaffold(
        bottomBar = {
            KairosBottomNav(
                selectedTab = KairosTab.NOTES,
                onTabSelected = { tab ->
                    onNavigate(tab.route)
                }
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            // 상단 헤더
            Text(
                text = "Notes",
                color = colors.text,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // 검색 바
            NotesSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onEvent(NotesEvent.UpdateSearchQuery(it)) },
                onClear = { viewModel.onEvent(NotesEvent.ClearSearch) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 탭 (노트/북마크)
            NotesTabRow(
                selectedTab = uiState.selectedTab,
                noteCount = uiState.totalNoteCount,
                bookmarkCount = uiState.totalBookmarkCount,
                onTabSelected = { viewModel.onEvent(NotesEvent.SelectTab(it)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 폴더 Chips (노트 탭일 때만 표시)
            AnimatedVisibility(
                visible = uiState.selectedTab == NotesTab.NOTES,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                FolderChips(
                    selectedFolder = uiState.selectedFolder,
                    noteCountByFolder = uiState.noteCountByFolder,
                    totalCount = uiState.totalNoteCount,
                    onFolderSelected = { viewModel.onEvent(NotesEvent.SelectFolder(it)) }
                )
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
            } else {
                // 컨텐츠
                when (uiState.selectedTab) {
                    NotesTab.NOTES -> NotesList(
                        notes = uiState.notes,
                        onNoteClick = onNoteClick,
                        onNoteDelete = { noteId ->
                            viewModel.onEvent(NotesEvent.DeleteNote(noteId))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    NotesTab.BOOKMARKS -> BookmarksList(
                        bookmarks = uiState.bookmarks,
                        onBookmarkClick = onBookmarkClick,
                        onBookmarkDelete = { bookmarkId ->
                            viewModel.onEvent(NotesEvent.DeleteBookmark(bookmarkId))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 노트 목록
 */
@Composable
private fun NotesList(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onNoteDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (notes.isEmpty()) {
        NotesEmptyState(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = notes,
                key = { it.id }
            ) { note ->
                NoteCard(
                    note = note,
                    onClick = { onNoteClick(note) },
                    onDelete = { onNoteDelete(note.id) }
                )
            }

            // 하단 여백
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * 북마크 목록
 */
@Composable
private fun BookmarksList(
    bookmarks: List<Bookmark>,
    onBookmarkClick: (Bookmark) -> Unit,
    onBookmarkDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (bookmarks.isEmpty()) {
        BookmarksEmptyState(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = bookmarks,
                key = { it.id }
            ) { bookmark ->
                BookmarkCard(
                    bookmark = bookmark,
                    onClick = { onBookmarkClick(bookmark) },
                    onDelete = { onBookmarkDelete(bookmark.id) }
                )
            }

            // 하단 여백
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
