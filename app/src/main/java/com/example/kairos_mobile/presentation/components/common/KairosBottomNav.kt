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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * Kairos 하단 네비게이션 바 (PRD v4.0)
 * HOME / CALENDAR / NOTES / SETTINGS 4개 탭
 * 무채색 미니멀리스트 디자인
 */
@Composable
fun KairosBottomNav(
    selectedTab: KairosTab = KairosTab.HOME,
    onTabSelected: (KairosTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.card)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        KairosTab.entries.forEach { tab ->
            KairosNavItem(
                tab = tab,
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 개별 네비게이션 아이템
 */
@Composable
private fun KairosNavItem(
    tab: KairosTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    // 애니메이션 색상
    val iconColor by animateColorAsState(
        targetValue = if (selected) colors.accent else colors.iconMuted,
        animationSpec = tween(durationMillis = 200),
        label = "iconColor"
    )

    val labelColor by animateColorAsState(
        targetValue = if (selected) colors.text else colors.textMuted,
        animationSpec = tween(durationMillis = 200),
        label = "labelColor"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) colors.accentBg else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "backgroundColor"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // 리플 효과 제거 (미니멀)
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) tab.filledIcon else tab.outlinedIcon,
            contentDescription = tab.label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tab.label,
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            letterSpacing = 0.3.sp
        )
    }
}

/**
 * Kairos 탭 정의 (PRD v4.0)
 * HOME / CALENDAR / NOTES / SETTINGS
 */
enum class KairosTab(
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val label: String,
    val route: String
) {
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
    ),
    NOTES(
        filledIcon = Icons.Filled.Description,
        outlinedIcon = Icons.Outlined.Description,
        label = "Notes",
        route = "notes"
    ),
    SETTINGS(
        filledIcon = Icons.Filled.Settings,
        outlinedIcon = Icons.Outlined.Settings,
        label = "Settings",
        route = "settings"
    )
}
