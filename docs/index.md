# Flit.

**떠오른 순간, 바로 던지면 끝** — 멀티모달 캡처 앱

Flit.은 텍스트, 이미지, 음성 등 다양한 형태의 생각을 빠르게 캡처하고, AI가 자동으로 분류·정리해주는 Android 앱입니다.

---

## 📂 문서 구조

### direction/ — 제품 방향

- [PRD v10.0](direction/flit_prd_v10.md) — 비전, 요구사항, Phase 정의
- [철학 및 원칙](direction/philosophy_principles.md) — Just Capture, 설계 판단 기준, UX 라이팅 원칙

### specs/ — 명세서

- [기능명세서](specs/functional_spec.md) — 동작 규칙, 상태 전이, 엣지 케이스
- [데이터모델](specs/data_model_spec.md) — 스키마, 관계, 상태 머신
- [API 명세서](specs/api_spec.md) — 엔드포인트, 요청/응답, 에러 코드
- [디자인 가이드](specs/design-guide.md) — 색상, 타이포, 간격, 컴포넌트, UX 패턴
- [UX 라이팅 감사](specs/ux-writing-audit.md) — P0~P2 우선순위별 라이팅 개선
- [브랜드 아이덴티티](specs/brand-identity.md) — 로고, 앱 아이콘, 워드마크, 에셋
- [프레젠테이션 템플릿](specs/design-presentation-template.md) — 카드/그리드/배지 양식
- [Play Store 리스팅](specs/play-store-listing.md) — 스토어 설명, 에셋, 체크리스트

### plan/ — 개발 계획

- [UI 검토안](plan/ui-review-plan.md) — P0~P3 17건 + 코드 검증 3건
- [Phase 4 실시간 인식](plan/phase4/realtime_recognition_plan.md) — 로컬 파싱/AI 디바운스/하이브리드 비교

### insights/ — 인사이트

- [아키텍처 컨벤션](insights/architecture_conventions.md) — ViewModel→Repository 의존 기준, UseCase 규칙
- [버그 수정 회고](insights/2026-02-07_bugfix-retrospective.md) — 7가지 교훈
- [레거시 코드 교훈](insights/2026-02-07_legacy-audit-lessons.md) — 삭제 판단 기준 6가지
- [멀티 에이전트](insights/2026-02-07_multi-agent-insights.md) — 팀 구성, 태스크 의존성
- [Bash 권한 패턴](insights/2026-02-13_bash-permission-patterns.md) — Claude Code 권한 매칭
- [리브랜딩 교훈](insights/2026-02-13_rebrand-lessons.md) — KAIROS → Flit. 전환 경험
- [State Hoisting](insights/2026-02-13_state-hoisting-refactor.md) — Compose 상태 끌어올리기
- [Roborazzi 함정](insights/2026-02-14_roborazzi-robolectric-pitfalls.md) — 스크린샷 테스트 주의점

### analysis/ — 분석

- [UI 개선 보고서](analysis/ui-improvement-report.md) — P0/P1/P2 23개 항목
- [Material Design 점검](analysis/material-design-audit.md) — High 10건, Medium 10건
- [QA 회고](analysis/qa-retrospective.md) — 51건 이슈 분석, 반복 패턴 5가지

### performance/ — 성능

- [가이드](performance/README.md) — 5개 메트릭, 파이프라인
- [PR 리포트 템플릿](performance/pr_report_template.md)
- [성능 로그 템플릿](performance/performance_log_template.md)
- [성능 로그](performance/performance_log.md)
- [벤치마크 노트](performance/2026-02-12_benchmark_apm_workflow_notes.md)

### archive/ — 아카이브

- [Phase 2 리뷰 체크리스트](archive/plan/phase2/review_checklist_template.md)
- [Phase 2 테스트 계획](archive/plan/phase2/test_plan_viewmodel_worker_template.md)
- [Phase 2a 구현 보고서](archive/plan/phase2a/implementation_report.md)
- [Phase 2a QA 계획](archive/plan/phase2a/qa_plan.md)
- [Phase 2b 구현 계획](archive/plan/phase2b/implementation_plan.md)
- [Phase 2b 구현 보고서](archive/plan/phase2b/implementation_report.md)
- [Phase 3 서버 연동](archive/plan/phase3/server_integration_plan.md)
- [앱 철학 논의](archive/discussion/2026-02-05_app_philosophy.md)
- [프라이버시 논의](archive/discussion/2026-02-05_privacy_and_sync.md)

### 문서 관리

- [문서 인덱스](doc-index.md) — 전체 목록 + 상태 + Changelog
- [로드맵](ROADMAP.md) — 개발 로드맵 + Phase별 참조 문서 매핑
