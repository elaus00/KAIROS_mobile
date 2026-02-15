package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.flit.app.presentation.settings.LegalDocumentScreen
import com.flit.app.presentation.settings.LegalDocumentSection
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * LegalDocumentScreen 스크린샷 테스트
 * 개인정보 처리방침, 서비스 이용약관 등 법적 문서 화면 UI 검증
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LegalDocumentScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleSections = listOf(
        LegalDocumentSection(
            title = "개인정보의 수집 및 이용 목적",
            bullets = listOf(
                "회원 가입 및 관리: 회원 식별, 본인 확인, 고지사항 전달",
                "서비스 제공: 멀티모달 캡처 및 자동 분류, 노트 생성, 캘린더 동기화",
                "AI 기능 제공: 텍스트 및 이미지 분석을 통한 자동 분류 및 태그 생성"
            )
        ),
        LegalDocumentSection(
            title = "수집하는 개인정보 항목",
            bullets = listOf(
                "필수 항목: 이메일, 사용자 ID(Google 계정 정보)",
                "자동 수집 항목: 기기 ID, 앱 사용 로그, 캡처 메타데이터(생성일시, 유형)",
                "선택 항목: 프로필 이름, 프로필 사진(Google 계정 연동 시)"
            )
        ),
        LegalDocumentSection(
            title = "개인정보의 보유 및 이용 기간",
            bullets = listOf(
                "회원 탈퇴 시까지 또는 서비스 종료 시까지 보유",
                "법령에 따라 보존이 필요한 경우 해당 기간 동안 보유",
                "탈퇴 후 30일 이내 파기(법령 보존 의무 항목 제외)"
            )
        )
    )

    @Test
    fun privacyPolicyScreen() {
        composeTestRule.captureScreenshot("legal_privacy_policy.png") {
            LegalDocumentScreen(
                title = "개인정보 처리방침",
                version = "1.0",
                effectiveDate = "2026-02-14",
                sections = sampleSections,
                onNavigateBack = {}
            )
        }
        composeTestRule.onNodeWithText("개인정보 처리방침").assertIsDisplayed()
    }

    @Test
    fun termsOfServiceScreen() {
        composeTestRule.captureScreenshot("legal_terms_of_service.png") {
            LegalDocumentScreen(
                title = "서비스 이용약관",
                version = "1.0",
                effectiveDate = "2026-02-14",
                sections = listOf(
                    LegalDocumentSection(
                        title = "서비스 이용",
                        bullets = listOf(
                            "회원은 본 약관 및 관련 법령을 준수하여 서비스를 이용해야 합니다",
                            "회원은 타인의 개인정보를 무단으로 수집, 저장, 공개하는 행위를 금지합니다",
                            "회사는 회원이 약관을 위반할 경우 서비스 이용을 제한할 수 있습니다"
                        )
                    ),
                    LegalDocumentSection(
                        title = "콘텐츠 및 지적재산권",
                        bullets = listOf(
                            "회원이 작성한 캡처 및 노트는 회원에게 귀속됩니다",
                            "회사는 서비스 제공을 위한 범위 내에서 회원 콘텐츠를 사용할 수 있습니다",
                            "회원은 타인의 저작권을 침해하는 콘텐츠를 업로드할 수 없습니다"
                        )
                    )
                ),
                onNavigateBack = {}
            )
        }
        composeTestRule.onNodeWithText("서비스 이용약관").assertIsDisplayed()
    }

    @Test
    fun legalDocumentScreenWithLongContent() {
        composeTestRule.captureScreenshot("legal_long_content.png") {
            LegalDocumentScreen(
                title = "개인정보 처리방침",
                version = "1.0",
                effectiveDate = "2026-02-14",
                sections = sampleSections + listOf(
                    LegalDocumentSection(
                        title = "개인정보의 제3자 제공",
                        bullets = listOf(
                            "회사는 원칙적으로 회원의 개인정보를 제3자에게 제공하지 않습니다",
                            "법령에 따라 요구되는 경우에만 제공할 수 있습니다"
                        )
                    ),
                    LegalDocumentSection(
                        title = "개인정보 보호책임자",
                        bullets = listOf(
                            "회사는 개인정보 처리에 관한 업무를 총괄하는 개인정보 보호책임자를 지정하고 있습니다",
                            "개인정보 관련 문의는 개인정보 보호책임자에게 연락하시기 바랍니다"
                        )
                    )
                ),
                onNavigateBack = {}
            )
        }
        composeTestRule.onNodeWithText("개인정보 처리방침").assertIsDisplayed()
    }
}
