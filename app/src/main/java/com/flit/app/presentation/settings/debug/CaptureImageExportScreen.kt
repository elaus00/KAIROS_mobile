package com.flit.app.presentation.settings.debug

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.flit.app.presentation.components.common.FlitSnackbar
import com.flit.app.presentation.settings.components.SettingsCard
import com.flit.app.presentation.settings.components.SettingsDivider
import com.flit.app.ui.theme.FlitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureImageExportScreen(
    viewModel: CaptureImageExportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CaptureImageExportEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val colors = FlitTheme.colors
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .imePadding()
            ) { data ->
                FlitSnackbar(snackbarData = data)
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "이미지 내보내기",
                        color = colors.text,
                        fontSize = 20.sp,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        CaptureImageExportContent(
            uiState = uiState,
            onNameChange = viewModel::updateEditableName,
            onExport = viewModel::exportImage,
            onDelete = viewModel::deleteImage,
            onReload = viewModel::reload,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun CaptureImageExportContent(
    uiState: CaptureImageExportUiState,
    onNameChange: (String, String) -> Unit,
    onExport: (String) -> Unit,
    onDelete: (String) -> Unit,
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FlitTheme.colors
    if (uiState.isLoading) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = colors.accent,
                modifier = Modifier.size(28.dp)
            )
        }
        return
    }

    if (uiState.items.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("업로드된 이미지가 없습니다.", color = colors.textMuted, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onReload) {
                Text("새로고침")
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(uiState.items, key = { it.id }) { item ->
            SettingsCard {
                Column(modifier = Modifier.padding(12.dp)) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.previewUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "업로드 이미지",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.originalName,
                        color = colors.textMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = item.editableName,
                        onValueChange = { onNameChange(item.id, it) },
                        label = { Text("내보낼 파일 이름") },
                        placeholder = {
                            Text(item.originalName.substringBeforeLast(".", item.originalName))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { onDelete(item.id) }
                        ) {
                            Text("삭제")
                        }
                        Button(
                            enabled = !item.isExporting,
                            onClick = { onExport(item.id) }
                        ) {
                            if (item.isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("내보내기")
                            }
                        }
                    }
                }
                SettingsDivider()
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CaptureImageExportContentPreview() {
    FlitTheme {
        CaptureImageExportContent(
            uiState = CaptureImageExportUiState(
                isLoading = false,
                items = listOf(
                    CaptureImageExportItem(
                        id = "1",
                        path = "/tmp/a.jpg",
                        previewUri = "",
                        originalName = "img_1.jpg",
                        fileExtension = "jpg",
                        editableName = "회의록_샘플"
                    )
                )
            ),
            onNameChange = { _, _ -> },
            onExport = {},
            onDelete = {},
            onReload = {}
        )
    }
}
