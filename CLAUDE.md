# Flit. Mobile

멀티모달 캡처 앱. Clean Architecture + MVVM + Jetpack Compose.
핵심 철학: Just Capture ("떠오른 순간, 바로 던지면 끝")

## 설계 판단 기준

기능 추가/UI 변경 시 하나라도 Yes면 재고:
- "캡처까지의 단계를 늘리는가?"
- "사용자에게 선택을 강요하는가?"
- "정리 부담을 전가하는가?"
- "핵심 루프(기록 → 정리 → 실행)를 방해하는가?"
- "AI 분류 품질을 타협하는가?"

## 패키지 식별자

- **applicationId** (Play Store 식별자): `app.flit.mobile`
- **namespace** (코드 패키지명): `com.flit.app`
- 두 값은 독립적임. 소스 코드의 import/패키지는 `com.flit.app` 유지

## Architecture

```
presentation/ → domain/ → data/ (단방향 의존)
```
- Domain Layer: 순수 Kotlin (Android 의존성 없음)
- DTO: data/remote/dto/, Entity: data/local/database/entities/

## Commands

```bash
./gradlew testDebugUnitTest        # 유닛 테스트
./gradlew :app:compileDebugKotlin  # 컴파일 체크
./gradlew assembleDebug            # APK 빌드

# 스크린샷 테스트 (Roborazzi)
./gradlew :app:testDebugUnitTest --tests "com.flit.app.screenshot.*"  # 전체 스크린샷 테스트
./gradlew :app:testDebugUnitTest --tests "com.flit.app.screenshot.*" -Proborazzi.test.record  # 골든 이미지 기록
```

## Code Style

- 주석: 한글 필수
- Compose: remember 람다 캐싱, LazyColumn key 필수, derivedStateOf 활용
- Room: 스키마 변경 시 Migration + 버전 증가 필수
- 보안: API 키 하드코딩 금지, EncryptedSharedPreferences 사용

## UI 작업 필수 참조

UI/UX 관련 작업(화면 추가, 컴포넌트 수정, 디자인 변경 등) 시 반드시 아래 3개 문서를 참조해야 한다:

1. **`docs/direction/philosophy_principles.md`** — 철학 및 설계 판단 기준 (최상위)
2. **`docs/specs/design-guide.md`** — 디자인 시스템 (색상/타이포/간격/컴포넌트/UX 패턴/구현 원칙)
3. **`.claude/skills/apple_hig_guide/SKILL.md`** — Apple HIG 기반 디자인 원칙

핵심 구현 규칙:
- **Theme 우선**: 하드코딩 `Color(0xFF...)` 금지, 반드시 `FlitTheme.colors` 토큰 사용
- **라이브러리 우선**: 커스텀 XML drawable/Canvas 대신 Material Icons, Material3 컴포넌트 사용
- **Material3 오버라이드**: Material3 컴포넌트 사용 시 반드시 FlitTheme 색상을 명시적으로 적용

## 문서 체계 및 컨텍스트 관리

### Docs 디렉토리 구조

```
Docs/
├── doc-index.md             # 문서 인덱스 (전체 목록 + 상태)
├── ROADMAP.md               # 개발 로드맵 + Phase별 참조 문서 매핑
├── direction/               # 사용자 직접 작성 문서 (PRD = 최상위)
├── specs/                   # 명세서 (기능/화면/데이터모델/API)
├── plan/                    # Phase별 개발 계획 문서
│   ├── phase1/
│   ├── phase2a/
│   ├── phase2b/
│   └── phase3/
├── discussion/              # 논의 기록
└── analysis/                # 분석 기록
```

### 문서 우선순위 (상위가 절대 우선)

1. `Docs/direction/` — PRD (**최상위 문서, 모든 것에 우선한다. 사용자 요청 없이 AI가 임의 수정 금지**)
2. `Docs/specs/` — 기능명세서, 화면정의서, 데이터모델 명세서, API 명세서
3. `Docs/plan/` — Phase별 개발 계획 문서

### 문서 변경 흐름

수정사항은 반드시 이 순서로 반영: **PRD → spec 문서 → plan 문서**

### 컨텍스트 사용 규칙

- **Plan Mode (구현 계획 수립 시)**: 반드시 `Docs/direction/` 문서와 `Docs/specs/` 문서를 참조해야 한다
- **개발 진행 시**: 반드시 `Docs/plan/` 계획 문서와 `Docs/specs/` 문서를 참조해야 한다
- **충돌 발견 시**: 계획 문서와 spec 문서가 충돌하면 **즉시 작업을 멈추고 사용자에게 보고**해야 한다
- `Docs/direction/`의 PRD 문서는 가장 최상위 문서이며, 모든 것에 우선한다

### 문서 인덱스 관리

- `Docs/` 디렉토리에 문서를 추가/편집/삭제할 때 반드시 `Docs/doc-index.md`를 함께 업데이트해야 한다

## 참조

- 문서 인덱스: `Docs/doc-index.md`
- 로드맵: `Docs/ROADMAP.md`
- PRD: `Docs/direction/flit_prd_v10.md`

## 코딩 가이드라인

### 1. 코딩 전 생각
- 가정을 명시하고, 불확실하면 질문
- 여러 해석이 가능하면 제시 (조용히 선택하지 말 것)
- 더 단순한 방법이 있으면 제안

### 2. 단순함 우선
- 요청받은 것만 구현, 추측성 기능 금지
- 단일 용도 코드에 추상화 금지
- 불가능한 시나리오에 에러 핸들링 금지

### 3. 최소 변경
- 인접 코드 "개선" 금지, 기존 스타일 유지
- 내 변경으로 생긴 미사용 코드만 삭제
- 모든 변경 라인은 요청에 직접 연결되어야 함

### 4. 목표 기반 실행
- 작업을 검증 가능한 목표로 변환
- 멀티스텝 작업은 계획 명시: `[Step] → verify: [check]`

## 구현 위임 정책

**코드 구현은 기본적으로 Codex(gpt-5.3-codex-medium)에 위임한다.**

- Claude(Opus)는 **계획, 설계, 컨텍스트 수집, 검증** 역할을 담당
- 코드 작성/수정은 `/codex-impl` 스킬을 통해 Codex에 위임
- 사용자가 명시적으로 "Claude가 직접 구현해", "Codex 쓰지 마" 등으로 지시한 경우에만 Claude가 직접 코드를 작성
- 단순 수정(1~2줄 오타, import 추가 등)은 Claude가 직접 처리 가능
- Claude 자체 세팅이나 웹 조사 같은 경우는 자체적으로 판단해서 처리

### Codex 위임 워크플로우

```
1. Claude: 작업 분석 → 관련 파일 읽기 → 구현 계획 수립
2. Claude: Codex에 전달할 자기 완결적 프롬프트 작성
3. Codex: codex exec --full-auto --ephemeral 로 구현
4. Claude: git diff 확인 → 컴파일 체크 → 품질 검증 → 보고
```

## Claude 설정 작업 필수 참조

스킬, 커맨드, 에이전트, 훅, MCP 서버 등 Claude Code 설정 관련 작업 시 반드시 `plugin-dev` 플러그인의 해당 스킬을 참조해야 한다:

| 작업 유형 | 참조 스킬 |
|---|---|
| 플러그인 구조/매니페스트 | `/plugin-dev:plugin-structure` |
| 스킬 생성/수정 | `/plugin-dev:skill-development` |
| 슬래시 커맨드 생성/수정 | `/plugin-dev:command-development` |
| 에이전트 생성/수정 | `/plugin-dev:agent-development` |
| 훅 생성/수정 | `/plugin-dev:hook-development` |
| MCP 서버 통합 | `/plugin-dev:mcp-integration` |
| 플러그인 설정 관리 | `/plugin-dev:plugin-settings` |

## 주의사항
- 새로운 에이전트를 구축할때에는 Sonnet 모델을 사용할 것