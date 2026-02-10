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
│   ├── phase2/
│   ├── phase2a/
│   ├── phase2b/
│   └── phase3/
├── analysis/                # 분석 기록
├── insights/                # 개발 인사이트 기록
├── discussion/              # 논의 기록
└── performance/             # 성능 기준/리포트 템플릿
```

## 문서 목록

### direction/ (최상위 문서)

| 파일 | 설명 | 버전 | 최종 수정 |
|------|------|------|-----------|
| `kairos_prd_v10.md` | 제품 기획안 (PRD) — 비전, 요구사항, Phase 정의 | v10.0 | 2026-02-06 |

### specs/

| 파일 | 설명 | 버전 | 기준 문서 | 최종 수정 |
|------|------|------|-----------|-----------|
| `functional_spec.md` | 기능명세서 — 동작 규칙, 상태 전이, 엣지 케이스 | v2.3 | PRD v10.0, 데이터모델 v2.0 | 2026-02-07 |
| `data_model_spec.md` | 데이터모델 명세서 — 스키마, 관계, 상태 머신 | v2.0 | PRD v10.0, 기능명세서 v2.0 | 2026-02-06 |
| — | 화면정의서 (`screen_spec.md`) | 작성 예정 | — | — |
| `api_spec.md` | API 명세서 — 엔드포인트/요청/응답/에러 코드 | v2.5 | PRD v10.0, 기능명세서 v2.3, 데이터모델 v2.0 | 2026-02-10 |
| `design-guide.md` | 디자인 가이드 — 색상/타이포/간격/컴포넌트/금지사항 | v1.0 | PRD v10.0 | 2026-02-08 |
| `ux-writing-audit.md` | UX 라이팅 감사 — 가이드라인/텍스트 인벤토리/개선 제안(P0 8건, P1 16건, P2 17건) | v1.0 | PRD v10.0, design-guide v1.0 | 2026-02-08 |

### plan/

| 디렉토리 | 설명 | 상태 |
|----------|------|------|
| `phase1/` | Phase 1 (MVP) 계획 문서 보관 디렉토리 | **정리 완료 (문서 비움)** |
| `phase2/` | Phase 2 공통 참고/템플릿 문서 | 진행 중 |
| `phase2a/` | Phase 2a (코어 루프 완성) 개발 계획 | **완료** |
| `phase2b/` | Phase 2b (사용성 확장) 개발 계획 + 구현 보고서 | **구현 완료 (테스트 보강 중)** |
| `phase3/` | Phase 3 (서버 연동) 개발 계획 | **구현 완료 (Phase 3a/3b/API 정렬 포함)** |

#### Phase 1 상세 문서

| 파일 | 설명 | 상태 |
|------|------|------|
| `phase1/.gitkeep` | 빈 디렉토리 유지용 파일 | 유지 |

#### Phase 2a 문서

| 파일 | 설명 | 상태 |
|------|------|------|
| `phase2a/implementation_report.md` | Phase 2a 구현 완료 보고서 — 10개 서브페이즈, 81개 파일 | 완료 |
| `phase2a/qa_plan.md` | Phase 1+2a QA 계획서 — 6영역(테스트갭/핵심플로우/Worker/화면별/엣지케이스/아키텍처), 37건 | 미착수 |

#### Phase 2b 문서

| 파일 | 설명 | 상태 |
|------|------|------|
| `phase2b/implementation_plan.md` | Phase 2b 구현 계획서 — 9개 서브페이즈, ~52개 파일 | 계획 수립 |
| `phase2b/implementation_report.md` | Phase 2b 구현 완료 보고서 — 전체 서브페이즈 구현 완료, 테스트 보강 중 | 구현 완료 |

#### Phase 3 문서

| 파일 | 설명 | 상태 |
|------|------|------|
| `phase3/server_integration_plan.md` | Phase 3 서버 연동 계획서 — 8개 서브페이즈 중 3-0/3-1/3-6 완료 | 구현 진행 중 |

#### Phase 2 공통 문서

| 파일 | 설명 | 상태 |
|------|------|------|
| `phase2/test_plan_viewmodel_worker_template.md` | ViewModel/Worker 테스트 계획 템플릿 | 재사용 예정 |
| `phase2/review_checklist_template.md` | 코드 리뷰 체크리스트 템플릿 | 재사용 예정 |

### discussion/

| 파일 | 설명 | 날짜 |
|------|------|------|
| `2026-02-05_app_philosophy.md` | 앱 철학 (Guilt-Free Capture) 논의 | 2026-02-05 |
| `2026-02-05_privacy_and_sync.md` | 프라이버시 및 동기화 논의 | 2026-02-05 |

### insights/

| 파일 | 설명 | 날짜 |
|------|------|------|
| `architecture_conventions.md` | 아키텍처 컨벤션 정리 | 2026-02-07 |
| `2026-02-07_bugfix-retrospective.md` | 버그 수정 회고 | 2026-02-07 |
| `2026-02-07_multi-agent-insights.md` | 멀티 에이전트 개발 인사이트 (Phase 1 MVP) | 2026-02-07 |

### analysis/

| 파일 | 설명 | 날짜 |
|------|------|------|
| `ui-improvement-report.md` | UI 디자이너 관점 개선 보고서 — 시각적 일관성, 접근성, 인터랙션 등 23개 항목 | 2026-02-08 |

### performance/

| 파일 | 설명 | 날짜 |
|------|------|------|
| `README.md` | 성능 문서 사용 가이드 | 2026-02-07 |
| `perf_gate_baseline.json` | 성능 게이트 기준값 | 2026-02-07 |
| `pr_report_template.md` | PR 성능 리포트 템플릿 | 2026-02-07 |

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
| 2026-02-07 | `plan/phase1/test_plan_viewmodel_worker.md` | ViewModel + Worker 테스트 계획서 작성 (79개 케이스) |
| 2026-02-06 | `plan/phase1/implementation_plan.md` | Phase 1 구현 계획서 작성 — 13개 서브페이즈 |
| 2026-02-07 | `plan/phase1/*` | Phase 1 문서 정리: 계획/리포트/리뷰 문서 삭제, 템플릿 2건을 `plan/phase2/`로 이동 |
| 2026-02-07 | `ROADMAP.md` | Phase 1 검증 상태 업데이트: 유닛 테스트 96/96 통과 반영 |
| 2026-02-07 | `INDEX.md` | 실파일 기준으로 문서 인덱스 정합성 보정 (specs/api, insights, performance 반영) |
| 2026-02-07 | `ROADMAP.md` | 참조 문서 경로(`functional_spec`, `data_model_spec`, `api_spec`) 및 Phase 1 홈 화면 범위 설명을 PRD v10 기준으로 보정 |
| 2026-02-07 | `ROADMAP.md` | PRD/기능명세서 재점검 결과 반영: Phase 1은 자동화 완료이나 명세 정합성 보강 필요 상태 명시 |
| 2026-02-07 | `plan/phase2a/implementation_report.md` | Phase 2a 구현 완료 보고서 작성 — 10개 서브페이즈, 81개 파일 |
| 2026-02-07 | `ROADMAP.md` | Phase 2a 상태를 "구현 완료"로 업데이트 (컴파일 + 96개 테스트 통과) |
| 2026-02-07 | `INDEX.md` | Phase 2a 문서 섹션 추가, 상태 "완료"로 변경 |
| 2026-02-07 | `plan/phase2b/implementation_plan.md` | Phase 2b 구현 계획서 작성 — 9개 서브페이즈, ~52개 파일 |
| 2026-02-07 | `plan/phase3/server_integration_plan.md` | Phase 3 서버 연동 계획서 작성 — Mock 삭제 → 실제 API, 8개 서브페이즈 |
| 2026-02-07 | `INDEX.md` | Phase 2b/3 문서 섹션 추가, 상태 "계획 수립"으로 변경 |
| 2026-02-07 | `ROADMAP.md` | Phase 2b/3 상세 내용 업데이트 |
| 2026-02-07 | `functional_spec.md` | v2.1 → v2.3: PRD 변경사항 반영 (BOOKMARK 장기 로드맵, 임시 저장 EncryptedSharedPreferences 제거, 수정 이력 기반 학습 Phase 2b~3b, 분류 프리셋/사용자 지시 Phase 3a, AI 통합 그룹화, Phase 로드맵 업데이트) |
| 2026-02-08 | `analysis/ui-improvement-report.md` | UI 디자이너 관점 개선 보고서 작성 — P0 2건, P1 15건, P2 7건 |
| 2026-02-08 | `specs/design-guide.md` | 디자인 가이드 v1.0 작성 — 색상/타이포/간격/컴포넌트/금지사항 체크리스트 |
| 2026-02-08 | `specs/ux-writing-audit.md` | UX 라이팅 감사 v1.0 작성 — 가이드라인, 19개 화면 ~170항목 텍스트 인벤토리, 개선 제안 P0/P1/P2 |
| 2026-02-09 | `plan/phase2a/qa_plan.md` | Phase 1+2a QA 계획서 v1.0 작성 — 6영역 37건, 테스트 갭 7건 + 핵심 플로우 6건 + Worker 5건 + 화면별 11건 + 엣지 5건 + 아키텍처 3건 |
| 2026-02-09 | `plan/phase2b/implementation_report.md` | Phase 2b 구현 완료 보고서 작성 — 계획서 대비 전체 서브페이즈(2b-0~2b-7) 구현 완료 확인, 테스트 갭 4건 식별 |
| 2026-02-09 | `ROADMAP.md` | Phase 2b 상태를 "구현 완료"로 업데이트 |
| 2026-02-09 | `INDEX.md` | Phase 2b 보고서 문서 추가, 상태 변경 |
| 2026-02-09 | `plan/phase3/server_integration_plan.md` | v1.0→v2.0: 3-0/3-1/3-6 완료 반영, 에러 레이어(ApiException/ApiResponseHandler/ErrorInterceptor) 구현 완료 기록 |
| 2026-02-09 | `INDEX.md` | Phase 3 상태를 "구현 진행 중"으로 업데이트 |
| 2026-02-10 | `specs/api_spec.md` | v2.4→v2.5: API 계약 정렬 — 구독 features 추가(analytics_dashboard/ocr), OCR 엔드포인트(/ocr/extract), Auth refresh user nullable, 시맨틱검색/대시보드 엔드포인트 추가, 요약 테이블 갱신 |
| 2026-02-10 | `ROADMAP.md` | Phase 3a/3b 구현 완료 + API 계약 정렬 완료 섹션 추가 |
| 2026-02-10 | `INDEX.md` | Phase 3 상태 "구현 완료"로 업데이트, api_spec 버전 갱신 |
