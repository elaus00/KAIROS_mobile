---
name: codex-impl
description: Codex(gpt-5.3-codex)에 구현 작업을 위임합니다. Claude가 계획/설계하고 Codex가 코드를 구현합니다.
argument-hint: <구현할 작업 설명>
user-invocable: true
allowed-tools: Read, Grep, Glob, Bash, Write
---

# Codex 구현 위임 스킬

Claude(Opus)가 설계/계획을 수립하고, Codex(gpt-5.3-codex)가 실제 코드를 구현하는 워크플로우.

## 실행 절차

### 1단계: 작업 분석 및 컨텍스트 수집

$ARGUMENTS를 분석하여 구현에 필요한 파일과 컨텍스트를 파악한다.

- 관련 소스 파일을 Read로 읽어 현재 코드 구조를 파악
- 수정 대상 파일 경로, 의존 관계, 기존 패턴을 확인
- 필요 시 Grep/Glob으로 관련 코드를 탐색

### 2단계: 구현 프롬프트 작성

Codex에 전달할 **자기 완결적인 구현 지시서**를 작성하여 `/tmp/codex-prompt.txt`에 저장한다.

프롬프트에 반드시 포함할 내용:
- **목표**: 무엇을 구현/수정해야 하는지
- **대상 파일**: 수정할 파일의 전체 경로
- **코드 규칙**: 아래 프로젝트 규칙을 반드시 포함
- **구체적 지시**: 함수명, 클래스명, 파라미터 등 구체적 설계

프롬프트에 항상 포함할 프로젝트 규칙:
```
## 프로젝트 규칙 (반드시 준수)
- 주석은 한글로 작성
- Compose: remember 람다 캐싱, LazyColumn key 필수, derivedStateOf 활용
- 하드코딩 Color(0xFF...) 금지, 반드시 FlitTheme.colors 토큰 사용
- Material3 컴포넌트 사용 시 FlitTheme 색상을 명시적으로 적용
- Domain Layer는 순수 Kotlin (Android 의존성 없음)
- DTO: data/remote/dto/, Entity: data/local/database/entities/
- 기존 코드 스타일과 패턴을 따를 것
- 불필요한 추상화, 에러 핸들링, 기능 추가 금지
```

### 3단계: Codex 실행

Write 도구로 `/tmp/codex-prompt.txt`에 프롬프트를 저장한 뒤, 아래 Bash 명령을 실행한다:

```bash
codex exec --full-auto --ephemeral -o /tmp/codex-result.txt - < /tmp/codex-prompt.txt
```

**주의**: 이 명령은 시간이 걸릴 수 있으므로 timeout을 충분히 설정한다 (최소 300000ms).

### 4단계: 결과 검증

Codex 완료 후 반드시 수행:

1. `/tmp/codex-result.txt`를 Read로 읽어 Codex 작업 결과 확인
2. `git diff`로 실제 변경된 파일과 내용 확인
3. 변경된 파일을 Read로 읽어 코드 품질 검증:
   - 프로젝트 규칙 준수 여부
   - 기존 패턴과의 일관성
   - 불필요한 변경이 없는지
4. 컴파일 체크: `./gradlew :app:compileDebugKotlin`
5. 문제가 있으면 사용자에게 보고하고 직접 수정하거나 Codex를 재호출

### 5단계: 결과 보고

사용자에게 다음을 보고한다:
- Codex가 수정한 파일 목록
- 주요 변경 사항 요약
- 컴파일 결과
- 추가 조치가 필요한 사항 (있을 경우)

## 주의사항

- Codex는 별도 프로세스이므로 Claude의 대화 컨텍스트를 모른다. 프롬프트에 모든 필요 정보를 포함할 것.
- 병렬로 여러 Codex를 실행할 때는 서로 다른 파일을 수정하도록 분리할 것.
- Codex 결과를 무조건 신뢰하지 말 것. 반드시 검증 후 사용자에게 보고한다.
