# Commit Skill

변경사항을 분석하고 의미있는 커밋 메시지를 생성하여 커밋합니다.
**중요**: 주요 변경사항이 있으면 CLAUDE.md와 README.md를 자동으로 업데이트합니다.

## 사용법

```
/commit
/commit -m "커스텀 메시지"
/commit --no-docs    # 문서 업데이트 건너뛰기
```

## 커밋 메시지 규칙

### 타입 (Type)

| 타입 | 설명 |
|------|------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 리팩토링 (기능 변경 없음) |
| `style` | 코드 포맷팅, 세미콜론 누락 등 |
| `docs` | 문서 수정 |
| `test` | 테스트 코드 추가/수정 |
| `chore` | 빌드 설정, 패키지 매니저 등 |

### 메시지 형식

```
<type>(<scope>): <subject>

<body>

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>
```

### 예시

```
feat(capture): 음성 입력 기능 추가

- VoiceRecognizer 클래스 구현
- CaptureViewModel에 음성 녹음 상태 추가
- 오프라인 시 로컬 저장 지원

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>
```

## 실행 단계

1. `git status`로 변경된 파일 확인
2. `git diff`로 변경 내용 분석
3. `git log --oneline -5`로 최근 커밋 스타일 확인
4. 변경 내용에 맞는 타입과 스코프 결정
5. **[NEW] 문서 업데이트 필요 여부 판단** (아래 기준 참조)
6. **[NEW] 필요시 CLAUDE.md, README.md 업데이트**
7. 의미있는 커밋 메시지 생성
8. 사용자에게 메시지 확인 요청
9. `git add`로 관련 파일만 스테이징 (민감 파일 제외)
10. `git commit` 실행

## 문서 자동 업데이트 규칙

### 업데이트 대상

| 변경 유형 | CLAUDE.md | README.md |
|----------|-----------|-----------|
| 새로운 기능/화면 추가 (`feat`) | ✅ | ✅ |
| 새로운 UseCase/Repository 추가 | ✅ | ❌ |
| API 엔드포인트 변경 | ❌ | ✅ |
| 아키텍처 구조 변경 | ✅ | ✅ |
| DB 스키마 변경 (Migration) | ✅ | ❌ |
| 빌드 명령어 변경 | ✅ | ✅ |
| 버그 수정 (`fix`) | ❌ | ❌ |
| 리팩토링 (`refactor`) | ❌ | ❌ |
| 테스트 추가 (`test`) | ❌ | ❌ |

### CLAUDE.md 업데이트 내용
- 새로운 UseCase/Repository 목록
- 아키텍처 레이어별 규칙 변경
- Database 버전/테이블 변경
- 새로운 개발 가이드라인

### README.md 업데이트 내용
- 주요 기능 섹션
- 프로젝트 구조
- API 문서
- Phase 완료 상태

### 업데이트 스킵 조건
- `--no-docs` 플래그 사용 시
- `docs`, `style`, `test`, `chore` 타입 커밋
- CLAUDE.md, README.md만 변경된 경우

## 주의사항

- **민감 파일 제외**: `local.properties`, `.env`, `google-services.json`, `*.jks`는 절대 커밋하지 않음
- **amend 금지**: 기존 커밋 수정은 명시적 요청 없이 하지 않음
- **force push 금지**: `--force` 옵션은 사용하지 않음
- **한글 커밋 메시지**: 본문은 한글로 작성 가능, 타입과 스코프는 영문
- **문서 업데이트**: 기존 내용 유지하면서 추가/수정 (덮어쓰기 금지)
