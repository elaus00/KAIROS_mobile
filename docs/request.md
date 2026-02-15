# 사용자 조치 필요 항목

## [2026-02-15] FCM 인프라 세팅 세션

- [ ] **`app/src/main/google-services.json` 삭제** — production으로 복사 후 남은 불필요 파일. Flavor별 파일(`dev/`, `staging/`, `production/`)만 유지하면 됨

## [2026-02-15] Google OAuth + 버전 업그레이드 세션

- [ ] **Google Cloud Console: release 키스토어 등록**
  - `app.flit.mobile.staging` + SHA-1: `A3:22:8E:EA:4E:AA:D2:E6:14:57:4F:BC:8C:81:AE:18:33:E9:AE:A9`
  - `app.flit.mobile` (production) + 동일 SHA-1
- [ ] **Google 로그인 실기기 테스트** — dev 서버 배포 완료, 기기에서 로그인 동작 확인 필요
- [ ] **모바일 develop 브랜치 커밋** — CalendarViewModel 수정 + 라이브러리 버전 업그레이드 (libs.versions.toml) 아직 미커밋

---
