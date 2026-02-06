# CLAUDE.md & 에이전틱 개발 컨텍스트 관리 방법론

**작성일**: 2026-02-06
**목적**: 프로젝트 단계별 컨텍스트 관리 전략 수립

---

## 1. CLAUDE.md 개요

### 정의
CLAUDE.md는 Claude Code가 **매 세션 시작 시 자동으로 읽는** 마크다운 파일.
프로젝트별 지침을 저장해 매번 반복 설명 없이 일관된 컨텍스트 유지.

### 파일 위치 (우선순위 순)

| 위치 | 용도 |
|------|------|
| `프로젝트루트/CLAUDE.md` | 팀 공유용 (git 커밋) |
| `.claude/CLAUDE.md` | 서브디렉토리 정리 |
| `CLAUDE.local.md` | 개인 설정 (gitignore 권장) |
| `~/.claude/CLAUDE.md` | 전역 기본값 |

### 베스트 프랙티스

1. **간결하게 유지** - 컨텍스트는 한정된 자원, 모든 줄이 실제 작업과 경쟁
2. **/init으로 시작** - 자동 생성 후 불필요한 부분 삭제
3. **실제 워크플로우 반영** - 이론적 베스트 프랙티스가 아닌 팀 실제 개발 방식
4. **민감 정보 제외** - API 키, 비밀번호 절대 포함 금지

---

## 2. 핵심 인사이트: 단계별 컨텍스트 분리 필요성

### 문제 인식

프로젝트 단계에 따라 필요한 컨텍스트가 다름:

| 단계 | 필요한 컨텍스트 | 불필요한 컨텍스트 |
|------|----------------|------------------|
| **초기 기획** | 비전, 철학, 사용자 문제, 시장 | 코드 스타일, 빌드 명령어 |
| **기획 구체화** | PRD, 유저 스토리, 기술 제약사항 | 구현 세부사항, 테스트 전략 |
| **개발** | 아키텍처, 코드 스타일, 빌드 명령어 | 비전 철학의 장황한 설명 |

### 결론

단일 CLAUDE.md에 모든 컨텍스트를 담으면:
- 토큰 낭비
- 불필요한 정보가 주의를 분산
- 단계에 맞지 않는 컨텍스트 로드

---

## 3. 발견한 방법론들

### 방법 A: 3-File Pattern (Manus 스타일)

**출처**: [planning-with-files](https://github.com/OthmanAdi/planning-with-files)

```
프로젝트/
├── CLAUDE.md           # 최소한의 고정 컨텍스트
├── task_plan.md        # 마스터 블루프린트 (phases, checkboxes)
├── findings.md         # 연구/발견 누적
└── progress.md         # 세션 로그 (에러, 시도)
```

**핵심 원칙**: "Context Window = RAM, Filesystem = Disk"

**장점**:
- 복잡한 멀티스텝 작업에 적합
- 세션 간 정보 손실 방지
- 실패 반복 방지 (progress.md)

---

### 방법 B: ROADMAP + Tasks 폴더

**출처**: [Zhu Liang 워크플로우](https://thegroundtruth.media/p/my-claude-code-workflow-and-personal-tips)

```
프로젝트/
├── CLAUDE.md           # ROADMAP.md import
├── ROADMAP.md          # 전체 개발 프로세스 개요
├── tasks/
│   ├── feature-auth.md     # PRD + 시스템 설계
│   ├── feature-search.md
│   └── feature-sync.md
├── AD_HOC_TASKS.md     # 작은 개선사항
└── REFACTORS.md        # 리팩토링 목록
```

**핵심**: 각 task 파일이 "PRD + 시스템 설계"를 결합

**워크플로우**:
1. Planning Phase: ROADMAP.md 참조
2. /clear (컨텍스트 리셋)
3. Implementation: 새 세션에서 task 파일 참조
4. Ad Hoc: 작은 작업은 한 세션에 하나씩

---

### 방법 C: Feature-Specific Context

**출처**: [Alabê Duarte](https://alabeduarte.com/context-engineering-with-claude-code-my-evolving-workflow/)

**단일 CLAUDE.md 대신 기능별 마크다운 생성:**

```
1. Understanding → SEARCH_FEATURE.md 생성
2. /clear
3. Planning → @SEARCH_FEATURE.md 참조하며 계획
4. /clear
5. Implementation → @SEARCH_FEATURE.md#L7-17 정밀 참조
```

**특징**:
- 기능별 독립 문서
- 정밀한 라인 참조 가능
- 버전 관리에 커밋하지 않음 (개인 참조용)

---

## 4. KAIROS 프로젝트 적용 제안

### 현재 문제점

| 항목 | 문제 |
|------|------|
| CLAUDE.md | 211줄 - 너무 김 (Phase 3 완료 내역까지 포함) |
| Docs 구조 | Phase 문서들이 flat하게 나열됨 |
| 컨텍스트 분리 | 기획/개발 컨텍스트 혼재 |

### 제안 구조 (방법 B 변형)

```
KAIROS_mobile/
├── CLAUDE.md                      # 핵심만 (~50줄)
├── ROADMAP.md                     # 로드맵 + 단계별 참조 정의
│
└── Docs/
    ├── reference/                 # 🔒 고정 레퍼런스 (항상 유효)
    │   ├── architecture.md        # 아키텍처 레이어 규칙
    │   ├── code-style.md          # 코드 스타일 + Compose 규칙
    │   └── philosophy.md          # 앱 철학 상세 (3 Zero)
    │
    ├── plan/                      # 📋 기획 문서
    │   └── PRD_KAIROS_Inbox.md    # 현재 PRD
    │
    ├── phases/                    # 🚀 단계별 구현
    │   ├── phase2-enhanced-input/
    │   │   └── implementation.md
    │   ├── phase3-integration/
    │   │   ├── implementation.md
    │   │   └── summary.md
    │   └── phase4-polish/
    │       ├── tasks.md           # TODO 목록
    │       └── context.md         # 이 단계에서 필요한 컨텍스트
    │
    ├── analysis/                  # 📊 분석/리서치 문서
    │   └── ...
    │
    └── discussion/                # 💬 토론/의사결정 기록
        └── ...
```

### 핵심 파일 설계

#### CLAUDE.md (개발 세션용 - 간결)

```markdown
# KAIROS Mobile

멀티모달 캡처 앱. Clean Architecture + MVVM + Jetpack Compose.

## 핵심 철학
> "떠오른 순간, 바로 던지면 끝" - 캡처 friction 최소화

## 아키텍처
- presentation/ → domain/ → data/ (단방향)
- Domain: 순수 Kotlin (Android 의존성 금지)
- DTO: data/remote/dto/, Entity: data/local/database/entities/

## 빌드
./gradlew testDebugUnitTest    # 테스트
./gradlew assembleDebug        # APK

## 스타일
- 주석: 한글
- Compose: remember 람다 캐싱, LazyColumn key 필수
- Room: 스키마 변경 시 Migration 필수

## 현재 단계
@see ROADMAP.md
```

#### ROADMAP.md (로드맵 + 단계별 참조)

```markdown
# KAIROS Development Roadmap

## 현재 진행: Phase 4 - Polish

### 단계별 상태

| Phase | 상태 | 주요 기능 |
|-------|------|----------|
| Phase 1 | ✅ 완료 | 텍스트 캡처, AI 분류, 오프라인 |
| Phase 2 | ✅ 완료 | 이미지, 공유 인텐트, 웹클립 |
| Phase 3 | ✅ 완료 | Calendar, Todoist, 히스토리 |
| **Phase 4** | 🚧 진행중 | 위젯, 알림, 글씨 크기, 다국어 |

---

## Phase 4: Polish

### 참조 문서
- 전체 PRD: @Docs/plan/PRD_KAIROS_Inbox.md
- TODO 목록: @Docs/phases/phase4-polish/tasks.md
- 아키텍처: @Docs/reference/architecture.md

### 작업 목록
1. [ ] 글씨 크기 설정
2. [ ] 다국어 지원 (한/영)
3. [ ] 홈 위젯 (Glance API)
4. [ ] 위젯 퀵 캡처
```

### 세션별 컨텍스트 활용 가이드

| 작업 유형 | 로드할 컨텍스트 |
|----------|---------------|
| **일반 개발** | CLAUDE.md + ROADMAP.md (기본) |
| **기획/설계 판단** | + @Docs/reference/philosophy.md |
| **새 기능 구현** | + @Docs/phases/phase4-polish/context.md |
| **아키텍처 변경** | + @Docs/reference/architecture.md |
| **PRD 확인 필요** | + @Docs/plan/PRD_KAIROS_Inbox.md |

---

## 5. 기대 효과

1. **토큰 효율성**: 기본 세션에 50줄만 로드
2. **필요 시 확장**: 명시적으로 @reference 호출
3. **관심사 분리**: 철학/아키텍처/단계별 작업 분리
4. **재사용성**: 다음 프로젝트에서 구조 템플릿으로 활용

---

## 6. 참고 자료

- [The Complete Guide to CLAUDE.md - Builder.io](https://www.builder.io/blog/claude-md-guide)
- [Using CLAUDE.MD files - Claude Blog](https://claude.com/blog/using-claude-md-files)
- [Agentic Development in 2026 - DEV Community](https://dev.to/chand1012/the-best-way-to-do-agentic-development-in-2026-14mn)
- [CLAUDE.md Examples - GitHub](https://github.com/ArthurClune/claude-md-examples)
- [Context Engineering Workflow - Alabê Duarte](https://alabeduarte.com/context-engineering-with-claude-code-my-evolving-workflow/)
- [Claude Code Workflow Tips - Zhu Liang](https://thegroundtruth.media/p/my-claude-code-workflow-and-personal-tips)
- [Planning-First Development - Nathan Fox](https://www.nathanfox.net/p/planning-first-development-claude-code)
- [Planning with Files (Manus Style)](https://github.com/OthmanAdi/planning-with-files)

---

## 7. 다음 단계

- [ ] 제안 구조 승인 후 실제 파일 생성/이동
- [ ] CLAUDE.md 간소화
- [ ] ROADMAP.md 생성
- [ ] Docs/reference/ 폴더 구성
- [ ] Docs/phases/ 구조 정리
