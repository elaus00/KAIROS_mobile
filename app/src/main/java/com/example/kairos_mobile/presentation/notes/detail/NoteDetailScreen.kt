package com.example.kairos_mobile.presentation.notes.detail

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.core.net.toUri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 노트 상세/편집 화면 (Apple Notes 스타일)
 * 콘텐츠 중심 — 제목/본문이 자연스럽게 이어지는 에디터
 *
 * 개선사항:
 * - §4.1 자동 저장: 뒤로가기 시 변경사항 자동 저장
 * - §4.4 삭제: Snackbar 실행 취소 패턴
 * - §3.1 폴더 칩 직접 노출: 수정 마찰 3→1탭
 * - §3.4 원본 텍스트 토글: 원문 확인 가능
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = KairosTheme.colors
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 자동 저장 후 뒤로가기 (§4.1 입력 보호)
    BackHandler {
        viewModel.autoSaveAndExit()
    }

    // navigate back 이벤트 관찰
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            viewModel.onNavigateBackHandled()
            onNavigateBack()
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
            viewModel.onShareHandled()
        }
    }

    // 삭제 시 Snackbar 표시 (§4.4 실행 취소)
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            val result = snackbarHostState.showSnackbar(
                message = "삭제됨",
                actionLabel = "실행 취소",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onUndoDelete()
            }
        }
    }

    // 폴더 선택 바텀시트 상태
    var showFolderSheet by remember { mutableStateOf(false) }
    // 더보기 메뉴 상태
    var showMoreMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { viewModel.autoSaveAndExit() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = colors.text
                        )
                    }
                },
                actions = {
                    // 더보기 메뉴 (공유, 폴더 이동, 삭제, 정보)
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "더보기",
                                tint = colors.text
                            )
                        }
                        NoteMoreMenu(
                            expanded = showMoreMenu,
                            onDismiss = { showMoreMenu = false },
                            onShare = {
                                showMoreMenu = false
                                viewModel.onShare()
                            },
                            onMoveFolder = {
                                showMoreMenu = false
                                showFolderSheet = true
                            },
                            onDelete = {
                                showMoreMenu = false
                                viewModel.onDelete()
                            },
                            noteDetail = uiState.noteDetail,
                            folders = uiState.folders,
                            selectedFolderId = uiState.selectedFolderId
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colors.card,
                    contentColor = colors.text,
                    actionColor = colors.accent
                )
            }
        },
        containerColor = colors.background
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colors.accent,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            uiState.error != null && uiState.noteDetail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "",
                            color = colors.textMuted,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.onRetry() }) {
                            Text(
                                text = "다시 시도",
                                color = colors.accent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // 날짜 (작은 텍스트로 최상단)
                    uiState.noteDetail?.let { detail ->
                        Text(
                            text = formatDateCompact(detail.updatedAt),
                            color = colors.textMuted,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 제목 입력 (큰 폰트, Apple Notes 스타일)
                    BasicTextField(
                        value = uiState.editedTitle,
                        onValueChange = { viewModel.onTitleChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = colors.text,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 36.sp
                        ),
                        cursorBrush = SolidColor(colors.accent),
                        singleLine = false,
                        decorationBox = { innerTextField ->
                            Box {
                                if (uiState.editedTitle.isEmpty()) {
                                    Text(
                                        text = "제목",
                                        color = colors.placeholder,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    // 태그 + 폴더 칩 (§3.1 직접 노출 — 수정 마찰 1탭)
                    val tags = uiState.noteDetail?.tags ?: emptyList()
                    val folderName = uiState.folders.find {
                        it.id == uiState.selectedFolderId
                    }?.name

                    if (tags.isNotEmpty() || folderName != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 폴더 칩 (탭하면 바텀시트 — 1탭으로 수정 가능)
                            if (folderName != null) {
                                FolderChip(
                                    name = folderName,
                                    onClick = { showFolderSheet = true }
                                )
                            }
                            // 태그
                            tags.forEach { tag ->
                                Text(
                                    text = "#$tag",
                                    color = colors.textMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 본문 입력 (자연스러운 연속 영역)
                    BasicTextField(
                        value = uiState.editedBody,
                        onValueChange = { viewModel.onBodyChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 300.dp),
                        textStyle = TextStyle(
                            color = colors.text,
                            fontSize = 16.sp,
                            lineHeight = 26.sp
                        ),
                        cursorBrush = SolidColor(colors.accent),
                        decorationBox = { innerTextField ->
                            Box {
                                if (uiState.editedBody.isEmpty()) {
                                    Text(
                                        text = "내용을 입력하세요...",
                                        color = colors.placeholder,
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    // 원본 텍스트 토글 (§3.4 원본 보존 원칙)
                    uiState.noteDetail?.let { detail ->
                        val currentBody = uiState.editedBody
                        val originalText = detail.originalText
                        // 현재 편집 중인 본문과 원본이 다를 때만 표시
                        if (originalText.isNotBlank() && originalText != currentBody) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OriginalTextSection(
                                originalText = originalText,
                                expanded = uiState.showOriginalText,
                                onToggle = { viewModel.onToggleOriginalText() }
                            )
                        }
                    }

                    // 첨부 이미지 (라벨 없이, 라운드 카드)
                    uiState.noteDetail?.imageUri?.let { imageUri ->
                        Spacer(modifier = Modifier.height(24.dp))
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUri.toUri())
                                .crossfade(true)
                                .build(),
                            contentDescription = "첨부 이미지",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }

    // 폴더 선택 바텀시트
    if (showFolderSheet) {
        FolderBottomSheet(
            folders = uiState.folders,
            selectedFolderId = uiState.selectedFolderId,
            onFolderSelected = { folderId ->
                viewModel.onFolderChanged(folderId)
                showFolderSheet = false
            },
            onDismiss = { showFolderSheet = false }
        )
    }
}

/**
 * 폴더 칩 (탭하면 바텀시트 열림 — 수정 마찰 1탭)
 */
@Composable
private fun FolderChip(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.chipBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Folder,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = name,
            color = colors.textMuted,
            fontSize = 13.sp
        )
    }
}

/**
 * 원본 텍스트 접기/펼치기 섹션 (§3.4 원본 보존 원칙)
 */
@Composable
private fun OriginalTextSection(
    originalText: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        // 토글 버튼
        Row(
            modifier = Modifier
                .clickable(onClick = onToggle)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (expanded) "원문 접기" else "원문 보기",
                color = colors.textMuted,
                fontSize = 13.sp
            )
        }

        // 원문 내용
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Text(
                text = originalText,
                color = colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.chipBg)
                    .padding(12.dp)
            )
        }
    }
}

/**
 * 더보기 드롭다운 메뉴 (공유, 폴더 이동, 삭제, 노트 정보)
 */
@Composable
private fun NoteMoreMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onMoveFolder: () -> Unit,
    onDelete: () -> Unit,
    noteDetail: com.example.kairos_mobile.domain.model.NoteDetail?,
    folders: List<Folder>,
    selectedFolderId: String?
) {
    val colors = KairosTheme.colors
    val folderName = folders.find { it.id == selectedFolderId }?.name

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        containerColor = colors.card,
        shadowElevation = 4.dp,
        modifier = Modifier.widthIn(min = 200.dp)
    ) {
        // 공유
        DropdownMenuItem(
            text = {
                Text(
                    text = "공유",
                    color = colors.text,
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            onClick = onShare
        )
        // 폴더 이동
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "폴더",
                        color = colors.text,
                        fontSize = 15.sp
                    )
                    if (folderName != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = folderName,
                            color = colors.textMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            onClick = onMoveFolder
        )
        // 삭제 (§4.4 Snackbar 실행 취소)
        DropdownMenuItem(
            text = {
                Text(
                    text = "삭제",
                    color = colors.danger,
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = colors.danger,
                    modifier = Modifier.size(18.dp)
                )
            },
            onClick = onDelete
        )
        // 노트 정보 (날짜)
        noteDetail?.let { detail ->
            HorizontalDivider(color = colors.divider)
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            text = "생성  ${formatDateTime(detail.createdAt)}",
                            color = colors.textMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "수정  ${formatDateTime(detail.updatedAt)}",
                            color = colors.textMuted,
                            fontSize = 12.sp
                        )
                    }
                },
                onClick = {},
                enabled = false
            )
        }
    }
}

/**
 * 폴더 선택 바텀시트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderBottomSheet(
    folders: List<Folder>,
    selectedFolderId: String?,
    onFolderSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = KairosTheme.colors
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
                val isSelected = folder.id == selectedFolderId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) colors.accentBg else colors.background)
                        .clickable { onFolderSelected(folder.id) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = folder.name,
                        color = if (isSelected) colors.accent else colors.text,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "선택됨",
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 간결한 날짜 포맷 (예: "2월 11일")
 */
private fun formatDateCompact(epochMs: Long): String {
    val dateTime = Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    return dateTime.format(DateTimeFormatter.ofPattern("M월 d일"))
}

/**
 * 상세 날짜 포맷 (예: "2026.2.11 14:30")
 */
private fun formatDateTime(epochMs: Long): String {
    val dateTime = Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    return dateTime.format(DateTimeFormatter.ofPattern("yyyy.M.d HH:mm"))
}
