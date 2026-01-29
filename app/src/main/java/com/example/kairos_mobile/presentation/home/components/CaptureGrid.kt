package com.example.kairos_mobile.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.ui.components.kairosCard
import com.example.kairos_mobile.ui.theme.KairosTheme
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 캡처 그리드 컴포넌트 (PRD v4.0)
 * 최근 캡처 3열 그리드
 */
@Composable
fun CaptureGrid(
    captures: List<Capture>,
    onCaptureClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = captures,
            key = { it.id }
        ) { capture ->
            CaptureGridItem(
                capture = capture,
                onClick = { onCaptureClick(capture.id) }
            )
        }
    }
}

/**
 * 캡처 그리드 아이템
 */
@Composable
private fun CaptureGridItem(
    capture: Capture,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .kairosCard()
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 타입 표시
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.chipBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = getTypeShortName(capture.classification?.type),
                    color = colors.chipText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 내용 미리보기
            Text(
                text = capture.content.take(40),
                color = colors.text,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            // 시간
            Text(
                text = timeFormat.format(capture.timestamp),
                color = colors.textMuted,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * CaptureType의 짧은 이름
 */
private fun getTypeShortName(type: CaptureType?): String {
    return when (type) {
        CaptureType.TODO -> "할일"
        CaptureType.IDEA -> "아이디어"
        CaptureType.NOTE -> "노트"
        CaptureType.QUICK_NOTE -> "메모"
        CaptureType.CLIP -> "클립"
        else -> "노트"
    }
}

/**
 * 캡처 그리드 빈 상태
 */
@Composable
fun CaptureGridEmpty(
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "아직 캡처가 없습니다",
                color = colors.textMuted,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "아래에서 생각을 캡처해보세요",
                color = colors.textMuted,
                fontSize = 12.sp
            )
        }
    }
}
