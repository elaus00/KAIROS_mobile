#!/usr/bin/env python3
"""PreToolUse 훅: .kt 파일 직접 수정 차단 — Codex 위임 강제

정책: 코드 구현은 /codex-impl 스킬을 통해 Codex에 위임해야 한다.
우회: .claude/.direct-impl 플래그 파일이 존재하면 직접 수정 허용.
"""
import json, sys, os

data = json.load(sys.stdin)
path = data.get('tool_input', {}).get('file_path', '')

# .kt 파일이 아니면 허용
if not path.endswith('.kt'):
    sys.exit(0)

# 직접 구현 플래그 파일이 있으면 허용
project_dir = os.environ.get('CLAUDE_PROJECT_DIR', '.')
flag_file = os.path.join(project_dir, '.claude', '.direct-impl')
if os.path.exists(flag_file):
    sys.exit(0)

# .kt 파일 직접 수정 차단
output = {
    "hookSpecificOutput": {
        "permissionDecision": "deny"
    },
    "systemMessage": (
        ".kt 파일 직접 수정이 차단되었습니다. "
        "/codex-impl 스킬을 통해 Codex에 구현을 위임하세요. "
        "사용자가 직접 구현을 지시한 경우, "
        ".claude/.direct-impl 파일을 생성하여 이 제한을 해제하세요 "
        "(작업 완료 후 삭제)."
    )
}
print(json.dumps(output))
