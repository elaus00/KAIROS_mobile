# Claude Code Bash 허용 패턴의 한계와 해결

## 발견

Claude Code의 `Bash()` 허용 패턴은 **명령어 문자열의 시작 부분(prefix)**으로만 매칭된다.

```
Bash(jq:*)  →  "jq ..." ✅  /  "SID=$(cat ...) && jq ..." ❌
```

따라서 변수 할당(`VAR=`), 테스트 브라켓(`[`), for 루프 등으로 시작하는 compound 명령은
내부 명령이 허용되어 있어도 매칭되지 않는다.

## deny 패턴도 동일한 한계

`Bash(rm -rf*)` deny 규칙은 `bash -c 'rm -rf /'`를 차단하지 못한다.
`bash -c:*`가 이미 allow에 있으므로 **현재도 우회 가능**.
→ 설정만으로는 완벽한 보안이 불가능하며, AI의 안전 행동이 실질적 방어선.

## 해결: 3가지 조합

1. **쉘 패턴 prefix 추가** (글로벌 settings.json):
   - `Bash(for *)`, `Bash(if *)`, `Bash(while *)`, `Bash([ *)`, `Bash(export *)`, `Bash(SID=*)`

2. **반복 스킬은 전용 스크립트 분리**:
   - `~/.claude/scripts/` 디렉토리에 스크립트 생성
   - `Bash(~/.claude/scripts/*)` 한 줄로 전체 허용

3. **settings.local.json 정리**:
   - 조각난 허용 항목(do/done/for 개별 등록) 제거
   - 패턴 기반 등록으로 대체

## `Bash(*)` 와일드카드는 비권장

deny 리스트를 아무리 확장해도 완벽할 수 없고, 래핑으로 우회되므로
`Bash(*)`보다는 필요한 패턴만 선별 등록하는 것이 안전하다.
