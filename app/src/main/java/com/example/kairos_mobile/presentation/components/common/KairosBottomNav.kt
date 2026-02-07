package com.example.kairos_mobile.presentation.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * Kairos 플로팅 하단 네비게이션 바
 * NOTES(0) ← HOME(1) → CALENDAR(2) 3개 탭
 * 둥근 모서리, 선택 탭 검정 배경
 */
@Composable
fun KairosBottomNav(
    selectedTab: KairosTab = KairosTab.HOME,
    onTabSelected: (KairosTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(colors.card)
                .padding(horizontal = 28.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KairosTab.entries.forEach { tab ->
                KairosNavItem(
                    tab = tab,
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

/**
 * 개별 네비게이션 아이템
 * 선택 시 검정 배경 + 흰색 아이콘/텍스트
 */
@Composable
private fun KairosNavItem(
    tab: KairosTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    // 선택 시: 채워진 아이콘 + 진한 색상
    // 비선택 시: 윤곽 아이콘 + textMuted 색상
    val iconColor by animateColorAsState(
        targetValue = if (selected) colors.text else colors.textMuted,
        animationSpec = tween(durationMillis = 200),
        label = "iconColor"
    )

    Box(
        modifier = modifier
            .testTag("tab_${tab.route}")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (selected) tab.filledIcon else tab.outlinedIcon,
            contentDescription = tab.label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Kairos 탭 정의
 * NOTES(0) ← HOME(1) → CALENDAR(2)
 * 순서: Notes - Home - Calendar (Home이 가운데)
 */
enum class KairosTab(
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val label: String,
    val route: String
) {
    NOTES(
        filledIcon = Icons.Filled.Description,
        outlinedIcon = Icons.Outlined.Description,
        label = "Notes",
        route = "notes"
    ),
    HOME(
        filledIcon = Icons.Filled.Home,
        outlinedIcon = Icons.Outlined.Home,
        label = "Home",
        route = "home"
    ),
    CALENDAR(
        filledIcon = Icons.Filled.CalendarToday,
        outlinedIcon = Icons.Outlined.CalendarToday,
        label = "Calendar",
        route = "calendar"
    )
}
