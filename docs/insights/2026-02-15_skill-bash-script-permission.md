# Claude Code 스킬: 복합 Bash 스크립트 권한 문제 해결

**날짜**: 2026-02-15
**컨텍스트**: adb-screenshot 프로젝트 전용 스킬 개발

## 문제

스킬에서 복합 bash 스크립트(for 루프, 변수, 파이프라인 등)를 직접 실행하면, `settings.local.json`의 `permissions.allow` 패턴과 매칭되지 않아 **매번 사용자 승인을 요청**함.

### 실패한 시도

```markdown
# 스킬 SKILL.md
When this skill is invoked, run this bash command:
```bash
screenshot=""
for path in "/sdcard/DCIM/Screenshots" ...; do
  file=$(/opt/homebrew/bin/adb shell "ls -t $path/ ..." | /usr/bin/tr -d '\r')
  ...
done
...
```
```

이 복합 스크립트는 다음 패턴들과 매칭되지 **않음**:
```json
{
  "permissions": {
    "allow": [
      "Bash(/opt/homebrew/bin/adb:*)",
      "Bash(/usr/bin/tr:*)",
      "Bash(~/.claude/scripts/*)"
    ]
  }
}
```

**이유**: 복합 스크립트 전체가 하나의 명령으로 인식되어, 개별 명령 패턴으로는 매칭 불가.

## 해결 방법

복합 스크립트를 **별도 파일로 분리**하고, 스킬에서 파일을 실행한다.

### 1. 스크립트 파일 생성

```bash
# .claude/scripts/adb-screenshot.sh
#!/bin/bash

screenshot=""
screenshot_path=""

for path in "/sdcard/DCIM/Screenshots" "/sdcard/Pictures/Screenshots" "/sdcard/Screenshots"; do
  file=$(/opt/homebrew/bin/adb shell "ls -t $path/ 2>/dev/null | head -n1" | /usr/bin/tr -d '\r')
  if [ -n "$file" ]; then
    screenshot="$file"
    screenshot_path="$path/$file"
    break
  fi
done

if [ -z "$screenshot" ]; then
  echo "ERROR: No screenshot found"
  exit 1
fi

ext="${screenshot##*.}"
if /opt/homebrew/bin/adb pull "$screenshot_path" "/tmp/latest_screenshot.$ext" >/dev/null 2>&1; then
  echo "SUCCESS|/tmp/latest_screenshot.$ext|$screenshot"
else
  echo "ERROR: Pull failed"
  exit 1
fi
```

실행 권한 부여:
```bash
chmod +x .claude/scripts/adb-screenshot.sh
```

### 2. 스킬 수정

```markdown
# .claude/skills/adb-screenshot/SKILL.md
When this skill is invoked, run the adb-screenshot script:

```bash
.claude/scripts/adb-screenshot.sh
```

The script outputs in format: `SUCCESS|local_path|filename` or `ERROR: message`
```

### 3. 권한 설정 (자동)

스크립트 파일을 한 번 실행하면, Claude Code가 자동으로 패턴을 추가함:

```json
{
  "permissions": {
    "allow": [
      "Bash(.claude/scripts/adb-screenshot.sh:*)"
    ]
  }
}
```

또는 미리 와일드카드 패턴을 추가:
```json
{
  "permissions": {
    "allow": [
      "Bash(.claude/scripts/*)"
    ]
  }
}
```

## 핵심 인사이트

1. **복합 bash 스크립트는 권한 패턴 매칭이 안 됨**
   - `for`, `if`, 변수 할당, 파이프라인 등이 포함된 스크립트
   - CLAUDE.md에 "Bash 복합 명령 금지" 규칙으로 명시되어 있음

2. **해결: 스크립트 파일 분리**
   - `.claude/scripts/` 디렉토리에 스크립트 파일 생성
   - 스킬에서 파일 경로로 실행
   - 자동 패턴 매칭 또는 와일드카드 사용

3. **경로 주의**
   - 프로젝트별 스크립트: `.claude/scripts/` (프로젝트 디렉토리)
   - 글로벌 스크립트: `~/.claude/scripts/` (홈 디렉토리)
   - 스킬에서 참조하는 경로와 실제 파일 위치 일치 필수

## 적용 범위

이 패턴은 다음과 같은 경우에 적용:
- 스킬에서 복잡한 bash 로직 실행 필요
- 여러 단계의 데이터 처리 파이프라인
- 조건문, 반복문이 포함된 자동화 스크립트

## 참고

- CLAUDE.md 주의사항: "Bash 복합 명령 금지: 독립 명령은 병렬 도구 호출로 분리할 것"
- 간단한 순차 명령(`&&`로 연결)은 괜찮을 수 있으나, 복잡해지면 파일로 분리 권장
