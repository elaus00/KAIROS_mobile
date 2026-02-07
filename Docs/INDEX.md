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
├── specs/                   # 명세서 (기능/화면/데이터모델/API)
├── plan/                    # Phase별 개발 계획 문서
│   ├── phase1/
│   ├── phase2a/
│   ├── phase2b/
│   └── phase3/
├── insights/                # 개발 인사이트 기록
├── discussion/              # 논의 기록
└── analysis/                # 분석 기록
```

## 문서 목록

### direction/ (최상위 문서)

| 파일 | 설명 | 버전 | 최종 수정 |
|------|------|------|-----------|
| `kairos_prd_v10.md` | 제품 기획안 (PRD) — 비전, 요구사항, Phase 정의 | v10.0 | 2026-02-06 |

### specs/

| 파일 | 설명 | 버전 | 기준 문서 | 최종 수정 |
|------|------|------|-----------|-----------|
| `functional_spec.md` | 기능명세서 — 동작 규칙, 상태 전이, 엣지 케이스 | v2.1 | PRD v10.0, 데이터모델 v2.0 | 2026-02-06 |
| `data_model_spec.md` | 데이터모델 명세서 — 스키마, 관계, 상태 머신 | v2.0 | PRD v10.0, 기능명세서 v2.0 | 2026-02-06 |
| — | 화면정의서 (`screen_spec.md`) | 작성 예정 | — | — |
| — | API 명세서 (`api_spec.md`) | 작성 예정 | — | — |

### plan/

| 디렉토리 | 설명 | 상태 |
|----------|------|------|
| `phase1/` | Phase 1 (MVP) 개발 계획 | **구현 완료, 통합 테스트 미완** |
| `phase2a/` | Phase 2a (코어 루프 완성) 개발 계획 | 대기 |
| `phase2b/` | Phase 2b (사용성 확장) 개발 계획 | 대기 |
| `phase3/` | Phase 3 (고도화) 개발 계획 | 대기 |

### discussion/

| 파일 | 설명 | 날짜 |
|------|------|------|
| `2026-02-05_app_philosophy.md` | 앱 철학 (Guilt-Free Capture) 논의 | 2026-02-05 |
| `2026-02-05_privacy_and_sync.md` | 프라이버시 및 동기화 논의 | 2026-02-05 |

### insights/

| 파일 | 설명 | 날짜 |
|------|------|------|
| `2026-02-07_bugfix-retrospective.md` | 버그 수정 회고 | 2026-02-07 |
| `2026-02-07_multi-agent-insights.md` | 멀티 에이전트 개발 인사이트 (Phase 1 MVP) | 2026-02-07 |

### analysis/

| 파일 | 설명 | 날짜 |
|------|------|------|
| `2026-02-06_context-management-methodology.md` | 컨텍스트 관리 방법론 분석 | 2026-02-06 |

---

## Changelog

| 일시 | 문서 | 변경 내용 |
|------|------|-----------|
| 2026-02-06 19:58 | `INDEX.md` | Changelog 포맷 변경 — 분 단위 시간 기록 |
| 2026-02-06 19:55 | `INDEX.md` | 문서 관리 정책 추가 (PRD 수정 금지, 변경이력 기록 의무) |
| 2026-02-06 19:50 | `kairos_prd_v10.md` | 사용자 업로드 (v9.2 → v10.0) |
| 2026-02-06 19:50 | `functional_spec.md` | 사용자 업로드 (v2.1) |
| 2026-02-06 19:50 | `data_model_spec.md` | 사용자 업로드 (v2.0) |
| 2026-02-06 19:45 | `ROADMAP.md` | 초기 생성 — Phase 1~3 로드맵 정의 |
| 2026-02-06 19:45 | `INDEX.md` | 초기 생성 — 문서 인덱스 구조 수립 |
| 2026-02-07 | `insights/2026-02-07_multi-agent-insights.md` | 멀티 에이전트 개발 인사이트 작성 |
| 2026-02-07 | `plan/phase1/progress_report.md` | Phase 1 진행 보고서 작성 |
| 2026-02-07 | `plan/phase1/implementation_plan.md` | 서브페이즈별 진행 상태 테이블 추가 |
| 2026-02-06 | `plan/phase1/implementation_plan.md` | Phase 1 구현 계획서 작성 — 13개 서브페이즈 |
