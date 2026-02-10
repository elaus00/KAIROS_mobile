# KAIROS Development Roadmap

> 이 로드맵은 PRD(Docs/direction/kairos_prd_v10.md)의 Phase 정의를 기반으로 한다.
> Phase 범위나 우선순위 변경은 반드시 PRD에 먼저 반영한 후 이 문서를 업데이트한다.

## 참조 문서

| 문서 | 위치 | 용도 |
|------|------|------|
| PRD | `Docs/direction/kairos_prd_v10.md` | 비전, 요구사항, Phase 정의 (최상위) |
| 기능명세서 | `Docs/specs/functional_spec.md` | 기능별 동작 규칙, 상태 전이, 엣지 케이스 |
| 화면정의서 | `Docs/specs/screen-spec.md` | 화면 상태, 인터랙션, UI 스펙 |
| 데이터모델 명세서 | `Docs/specs/data_model_spec.md` | 스키마, 관계, 상태 머신 |
| API 명세서 | `Docs/specs/api_spec.md` | 엔드포인트, 요청/응답, 에러 코드 |

## Phase 1: MVP — 구현 완료, 자동화 검증 완료

**목표**: 제품의 핵심 가설을 검증할 수 있는 최소 단위

| 기능 | 범위 | 상태 |
|------|------|------|
| 캡처 | 텍스트 입력, 로컬 우선 저장, 완료 피드백, 임시 저장 | **완료** |
| AI 자동 분류 | 유형 판별, AI 제목, 태깅, 엔티티 추출, 사후 수정, Inbox 폴백 | **완료** |
| 홈 화면 | 캡처 전용 화면, 날짜 표시, 상단 바(🕐/🔔/⚙️), 전체 히스토리 | **완료** |
| 공유 인텐트 | 텍스트 수신, 미지원 형식 처리 | **완료** |
| 일정/할 일/노트 | 목록 조회, 삭제, 실행 취소 | **완료** |
| 온보딩 | 앱 소개, 첫 캡처 유도, AI 분류 안내 | **완료** |
| 설정 | 다크 모드, 개인정보 처리방침, 앱 버전 | **완료** |
| 빌드 상태 | `:app:compileDebugKotlin` 통과 (2026-02-07) | **완료** |
| 유닛 테스트 | `:app:testDebugUnitTest` 96개 전부 통과 (2026-02-07) | **완료** |

**명세 정합성 재점검 (2026-02-07):**
- PRD/기능명세서 대비 일부 항목은 구현 보강 필요 (AI 현황 시트 UI 세부, 삭제 플로우 일부, Notes 삭제 UX 등)
- 즉, 컴파일/유닛테스트는 완료 상태이나, 명세 100% 충족 기준으로는 추가 보완이 남아 있음

**개발 계획**: `Docs/plan/phase1/`
**Phase 2 공통 템플릿**: `Docs/plan/phase2/`

## Phase 2a: 코어 루프 완성 — 구현 완료

**목표**: 핵심 루프를 일상적으로 사용 가능한 수준으로 완성

| 기능 | 범위 | 상태 |
|------|------|------|
| DB v12 마이그레이션 | 5테이블 스키마 변경 + 2테이블 신규 | **완료** |
| 캡처 | 이미지 첨부 (갤러리/카메라) | **완료** |
| 휴지통 | 3단계 삭제 모델, 30일 보존, TrashScreen | **완료** |
| 할 일 관리 | 드래그 순서 변경, 완료 토글, AI 마감일 배지 | **완료** |
| 분류 로깅 | 분류 변경 이력 자동 기록 + 분석 이벤트 | **완료** |
| 일정 관리 | Google Calendar 동기화 (Mock), 제안 승인/거부 | **완료** |
| 알림 | 일정 제안/자동추가 로컬 푸시 알림 | **완료** |
| 설정 | 캘린더 동기화 설정, 온보딩 Google 연결 | **완료** |
| 위젯 | 4×1 입력 위젯 (Glance) | **완료** |
| 빌드 상태 | `:app:compileDebugKotlin` 통과 (2026-02-07) | **완료** |
| 유닛 테스트 | `:app:testDebugUnitTest` 96개 전부 통과 (2026-02-07) | **완료** |

**구현 규모**: 81개 파일 (36 신규, 44 수정, 1 삭제), 17 UseCase 추가, 3 Worker 추가

**개발 계획 및 보고서**: `Docs/plan/phase2a/`

## Phase 2b: 사용성 확장 (최소 출시 기준) — 구현 완료

**목표**: 멀티 인텐트 분할, 노트 편집, 필터링, 할일 위젯으로 최소 출시 기준 달성

| 기능 | 범위 | 상태 |
|------|------|------|
| DB 스키마 | `parent_capture_id`, `body` 추가 (DB v16) | **완료** |
| 멀티 인텐트 | AI 분류 시 다중 의도 → 개별 Capture 분할 | **완료** |
| 노트 편집 | NoteDetailScreen (제목/본문/태그/폴더 편집) | **완료** |
| 폴더 검증 | 이름 최대 30자 제한 | **완료** |
| 히스토리 필터 | 분류 유형 칩 + 날짜 범위 필터 | **완료** |
| 검색 필터 | FTS 검색 + 유형/날짜 필터 | **완료** |
| 할일 위젯 | 4×2 위젯, 오늘 마감 할일 + 체크박스 | **완료** |
| 관측 지표 | 4개 신규 이벤트 (revisited, completed, search, split) | **완료** |
| 유닛 테스트 | 112개 통과 + 추가 테스트 작성 중 | **진행 중** |
| 빌드 상태 | `:app:compileDebugKotlin` 통과 (2026-02-09) | **완료** |

**개발 계획 및 보고서**: `Docs/plan/phase2b/`

## Phase 3: 서버 연동 (Mock → 실제 API)

**목표**: MockKairosApi 제거, 실제 FastAPI 서버와 연동, 통합 테스트

| 기능 | 범위 | 서브페이즈 |
|------|------|-----------|
| 서버 배포 | FastAPI 스테이징 배포 + Health Check | 3-0 |
| 네트워크 정비 | 에러 핸들링, 인터셉터, ApiResult 모델 | 3-1 |
| AI 분류 연동 | `/classify` 실서버 전환 | 3-2 |
| Split 연동 | `/classify` split_items 실서버 검증 | 3-3 |
| 캘린더 연동 | `/calendar/events` Google OAuth 프록시 | 3-4 |
| 분석 연동 | `/analytics/events` 배치 전송 | 3-5 |
| Mock 제거 | MockKairosApi 삭제, 빌드 설정 정리 | 3-6 |
| 통합 테스트 | E2E 시나리오 검증 + FakeKairosApi 테스트 교체 | 3-7 |

**예상 규모**: ~23개 파일 (7 신규, 15 수정, 1 삭제)

**개발 계획**: `Docs/plan/phase3/server_integration_plan.md`

## Phase 3a: 서비스 지속성 — 구현 완료

**목표**: 서비스 지속 가능성 확보. 인증/과금 도입, AI 개인화

| 기능 | 범위 | 상태 |
|------|------|------|
| 사용자 인증 | Google OAuth 기반 인증 (JWT Bearer) | **완료** |
| 구독 모델 | 과금 기준 적용 (Google Play Billing) | **완료** |
| 일정 관리 | 양방향 Google Calendar 동기화 + 충돌 해결 | **완료** |
| 노트 | AI 자동 그룹화(구독), 재정리(구독), 시맨틱 검색(구독) | **완료** |
| AI 분류 | 분류 프리셋(구독), 사용자 지시(구독), 수정 이력 학습 | **완료** |

## Phase 3b: 외부 확장 — 구현 완료

**목표**: 외부 서비스 연동, 프리미엄 기능 확장

| 기능 | 범위 | 상태 |
|------|------|------|
| 공유 | 캡처/노트 외부 공유 | **완료** |
| OCR | 이미지 텍스트 추출 (Base64 JSON) | **완료** |
| 분석 대시보드 | 캡처 통계 시각화 | **완료** |

## API 계약 정렬 — 완료 (2026-02-10)

**목표**: 모바일 클라이언트 DTO/Repository를 서버 스펙에 맞춰 수정

| 항목 | 문제 | 수정 내용 |
|------|------|-----------|
| Subscription | @SerializedName 3개 불일치 | `auto_grouping`/`inbox_auto_classify`/`reorganize`로 수정, `modification_learning`/`analytics_dashboard`/`ocr` 추가 |
| Notes AI | 요청/응답 구조 완전 불일치 (3개 엔드포인트) | NoteAiInput/InboxClassifyResult/ProposedStructure 도메인 모델 도입, DTO/Repository/Worker/UseCase 전면 재작성 |
| OCR | 경로 + 프로토콜 불일치 | `/ocr` → `/ocr/extract`, Multipart → JSON+Base64 |
| Calendar | `expires_in` vs `expires_at` | ISO 8601 datetime 형식으로 전환 |
| Auth | refresh user NPE, 누락 필드 | `user` nullable 처리, `google_calendar_connected` 추가 |

**검증**: 컴파일 통과 + 전체 유닛 테스트 통과 + 서버 91개 테스트 통과
