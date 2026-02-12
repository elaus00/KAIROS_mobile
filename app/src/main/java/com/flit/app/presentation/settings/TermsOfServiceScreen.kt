package com.flit.app.presentation.settings

import androidx.compose.runtime.Composable

@Composable
fun TermsOfServiceScreen(
    onNavigateBack: () -> Unit = {}
) {
    LegalDocumentScreen(
        title = "이용약관",
        draftVersion = "0.1",
        updatedAt = "2026-02-12",
        sections = termsOfServiceSections,
        onNavigateBack = onNavigateBack
    )
}

private val termsOfServiceSections = listOf(
    LegalDocumentSection(
        title = "목적",
        bullets = listOf(
            "본 약관은 Flit 앱(이하 \"서비스\")의 이용 조건과 운영 기준을 정하는 것을 목적으로 합니다.",
            "본 문서는 정식 공표 전 운영 초안이며, 정식 버전 배포 시 갱신될 수 있습니다."
        )
    ),
    LegalDocumentSection(
        title = "서비스 제공 내용",
        bullets = listOf(
            "서비스는 캡처 입력, AI 자동 분류, 일정/할 일/노트 관리, 검색, 캘린더 연동, 설정 기능을 제공합니다.",
            "일부 기능은 네트워크 연결 또는 로그인 상태가 필요하며, 무료/구독 플랜에 따라 제공 범위가 달라질 수 있습니다.",
            "서비스는 제품 개선을 위해 기능, UI, 정책이 변경될 수 있습니다."
        )
    ),
    LegalDocumentSection(
        title = "계정 및 인증",
        bullets = listOf(
            "로그인 기능은 Google Sign-In 기반 인증을 사용하며, 사용자 인증 정보는 암호화 저장소에 보관됩니다.",
            "이용자는 자신의 계정 정보를 안전하게 관리해야 하며, 비정상 접근이 의심되는 경우 즉시 조치해야 합니다."
        )
    ),
    LegalDocumentSection(
        title = "이용자의 책임",
        bullets = listOf(
            "이용자는 관계 법령과 본 약관을 준수하여 서비스를 이용해야 합니다.",
            "타인의 권리를 침해하거나 불법/유해한 콘텐츠를 저장, 전송, 공유하는 행위를 금지합니다.",
            "이용자가 등록한 콘텐츠에 대한 권리와 책임은 이용자에게 있습니다."
        )
    ),
    LegalDocumentSection(
        title = "서비스 제한 및 중단",
        bullets = listOf(
            "점검, 장애, 정책 변경, 외부 연동 이슈 등으로 서비스 일부가 제한되거나 중단될 수 있습니다.",
            "AI 분류/동기화 등 네트워크 의존 기능은 통신 환경 또는 서버 상태에 따라 지연될 수 있습니다."
        )
    ),
    LegalDocumentSection(
        title = "데이터 처리 및 보관",
        bullets = listOf(
            "기록 데이터는 로컬 우선 저장을 기본으로 하며, 기능 제공에 필요한 범위에서 서버 처리/동기화가 이뤄질 수 있습니다.",
            "삭제된 콘텐츠의 처리 방식(실행 취소, 휴지통, 영구 삭제)은 앱 정책에 따릅니다.",
            "개인정보 처리에 대한 상세 사항은 개인정보 처리방침을 따릅니다."
        )
    ),
    LegalDocumentSection(
        title = "면책",
        bullets = listOf(
            "회사는 천재지변, 불가항력, 이용자 귀책, 제3자 서비스 장애로 인한 손해에 대해 책임이 제한될 수 있습니다.",
            "이용자가 서비스에 저장한 정보의 백업 및 관리 책임은 이용자에게 있습니다."
        )
    ),
    LegalDocumentSection(
        title = "약관 변경",
        bullets = listOf(
            "회사는 관련 법령 및 서비스 정책 변경에 따라 약관을 수정할 수 있습니다.",
            "중요 변경은 앱 내 고지 후 적용하며, 계속 이용 시 변경 약관에 동의한 것으로 봅니다."
        )
    ),
    LegalDocumentSection(
        title = "문의처",
        bullets = listOf(
            "서비스 이용 문의: elaus.dev@gmail.com",
            "개인정보 관련 문의: elaus.dev@gmail.com"
        )
    )
)
