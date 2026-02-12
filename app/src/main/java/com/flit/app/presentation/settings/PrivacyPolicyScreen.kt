package com.flit.app.presentation.settings

import androidx.compose.runtime.Composable

@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit = {}
) {
    LegalDocumentScreen(
        title = "개인정보 처리방침",
        draftVersion = "0.1",
        updatedAt = "2026-02-12",
        sections = privacyPolicySections,
        onNavigateBack = onNavigateBack
    )
}

private val privacyPolicySections = listOf(
    LegalDocumentSection(
        title = "수집하는 정보",
        bullets = listOf(
            "서비스 이용 시 캡처 원문, AI 제목, 분류 결과, 태그, 엔티티, 노트/할 일/일정 파생 데이터가 기기 내 로컬 DB에 저장됩니다.",
            "Google 로그인 시 서버 인증을 위해 ID 토큰이 전송되며, 앱에는 사용자 식별 정보(사용자 ID, 이메일), 접근 토큰/갱신 토큰이 암호화 저장됩니다.",
            "서버 통신 시 요청 식별을 위해 디바이스 ID가 함께 전송됩니다.",
            "사용 분석 기능을 위해 이벤트 유형, 이벤트 메타데이터, 시각 정보가 수집될 수 있습니다."
        )
    ),
    LegalDocumentSection(
        title = "정보 수집 및 처리 방식",
        bullets = listOf(
            "캡처는 네트워크와 무관하게 먼저 기기에 저장되며, AI 분류가 필요한 경우에만 분류 요청이 서버로 전송됩니다.",
            "로그인 사용자는 초기/주기 동기화 시 캡처, 노트, 할 일, 일정, 폴더, 태그 데이터가 서버와 동기화될 수 있습니다.",
            "분석 이벤트는 기기에 임시 저장된 후 배치 전송됩니다."
        )
    ),
    LegalDocumentSection(
        title = "이용 목적",
        bullets = listOf(
            "AI 자동 분류, 제목 생성, 태깅, 일정/할 일 파생 생성 등 핵심 기능 제공",
            "사용자 인증, 구독 상태 확인, 기기간 데이터 동기화",
            "서비스 품질 측정 및 기능 개선을 위한 통계 분석"
        )
    ),
    LegalDocumentSection(
        title = "권한 사용 안내",
        bullets = listOf(
            "캘린더(읽기/쓰기): 일정을 기기 캘린더와 연동할 때 사용합니다.",
            "알림: 일정/동기화 관련 알림 제공에 사용합니다.",
            "카메라/사진: 이미지 캡처 및 첨부 기능 사용 시에만 접근합니다."
        )
    ),
    LegalDocumentSection(
        title = "보관 및 삭제",
        bullets = listOf(
            "사용자 콘텐츠는 기본적으로 로컬에 보관되며, 사용자가 삭제할 수 있습니다.",
            "삭제된 항목은 실행 취소 구간 이후 휴지통으로 이동하며 30일 보관 후 자동 영구 삭제됩니다.",
            "동기화 완료된 분석 이벤트는 기기에서 최대 7일 이후 정리됩니다.",
            "로그아웃 시 인증 토큰은 삭제되며, 로컬 콘텐츠 데이터는 유지됩니다."
        )
    ),
    LegalDocumentSection(
        title = "제3자 제공 및 처리 위탁",
        bullets = listOf(
            "법령상 요구 또는 이용자 동의가 있는 경우를 제외하고, 개인정보를 판매하지 않습니다.",
            "서비스 제공을 위해 인증, AI 처리, 동기화, 분석 전송이 서버 인프라를 통해 처리될 수 있습니다."
        )
    ),
    LegalDocumentSection(
        title = "이용자 권리",
        bullets = listOf(
            "이용자는 앱 내에서 콘텐츠 삭제, 캘린더 연동 해제, 로그아웃을 수행할 수 있습니다.",
            "문의나 삭제 요청 등 개인정보 관련 요청은 elaus.dev@gmail.com 으로 접수할 수 있습니다."
        )
    ),
    LegalDocumentSection(
        title = "문의처",
        bullets = listOf(
            "개인정보 관련 문의: elaus.dev@gmail.com"
        )
    )
)
