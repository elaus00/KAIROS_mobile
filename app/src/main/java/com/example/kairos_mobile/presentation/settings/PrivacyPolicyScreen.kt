package com.example.kairos_mobile.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kairos_mobile.ui.theme.KairosTheme

/**
 * 개인정보 처리방침 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit = {}
) {
    val colors = KairosTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "개인정보 처리방침",
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // 서문
            PolicySection(
                title = "개인정보 처리방침",
                content = "KAIROS(이하 '앱')는 이용자의 개인정보를 중요하게 생각하며, " +
                        "「개인정보 보호법」 등 관련 법령을 준수하고 있습니다. " +
                        "본 개인정보 처리방침은 앱이 수집하는 개인정보의 항목, 수집 목적, " +
                        "보유 기간 등을 안내합니다."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 1. 수집하는 개인정보
            PolicySection(
                title = "1. 수집하는 개인정보",
                content = """
• 필수 정보: 이메일 주소, 닉네임
• 선택 정보: 프로필 사진
• 자동 수집 정보: 기기 정보, 앱 사용 기록, 접속 로그

서비스 이용 과정에서 아래 정보들이 자동으로 생성되어 수집될 수 있습니다:
- 캡처 데이터 (텍스트, 이미지, 음성)
- 일정 및 할 일 정보
- 메모 및 북마크 데이터
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. 개인정보 수집 목적
            PolicySection(
                title = "2. 개인정보 수집 목적",
                content = """
수집한 개인정보는 다음의 목적을 위해 활용됩니다:

• 서비스 제공: 캡처 저장, 일정 관리, AI 분류 기능 제공
• 회원 관리: 본인 확인, 계정 관리
• 서비스 개선: 사용 패턴 분석, 맞춤형 기능 제공
• 고객 지원: 문의 응대, 공지사항 전달
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. 개인정보 보유 기간
            PolicySection(
                title = "3. 개인정보 보유 기간",
                content = """
이용자의 개인정보는 원칙적으로 개인정보의 수집 및 이용목적이 달성되면 지체 없이 파기합니다.

• 회원 탈퇴 시: 즉시 파기 (단, 법령에 따라 보관이 필요한 경우 해당 기간 동안 보관)
• 관련 법령에 의한 보존:
  - 계약 또는 청약철회 등에 관한 기록: 5년
  - 소비자의 불만 또는 분쟁처리에 관한 기록: 3년
  - 접속 로그: 3개월
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. 개인정보의 제3자 제공
            PolicySection(
                title = "4. 개인정보의 제3자 제공",
                content = """
KAIROS는 이용자의 동의 없이 개인정보를 외부에 제공하지 않습니다.

다만, 아래의 경우에는 예외로 합니다:
• 이용자가 사전에 동의한 경우
• 법령의 규정에 의거하거나, 수사 목적으로 법령에 정해진 절차와 방법에 따라 수사기관의 요구가 있는 경우
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. 이용자의 권리
            PolicySection(
                title = "5. 이용자의 권리",
                content = """
이용자는 언제든지 다음의 권리를 행사할 수 있습니다:

• 개인정보 열람 요구
• 개인정보 정정 요구
• 개인정보 삭제 요구
• 개인정보 처리 정지 요구

위 권리 행사는 앱 내 설정 또는 고객센터를 통해 가능합니다.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 6. 연락처
            PolicySection(
                title = "6. 문의처",
                content = """
개인정보 관련 문의사항은 아래로 연락해 주시기 바랍니다:

• 이메일: privacy@kairos-app.com
• 고객센터: 앱 내 '설정 > 문의하기'

본 개인정보 처리방침은 2026년 1월 1일부터 적용됩니다.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 정책 섹션 컴포넌트
 */
@Composable
private fun PolicySection(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    val colors = KairosTheme.colors

    Column(modifier = modifier) {
        Text(
            text = title,
            color = colors.text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = content,
            color = colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
    }
}
