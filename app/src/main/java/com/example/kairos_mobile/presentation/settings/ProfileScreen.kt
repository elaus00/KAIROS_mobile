package com.example.kairos_mobile.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * 프로필 상세 화면
 * 사용자 정보, 연동 계정, 구독 정보 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {}
) {
    val colors = KairosTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "프로필",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 프로필 이미지 및 사용자 정보
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 프로필 아이콘
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(colors.chipBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "사용자",
                    color = colors.text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "user@email.com",
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 연동 계정 섹션
            Text(
                text = "연동 계정",
                color = colors.textMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            ProfileCard {
                ProfileInfoItem(
                    icon = "G",
                    title = "Google 계정",
                    description = "user@email.com"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 구독 섹션
            Text(
                text = "구독",
                color = colors.textMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            ProfileCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Free",
                            color = colors.text,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "기본 기능 이용 가능",
                            color = colors.textMuted,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = { /* TODO: 업그레이드 */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.card
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "업그레이드",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 로그아웃 버튼
            OutlinedButton(
                onClick = { /* TODO: 로그아웃 */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.danger
                ),
                border = BorderStroke(0.5.dp, colors.borderLight)
            ) {
                Text(
                    text = "로그아웃",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.danger,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 프로필 카드 컨테이너
 */
@Composable
private fun ProfileCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = KairosTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(
                width = 0.5.dp,
                color = colors.borderLight,
                shape = RoundedCornerShape(12.dp)
            ),
        content = content
    )
}

/**
 * 프로필 정보 아이템
 */
@Composable
private fun ProfileInfoItem(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘 (Google 로고 대체)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.chipBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                color = colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = colors.textMuted,
                fontSize = 13.sp
            )
        }
    }
}
