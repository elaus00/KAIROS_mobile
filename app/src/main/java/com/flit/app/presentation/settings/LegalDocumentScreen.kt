package com.flit.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flit.app.presentation.components.common.AppFontScaleProvider
import com.flit.app.ui.theme.FlitTheme

data class LegalDocumentSection(
    val title: String,
    val bullets: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDocumentScreen(
    title: String,
    draftVersion: String,
    updatedAt: String,
    sections: List<LegalDocumentSection>,
    onNavigateBack: () -> Unit = {}
) {
    AppFontScaleProvider {
    val colors = FlitTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.text,
                    navigationIconContentColor = colors.text
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(colors.background)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Surface(
                color = colors.card,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "초안 문서",
                        color = colors.text,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "버전 $draftVersion · 최종 수정 $updatedAt",
                        color = colors.textMuted,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "본 문서는 Flit 앱 현재 구현 기준의 운영 초안이며, 법률 자문을 대체하지 않습니다.",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            sections.forEachIndexed { index, section ->
                Text(
                    text = "${index + 1}. ${section.title}",
                    color = colors.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                )

                section.bullets.forEach { item ->
                    Text(
                        text = "• $item",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                color = colors.borderLight
            )

            Text(
                text = "문서 기준: PRD v10.1, 기능명세 v2.4, 데이터모델 v2.3, 앱 구현(설정/인증/동기화/분석/캘린더) 기준",
                color = colors.textMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
    }
}
