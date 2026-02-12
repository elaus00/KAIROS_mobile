Android debug APK를 빌드하고, 연결된 기기/에뮬레이터에 설치 후 실행한다.

1. `adb devices`로 연결된 기기 확인
2. 기기가 여러 대면 어디에 설치할지 사용자에게 질문
3. 빌드 + 설치 + 실행:

```bash
cd /Users/elaus/AndroidStudioProjects/Flit && ./gradlew installDebug
adb -s <선택된 기기> shell am start -n com.flit.app/.MainActivity
```

각 단계 결과를 간결하게 보고한다.
