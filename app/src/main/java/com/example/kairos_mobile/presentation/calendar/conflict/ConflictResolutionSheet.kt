package com.example.kairos_mobile.presentation.calendar.conflict

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kairos_mobile.domain.model.CalendarConflict
import com.example.kairos_mobile.domain.model.ConflictResolution
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 캘린더 충돌 해결 바텀시트 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictResolutionSheet(
    onDismiss: () -> Unit,
    viewModel: ConflictResolutionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = KairosTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "캘린더 충돌 해결",
                color = colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.accent)
                    }
                }
                uiState.conflicts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("충돌 없음", color = colors.textMuted, fontSize = 15.sp)
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(
                            items = uiState.conflicts,
                            key = { it.scheduleId }
                        ) { conflict ->
                            ConflictCard(
                                conflict = conflict,
                                isResolving = uiState.resolvingId == conflict.scheduleId,
                                onResolve = { resolution ->
                                    viewModel.resolveConflict(conflict, resolution)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 에러 스낵바
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            viewModel.onErrorDismissed()
        }
    }
}

/** 충돌 카드: before/after 비교 */
@Composable
private fun ConflictCard(
    conflict: CalendarConflict,
    isResolving: Boolean,
    onResolve: (ConflictResolution) -> Unit
) {
    val colors = KairosTheme.colors
    val formatter = remember { DateTimeFormatter.ofPattern("M/d HH:mm") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .padding(16.dp)
    ) {
        // 제목 비교
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("로컬", color = colors.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(conflict.localTitle, color = colors.text, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Google", color = colors.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(conflict.googleTitle, color = colors.text, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 시간 비교
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    formatEpochMs(conflict.localStartTime, formatter),
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    formatEpochMs(conflict.googleStartTime, formatter),
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 해결 버튼
        if (isResolving) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.accent, modifier = Modifier.size(24.dp))
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onResolve(ConflictResolution.OVERRIDE_GOOGLE) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accent)
                ) {
                    Text("로컬 유지", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { onResolve(ConflictResolution.OVERRIDE_LOCAL) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accent)
                ) {
                    Text("Google 적용", fontSize = 12.sp)
                }
                Button(
                    onClick = { onResolve(ConflictResolution.MERGE) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("병합", fontSize = 12.sp)
                }
            }
        }
    }
}

private fun formatEpochMs(epochMs: Long, formatter: DateTimeFormatter): String {
    return Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}
