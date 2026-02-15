# ADB Pull Screenshot 스킬

기기에서 사용자가 직접 캡처한 최근 스크린샷을 가져와서 확인합니다.

## 사용법

```
/adb-pull-screenshot          # 최근 1장
/adb-pull-screenshot 3        # 최근 3장
```

## 실행 단계

1. `adb devices`로 연결된 기기 확인
2. `.claude/scripts/adb-pull-screenshot.sh [N]` 실행하여 최근 스크린샷 N장 가져오기
3. 반환된 파일 경로의 이미지를 Read 도구로 읽어서 사용자에게 보여줌
4. 캡처된 화면에 대해 간단히 설명

## 인자

- 숫자 인자: 가져올 스크린샷 개수 (기본값: 1)

## 오류 처리

- 기기 미연결 시: `adb devices` 결과를 보여주고 기기 연결 안내
- 스크린샷 없음: 에러 메시지 전달
