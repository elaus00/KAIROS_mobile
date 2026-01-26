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
import com.example.kairos_mobile.ui.components.glassPanel
import com.example.kairos_mobile.ui.theme.*

/**
 * 미니멀한 글래스모피즘 하단 네비게이션 바
 */
@Composable
fun GlassBottomNavigation(
    selectedTab: NavigationTab = NavigationTab.CAPTURE,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .glassPanel(shape = RoundedCornerShape(40))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationTab.entries.forEach { tab ->
            GlassNavItem(
                icon = tab.icon,
                label = tab.label,
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) }
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
    modifier: Modifier = Modifier
) {
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
                            .background(GlassButtonHover)
                    } else {
                        Modifier
                    }
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) TextPrimary else TextTertiary,
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
