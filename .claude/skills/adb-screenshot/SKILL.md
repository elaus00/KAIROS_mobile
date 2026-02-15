# ADB Screenshot 스킬

연결된 Android 기기/에뮬레이터의 현재 화면을 직접 캡처합니다.

## 사용법

```
/adb-screenshot
```

## 실행 단계

1. `adb devices`로 연결된 기기 확인
2. `.claude/scripts/adb-screenshot.sh` 스크립트 실행하여 현재 화면 캡처
3. 반환된 파일 경로의 이미지를 Read 도구로 읽어서 사용자에게 보여줌
4. 캡처된 화면에 대해 간단히 설명

## 오류 처리

- 기기 미연결 시: `adb devices` 결과를 보여주고 기기 연결 안내
- 스크립트 실패 시: 에러 메시지 전달
