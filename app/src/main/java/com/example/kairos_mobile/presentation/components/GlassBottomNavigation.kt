package com.example.kairos_mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.kairos_mobile.ui.components.glassPanelThemed
import com.example.kairos_mobile.ui.theme.*

/**
 * 미니멀한 글래스모피즘 하단 네비게이션 바
 * Pill 형태 디자인 (cornerRadius: 40dp)
 */
@Composable
fun GlassBottomNavigation(
    selectedTab: NavigationTab = NavigationTab.CAPTURE,
    onTabSelected: (NavigationTab) -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .glassPanelThemed(isDarkTheme = isDarkTheme, shape = RoundedCornerShape(40.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationTab.entries.forEach { tab ->
            GlassNavItem(
                icon = tab.icon,
                label = tab.label,
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                isDarkTheme = isDarkTheme
            )
        }
    }
}

/**
 * 미니멀한 네비게이션 아이템
 */
@Composable
private fun GlassNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // 테마에 따른 색상 설정
    val selectedBgColor = if (isDarkTheme) GlassButtonHover else AiryAccentBlueLight
    val selectedIconColor = if (isDarkTheme) TextPrimary else AiryAccentBlue
    val unselectedIconColor = if (isDarkTheme) TextTertiary else AiryTextTertiary

    IconButton(
        onClick = onClick,
        modifier = modifier.size(44.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .then(
                    if (selected) {
                        Modifier
                            .clip(CircleShape)
                            .background(selectedBgColor)
                    } else {
                        Modifier
                    }
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) selectedIconColor else unselectedIconColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 네비게이션 탭 정의
 */
enum class NavigationTab(val icon: ImageVector, val label: String) {
    CAPTURE(Icons.Default.EditNote, "Capture"),
    SEARCH(Icons.Default.Search, "Search"),
    ARCHIVE(Icons.Default.Inventory2, "Archive"),
    SETTINGS(Icons.Default.Settings, "Settings")
}
