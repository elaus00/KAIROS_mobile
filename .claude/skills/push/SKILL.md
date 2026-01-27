# Push Skill

현재 브랜치를 원격 저장소에 푸시합니다.

## 사용법

```
/push
/push origin feature/my-branch
```

## 실행 단계

1. `git status`로 커밋되지 않은 변경사항 확인
2. `git branch --show-current`로 현재 브랜치 확인
3. `git log origin/<branch>..HEAD`로 푸시할 커밋 확인
4. 원격 브랜치 존재 여부 확인
5. `git push` 실행 (새 브랜치면 `-u` 플래그 사용)

## 안전 규칙

### 절대 금지

- `git push --force` (명시적 요청 없이)
- `git push --force` to `main` or `master` (경고 후에도 권장하지 않음)
- 커밋되지 않은 변경사항이 있을 때 푸시

### 확인 필요

- `main`/`master` 브랜치 직접 푸시 시 경고
- 새로운 브랜치 첫 푸시 시 `-u` 플래그 안내

## 출력 예시

```
📤 Push 준비

현재 브랜치: feature/voice-capture
원격: origin
푸시할 커밋: 3개

커밋 목록:
- a1b2c3d feat(capture): 음성 입력 기능 추가
- d4e5f6g fix(ui): 버튼 정렬 수정
- g7h8i9j chore: 의존성 업데이트

푸시를 진행할까요?
```

## 에러 처리

### 원격에 새로운 커밋이 있는 경우

```
⚠️ 원격 브랜치에 새로운 커밋이 있습니다.

옵션:
1. git pull --rebase 후 다시 푸시
2. git pull --merge 후 다시 푸시

어떤 방법을 사용할까요?
```

### 인증 실패

```
❌ 인증에 실패했습니다.

확인사항:
- SSH 키 설정 확인
- GitHub 토큰 만료 여부 확인
```
