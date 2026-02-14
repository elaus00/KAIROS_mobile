#!/usr/bin/env python3
"""PreToolUse 훅: 민감 파일 편집 방지"""
import json, sys

data = json.load(sys.stdin)
path = data.get('tool_input', {}).get('file_path', '')
sensitive_files = ['local.properties', '.env', 'google-services.json', 'keystore', '.jks']

if any(s in path for s in sensitive_files):
    print(f'Error: Cannot edit sensitive file: {path}', file=sys.stderr)
    sys.exit(2)
