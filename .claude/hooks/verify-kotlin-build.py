#!/usr/bin/env python3
"""PostToolUse 훅: Kotlin 파일 편집 후 빌드 검증"""
import json, sys, subprocess, os

data = json.load(sys.stdin)
path = data.get("tool_input", {}).get("file_path", "")

if path.endswith(".kt"):
    print("🔨 Kotlin 빌드 검증 중...")
    os.chdir("/Users/elaus/AndroidStudioProjects/Flit")
    result = subprocess.run(
        ["./gradlew", ":app:compileDebugKotlin", "--quiet"],
        capture_output=True, text=True, timeout=120
    )
    if result.returncode != 0:
        print("❌ 빌드 실패:")
        print(result.stderr[:1000] if result.stderr else result.stdout[:1000])
    else:
        print("✅ 빌드 성공")
