#!/bin/bash

# Android 디바이스에서 최근 스크린샷 찾기 및 가져오기

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
