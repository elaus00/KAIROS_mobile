# adb-screenshot

Android 디바이스에서 가장 최근에 캡처된 스크린샷을 adb로 가져와서 읽습니다.

## 사용법

```
/adb-screenshot
```

## 동작

1. 연결된 Android 디바이스의 스크린샷 디렉토리에서 가장 최근 파일을 찾습니다
2. adb pull로 로컬 임시 디렉토리로 가져옵니다
3. Read tool로 스크린샷을 읽어 사용자에게 보여줍니다

## 스크린샷 저장 위치

- `/sdcard/Pictures/Screenshots/` (대부분의 Android 디바이스)
- `/sdcard/DCIM/Screenshots/` (일부 디바이스)

---

**Instructions for Claude:**

When this skill is invoked, run the adb-screenshot script:

```bash
.claude/scripts/adb-screenshot.sh
```

The script outputs in format: `SUCCESS|local_path|filename` or `ERROR: message`

Then:
1. Parse the output
2. If SUCCESS: Use Read tool on the local path
3. Report the filename to user

**Error handling:**
- If no device connected: "adb 디바이스가 연결되지 않았습니다. USB 디버깅을 확인해주세요."
- If no screenshots found: "스크린샷을 찾을 수 없습니다. 디바이스에서 스크린샷을 캡처한 후 다시 시도해주세요."
- If pull fails: "스크린샷 가져오기 실패. adb 권한을 확인해주세요."

**Output:**
- Always show the screenshot using the Read tool
- Include the device path and timestamp in your response
- Be concise - just show the image and basic info
