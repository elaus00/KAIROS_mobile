# FCM 인프라 세팅 시 발견한 함정들

## 날짜: 2026-02-15

## 1. AGP 9.0.1 + kapt 플러그인 비호환

AGP 9.0.1은 built-in Kotlin을 사용하며, `org.jetbrains.kotlin.kapt` 플러그인과 호환되지 않는다.

**에러 메시지:**
```
The 'org.jetbrains.kotlin.kapt' plugin is not compatible with built-in Kotlin support.
```

**결론:** Hilt 등 annotation processor는 반드시 KSP(`ksp(libs.hilt.compiler)`)로 사용해야 한다. kapt 전환은 불가.

## 2. Hilt ASM 캐시 충돌

KSP/kapt 전환 시도 후 또는 Gradle 설정 변경 후, `./gradlew clean` 한 번으로 Hilt ASM 변환 캐시가 완전히 제거되지 않을 수 있다.

**에러:**
```
Cannot access output property 'classesOutputDir' of task ':app:transformDevDebugClassesWithAsm'
> NoSuchFileException: .../dagger/hilt/internal/aggregatedroot
```

**해결:** `./gradlew clean`을 다시 실행 후 재빌드. 그래도 안 되면 `app/build/` 디렉토리 수동 삭제.

## 3. google-services.json Flavor별 배치 경로

google-services 플러그인은 `app/src/main/`을 검색하지 않는다. Flavor별로 정확한 경로에 배치해야 한다:

- `app/src/dev/google-services.json` (devDebug)
- `app/src/staging/google-services.json` (stagingRelease)
- `app/src/production/google-services.json` (productionRelease)

검색 순서 예시 (devDebug):
1. `app/src/dev/debug/`
2. `app/src/debug/dev/`
3. `app/src/dev/` ← 여기서 찾음
4. `app/src/debug/`
5. `app/src/devDebug/`
6. `app/`
