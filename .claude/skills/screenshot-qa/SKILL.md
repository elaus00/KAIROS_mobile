# Screenshot QA 스킬

## 개요
Roborazzi 스크린샷 테스트를 활용한 디자인 QA 워크플로우

## 워크플로우

### 1. 디자인 문제 발견 시
1. 해당 화면의 스크린샷 테스트 실행: `./gradlew :app:testDebugUnitTest --tests "com.flit.app.screenshot.XxxScreenshotTest"`
2. `app/src/test/screenshots/` 내 생성된 PNG 확인
3. `docs/specs/design-guide.md` 대조하여 디자인 토큰 검증

### 2. 디자인 수정 후
1. 코드 수정
2. 스크린샷 테스트 재실행으로 변경 확인
3. 골든 이미지 업데이트: `./gradlew :app:testDebugUnitTest --tests "com.flit.app.screenshot.*" -Proborazzi.test.record`

### 3. 디자인 토큰 검증 체크리스트
- [ ] FlitTheme.colors 토큰 사용 (하드코딩 Color 금지)
- [ ] FlitTheme.typography 사용
- [ ] 간격: 4dp 그리드 준수
- [ ] 터치 타겟: 최소 44dp
- [ ] 다크 모드 대응 확인

### 4. 골든 이미지 업데이트 프로토콜
의도적 UI 변경 시:
1. 스크린샷 기록 모드 실행: `./gradlew :app:testDebugUnitTest --tests "com.flit.app.screenshot.*" -Proborazzi.test.record`
2. 생성된 스크린샷을 git에 커밋
3. PR에 변경 전/후 스크린샷 첨부

## 명령어 참조
- 전체 스크린샷 테스트: `./gradlew :app:testDebugUnitTest --tests "com.flit.app.screenshot.*"`
- 특정 화면: `./gradlew :app:testDebugUnitTest --tests "com.flit.app.screenshot.XxxScreenshotTest"`
- 골든 이미지 기록: `./gradlew :app:testDebugUnitTest --tests "com.flit.app.screenshot.*" -Proborazzi.test.record`
