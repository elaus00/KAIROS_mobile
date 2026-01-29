package com.example.kairos_mobile.presentation.notes.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kairos_mobile.domain.model.NoteFolder
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * NoteEditScreen (PRD v4.0)
 * 노트 편집 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors

    // 저장 성공 시 뒤로가기
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isNewNote) "새 노트" else "노트 편집",
                        color = colors.text,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                            tint = colors.icon
                        )
                    }
                },
                actions = {
                    // 삭제 버튼 (기존 노트일 때만)
                    if (!uiState.isNewNote) {
                        IconButton(
                            onClick = { viewModel.onEvent(NoteEditEvent.Delete) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "삭제",
                                tint = colors.danger
                            )
                        }
                    }

                    // 저장 버튼
                    IconButton(
                        onClick = { viewModel.onEvent(NoteEditEvent.Save) },
                        enabled = uiState.canSave && !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colors.accent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "저장",
                                tint = if (uiState.canSave) colors.accent else colors.textMuted
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.accent)
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 제목 입력
                TitleInput(
                    title = uiState.title,
                    onTitleChange = { viewModel.onEvent(NoteEditEvent.UpdateTitle(it)) }
                )

                // 폴더 선택
                FolderSelector(
                    selectedFolder = uiState.folder,
                    onFolderSelected = { viewModel.onEvent(NoteEditEvent.UpdateFolder(it)) }
                )

                // 태그
                TagsSection(
                    tags = uiState.tags,
                    newTagInput = uiState.newTagInput,
                    onNewTagInputChange = { viewModel.onEvent(NoteEditEvent.UpdateNewTagInput(it)) },
                    onAddTag = { viewModel.onEvent(NoteEditEvent.AddTag) },
                    onRemoveTag = { viewModel.onEvent(NoteEditEvent.RemoveTag(it)) }
                )

                // 내용 입력
                ContentInput(
                    content = uiState.content,
                    onContentChange = { viewModel.onEvent(NoteEditEvent.UpdateContent(it)) }
                )
            }
        }
    }
}

/**
 * 제목 입력
 */
@Composable
private fun TitleInput(
    title: String,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Column(modifier = modifier) {
        Text(
            text = "제목",
            color = colors.textMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            textStyle = TextStyle(
                color = colors.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            ),
            cursorBrush = SolidColor(colors.accent),
            decorationBox = { innerTextField ->
                Box {
                    if (title.isEmpty()) {
                        Text(
                            text = "제목을 입력하세요",
                            color = colors.placeholder,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 폴더 선택
 */
@Composable
private fun FolderSelector(
    selectedFolder: NoteFolder,
    onFolderSelected: (NoteFolder) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Column(modifier = modifier) {
        Text(
            text = "폴더",
            color = colors.textMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NoteFolder.entries.forEach { folder ->
                val isSelected = folder == selectedFolder

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) colors.accent else colors.chipBg
                        )
                        .clickable { onFolderSelected(folder) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = folder.getDisplayName(),
                        color = if (isSelected) {
                            if (colors.isDark) colors.background else Color.White
                        } else {
                            colors.chipText
                        },
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * 태그 섹션
 */
@Composable
private fun TagsSection(
    tags: List<String>,
    newTagInput: String,
    onNewTagInputChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Column(modifier = modifier) {
        Text(
            text = "태그",
            color = colors.textMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 태그 목록
        if (tags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        onRemove = { onRemoveTag(tag) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // 새 태그 입력
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.chipBg)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#",
                color = colors.textMuted,
                fontSize = 14.sp
            )

            BasicTextField(
                value = newTagInput,
                onValueChange = onNewTagInputChange,
                textStyle = TextStyle(
                    color = colors.text,
                    fontSize = 14.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(colors.accent),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (newTagInput.isEmpty()) {
                            Text(
                                text = "태그 추가",
                                color = colors.placeholder,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.weight(1f)
            )

            if (newTagInput.isNotEmpty()) {
                IconButton(
                    onClick = onAddTag,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "태그 추가",
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * 태그 Chip
 */
@Composable
private fun TagChip(
    tag: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.accentBg)
            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$tag",
            color = colors.accent,
            fontSize = 13.sp
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "태그 삭제",
                tint = colors.accent,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * 내용 입력
 */
@Composable
private fun ContentInput(
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Column(modifier = modifier) {
        Text(
            text = "내용",
            color = colors.textMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = content,
            onValueChange = onContentChange,
            textStyle = TextStyle(
                color = colors.text,
                fontSize = 15.sp,
                lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(colors.accent),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.chipBg)
                        .padding(16.dp)
                ) {
                    if (content.isEmpty()) {
                        Text(
                            text = "내용을 입력하세요...",
                            color = colors.placeholder,
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
