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

/**
 * 캡처 그리드 컴포넌트 (PRD v4.0)
 * 디자인 시안 반영: 3열 그리드, 제목 상단, 타입칩 하단 좌측
 */
@Composable
fun CaptureGrid(
    captures: List<Capture>,
    onCaptureClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
 * 디자인: 제목 상단, 타입칩 하단 좌측
 */
@Composable
private fun CaptureGridItem(
    capture: Capture,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier
            .aspectRatio(0.85f)  // 디자인에 맞게 세로로 더 긴 비율
            .kairosCard(elevation = 0.dp)  // shadow 제거
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단: 내용 (제목)
            Text(
                text = capture.content.take(50),
                color = colors.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // 하단: 타입 칩
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.chipBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = getTypeDisplayName(capture.classification?.type),
                    color = colors.chipText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

/**
 * CaptureType 표시 이름
 * 디자인 시안: "일정", "할 일", "아이디어", "노트"
 */
private fun getTypeDisplayName(type: CaptureType?): String {
    return when (type) {
        CaptureType.IDEA -> "아이디어"
        CaptureType.SCHEDULE -> "일정"
        CaptureType.TODO -> "할 일"
        CaptureType.NOTE -> "노트"
        CaptureType.QUICK_NOTE -> "메모"
        CaptureType.CLIP -> "클립"
        null -> "노트"
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
