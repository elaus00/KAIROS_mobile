# Docs 문서 인덱스

> 이 파일은 Docs/ 디렉토리의 전체 문서 목록과 상태를 관리한다.
> 문서를 추가/편집/삭제할 때 반드시 이 인덱스를 함께 업데이트해야 한다.

## 문서 관리 정책

1. **PRD 수정 금지**: `Docs/direction/`의 PRD 문서는 사용자의 명시적 요청이 없는 한, AI가 임의로 수정해서는 안 된다
2. **변경이력 기록 의무**: 모든 문서의 변경이력은 이 파일 하단의 Changelog에 기록해야 한다

## 디렉토리 구조

```
Docs/
├── INDEX.md                 # 이 파일 (문서 인덱스)
├── ROADMAP.md               # 개발 로드맵 + Phase별 참조 문서 매핑
├── direction/               # 사용자 직접 작성 문서 (PRD = 최상위)
├── specs/                   # 명세서 (기능/데이터모델/API/디자인)
├── plan/                    # Phase별 개발 계획 문서
│   └── phase4/              # Phase 4 (실시간 인식 피드백) 연구
├── insights/                # 개발 인사이트 기록
├── analysis/                # 분석 기록
├── design/                  # 디자인 목업
├── performance/             # 성능 기준/리포트 템플릿
├── image/                   # 참조 이미지
└── archive/                 # 완료된 Phase 문서 아카이브
    ├── plan/phase2~3/       # 구현 완료된 계획서/보고서
    └── discussion/          # 과거 논의 기록
```

## 문서 목록

### direction/ (최상위 문서)

| 파일 | 설명 | 버전 | 최종 수정 |
|------|------|------|-----------|
| `kairos_prd_v10.md` | 제품 기획안 (PRD) — 비전, 요구사항, Phase 정의 | v10.0 | 2026-02-06 |
| `philosophy_principles.md` | 철학 및 원칙 가이드 — Just Capture, 설계 판단 기준, UX 라이팅 원칙 | v2.0 | 2026-02-06 |

### specs/

| 파일 | 설명 | 버전 | 기준 문서 | 최종 수정 |
|------|------|------|-----------|-----------|
| `functional_spec.md` | 기능명세서 — 동작 규칙, 상태 전이, 엣지 케이스 | v2.3 | PRD v10.0, 데이터모델 v2.0 | 2026-02-07 |
| `data_model_spec.md` | 데이터모델 명세서 — 스키마, 관계, 상태 머신 | v2.2 | PRD v10.0, 기능명세서 v2.3 | 2026-02-12 |
| `api_spec.md` | API 명세서 — 엔드포인트/요청/응답/에러 코드 | v2.5 | PRD v10.0, 기능명세서 v2.3, 데이터모델 v2.0 | 2026-02-10 |
| `design-guide.md` | 디자인 가이드 — 색상/타이포/간격/컴포넌트/금지사항 | v1.1 | PRD v10.0 | 2026-02-12 |
| `ux-writing-audit.md` | UX 라이팅 감사 — P0 8건 반영 완료, P1 16건, P2 17건 | v1.1 | PRD v10.0, design-guide v1.1 | 2026-02-12 |

### plan/

| 디렉토리 | 설명 | 상태 |
|----------|------|------|
| `phase4/` | Phase 4 (실시간 인식 피드백) 연구 문서 | 연구 단계 |

#### Phase 4 문서

| 파일 | 설명 | 상태 |
|------|------|------|
| `phase4/realtime_recognition_plan.md` | 실시간 인식 피드백 기능 연구 — 로컬 파싱/AI 디바운스/하이브리드 3안 비교, 권장안 C(하이브리드) | 연구 단계 |

### insights/

| 파일 | 설명 | 날짜 |
|------|------|------|
| `architecture_conventions.md` | 아키텍처 컨벤션 — ViewModel→Repository 의존 기준, UseCase 규칙 | 2026-02-07 |
| `2026-02-07_bugfix-retrospective.md` | 버그 수정 회고 — 7가지 교훈 | 2026-02-07 |
| `2026-02-07_legacy-audit-lessons.md` | 레거시 코드 정리 교훈 — 삭제 판단 기준 6가지 | 2026-02-07 |
| `2026-02-07_multi-agent-insights.md` | 멀티 에이전트 개발 인사이트 — 팀 구성, 태스크 의존성 | 2026-02-07 |
| `2026-02-11_branding-marketing-report.md` | 브랜딩 & 마케팅 전략 보고서 — 네이밍 22개 분석, GTM 전략 | 2026-02-11 |

### analysis/

| 파일 | 설명 | 날짜 |
|------|------|------|
| `ui-improvement-report.md` | UI 디자이너 관점 개선 보고서 — P0/P1/P2 23개 항목 | 2026-02-08 |
| `material-design-audit.md` | Material Design 커스텀 현황 점검 — High 10건, Medium 10건 | 2026-02-08 |

### design/

| 파일 | 설명 | 날짜 |
|------|------|------|
| `kairos-redesign-mockup.html` | 인터랙티브 HTML 목업 — 기록/설정 화면 리디자인 | 2026-02-08 |

### performance/

| 파일 | 설명 | 날짜 |
|------|------|------|
| `README.md` | 성능 문서 사용 가이드 — 5개 메트릭, 파이프라인 | 2026-02-07 |
| `perf_gate_baseline.json` | 성능 게이트 기준값 | 2026-02-07 |
| `pr_report_template.md` | PR 성능 리포트 템플릿 | 2026-02-07 |

### image/

| 파일 | 설명 |
|------|------|
| `notion_widget.jpeg` | 참조 이미지 (위젯 디자인 레퍼런스) |

### archive/ (아카이브 — 완료된 Phase 문서)

> 구현 완료된 Phase의 계획서/보고서 및 과거 논의 기록. 의사결정 기록으로 보존.

| 파일 | 설명 | 원래 위치 |
|------|------|-----------|
| `plan/phase2/review_checklist_template.md` | 코드 리뷰 체크리스트 템플릿 | plan/phase2/ |
| `plan/phase2/test_plan_viewmodel_worker_template.md` | ViewModel/Worker 테스트 계획 템플릿 | plan/phase2/ |
| `plan/phase2a/implementation_report.md` | Phase 2a 구현 완료 보고서 | plan/phase2a/ |
| `plan/phase2a/qa_plan.md` | Phase 1+2a QA 계획서 | plan/phase2a/ |
| `plan/phase2b/implementation_plan.md` | Phase 2b 구현 계획서 | plan/phase2b/ |
| `plan/phase2b/implementation_report.md` | Phase 2b 구현 완료 보고서 | plan/phase2b/ |
| `plan/phase3/server_integration_plan.md` | Phase 3 서버 연동 계획서 | plan/phase3/ |
| `discussion/2026-02-05_app_philosophy.md` | 앱 철학 논의 기록 | discussion/ |
| `discussion/2026-02-05_privacy_and_sync.md` | 프라이버시 및 동기화 논의 | discussion/ |

---

## Changelog

| 일시 | 문서 | 변경 내용 |
|------|------|-----------|
| 2026-02-12 | `INDEX.md` | 문서 정리: 아카이브 9건, 삭제 3건, 누락 6건 등록, 디렉토리 구조 갱신 |
| 2026-02-12 | `ROADMAP.md` | screen-spec 경로 수정, Phase 2b 테스트 완료 반영, 아카이브 경로 갱신 |
| 2026-02-12 | `design-guide.md` | v1.0→v1.1: textMuted/placeholder WCAG AA 색상값 갱신 (Light #737373, Dark #7A7A7A) |
| 2026-02-12 | `ux-writing-audit.md` | v1.0→v1.1: P0 8건 전체 반영 완료 마킹 |
| 2026-02-12 | `philosophy_principles.md` | Snackbar 5초→3초 수정 (PRD v10.0 반영) |
| 2026-02-11 | `insights/2026-02-11_branding-marketing-report.md` | 브랜딩 & 마케팅 전략 보고서 v1.0 작성 |
| 2026-02-11 | `plan/phase4/realtime_recognition_plan.md` | 실시간 인식 피드백 기능 연구 문서 v1.0 작성 |
| 2026-02-10 | `specs/api_spec.md` | v2.4→v2.5: API 계약 정렬 |
| 2026-02-10 | `ROADMAP.md` | Phase 3a/3b 구현 완료 + API 계약 정렬 완료 섹션 추가 |
| 2026-02-09 | `plan/phase2b/implementation_report.md` | Phase 2b 구현 완료 보고서 작성 |
| 2026-02-09 | `plan/phase3/server_integration_plan.md` | v1.0→v2.0: 3-0/3-1/3-6 완료 반영 |
| 2026-02-08 | `specs/design-guide.md` | 디자인 가이드 v1.0 작성 |
| 2026-02-08 | `specs/ux-writing-audit.md` | UX 라이팅 감사 v1.0 작성 |
| 2026-02-08 | `analysis/ui-improvement-report.md` | UI 개선 보고서 작성 |
| 2026-02-07 | `plan/phase2a/implementation_report.md` | Phase 2a 구현 완료 보고서 작성 |
| 2026-02-07 | `ROADMAP.md` | Phase 2a 상태를 "구현 완료"로 업데이트 |
| 2026-02-07 | `functional_spec.md` | v2.1→v2.3: PRD 변경사항 반영 |
| 2026-02-06 | `kairos_prd_v10.md` | 사용자 업로드 (v9.2→v10.0) |
| 2026-02-06 | `INDEX.md` | 초기 생성 |
| 2026-02-06 | `ROADMAP.md` | 초기 생성 |
