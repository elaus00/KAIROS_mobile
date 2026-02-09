package com.example.kairos_mobile.presentation.notes.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
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
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 노트 상세/편집 화면
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

    // 폴더 선택 바텀시트 상태
    var showFolderSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "노트",
                        color = colors.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = colors.text
                        )
                    }
                },
                actions = {
                    // 공유 버튼
                    IconButton(onClick = { viewModel.onShare() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "공유",
                            tint = colors.text
                        )
                    }
                    // 저장 버튼 (변경사항이 있을 때만 활성화)
                    if (uiState.hasChanges) {
                        IconButton(
                            onClick = { viewModel.onSave() },
                            enabled = !uiState.isSaving
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
                                    tint = colors.accent
                                )
                            }
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
                    Spacer(modifier = Modifier.height(8.dp))

                    // 분류 칩
                    uiState.noteDetail?.let { detail ->
                        ClassificationChip(
                            classifiedType = detail.classifiedType,
                            noteSubType = detail.noteSubType
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 제목 입력 (라벨 없이, 큰 폰트)
                    BasicTextField(
                        value = uiState.editedTitle,
                        onValueChange = { viewModel.onTitleChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = colors.text,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        cursorBrush = SolidColor(colors.accent),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box {
                                if (uiState.editedTitle.isEmpty()) {
                                    Text(
                                        text = "제목",
                                        color = colors.placeholder,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 제목과 본문 사이 구분선
                    HorizontalDivider(
                        color = colors.divider,
                        thickness = 0.5.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 본문 입력 (라벨 없이, 자연스러운 텍스트 입력)
                    BasicTextField(
                        value = uiState.editedBody,
                        onValueChange = { viewModel.onBodyChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 200.dp),
                        textStyle = TextStyle(
                            color = colors.text,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // 첨부 이미지
                    uiState.noteDetail?.imageUri?.let { imageUri ->
                        Text(
                            text = "첨부 이미지",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(imageUri))
                                .crossfade(true)
                                .build(),
                            contentDescription = "첨부 이미지",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // 폴더 선택
                    Text(
                        text = "폴더",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FolderSelector(
                        folders = uiState.folders,
                        selectedFolderId = uiState.selectedFolderId,
                        onClick = { showFolderSheet = true }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 날짜 표시
                    uiState.noteDetail?.let { detail ->
                        Text(
                            text = "생성일: ${formatDateTime(detail.createdAt)}",
                            color = colors.textMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "수정일: ${formatDateTime(detail.updatedAt)}",
                            color = colors.textMuted,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
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
 * 분류 칩 표시
 */
@Composable
private fun ClassificationChip(
    classifiedType: ClassifiedType,
    noteSubType: com.example.kairos_mobile.domain.model.NoteSubType?,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val label = when {
        classifiedType == ClassifiedType.NOTES && noteSubType == com.example.kairos_mobile.domain.model.NoteSubType.IDEA -> "아이디어"
        classifiedType == ClassifiedType.NOTES && noteSubType == com.example.kairos_mobile.domain.model.NoteSubType.BOOKMARK -> "북마크"
        classifiedType == ClassifiedType.NOTES -> "노트"
        classifiedType == ClassifiedType.TODO -> "할 일"
        classifiedType == ClassifiedType.SCHEDULE -> "일정"
        else -> "임시"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.chipBg)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = colors.chipText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 폴더 선택 버튼
 */
@Composable
private fun FolderSelector(
    folders: List<Folder>,
    selectedFolderId: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val folderName = folders.find { it.id == selectedFolderId }?.name ?: "없음"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = folderName,
                color = colors.text,
                fontSize = 15.sp
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "폴더 선택",
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
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
 * 시간 포맷 (epoch ms -> "yyyy.M.d HH:mm")
 */
private fun formatDateTime(epochMs: Long): String {
    val dateTime = Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    return dateTime.format(DateTimeFormatter.ofPattern("yyyy.M.d HH:mm"))
}
