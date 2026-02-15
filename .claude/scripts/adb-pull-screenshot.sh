#!/bin/bash
# 기기에서 사용자가 캡처한 최근 스크린샷을 로컬로 가져오는 스크립트

set -euo pipefail

SAVE_DIR="/tmp/flit-screenshots"
DEVICE_SCREENSHOT_DIR="/sdcard/DCIM/Screenshots"
COUNT="${1:-1}"

mkdir -p "$SAVE_DIR"

# 최근 스크린샷 파일명 가져오기 (최신순, 줄 단위로 처리)
FILES=$(adb shell "ls -t '${DEVICE_SCREENSHOT_DIR}/' 2>/dev/null" | head -n "$COUNT" | tr -d '\r')

if [ -z "$FILES" ]; then
  echo "ERROR: 기기에 스크린샷이 없습니다 (${DEVICE_SCREENSHOT_DIR})" >&2
  exit 1
fi

# 각 파일을 로컬로 pull (파일명에 공백/괄호 대응)
while IFS= read -r FILE; do
  [ -z "$FILE" ] && continue
  adb pull "${DEVICE_SCREENSHOT_DIR}/${FILE}" "${SAVE_DIR}/${FILE}" > /dev/null 2>&1
  echo "${SAVE_DIR}/${FILE}"
done <<< "$FILES"
