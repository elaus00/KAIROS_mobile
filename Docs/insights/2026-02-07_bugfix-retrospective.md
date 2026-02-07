# 2026-02-07 Bugfix Retrospective

## 이번 수정에서 얻은 교훈

1. ViewModel 생명주기에 삭제 확정 로직을 묶으면 데이터 정합성이 깨진다.
- 지연 하드 삭제를 `viewModelScope`에만 두면 화면 이탈 시 취소되어 soft-delete 잔존 데이터가 누적된다.
- 삭제 확정은 UI 수명주기와 분리된 경로(Worker/앱 스코프/즉시 확정 보정)가 필요하다.

2. 페이지네이션은 "초기 페이지만 reactive" 구조를 피해야 한다.
- 1페이지는 `collect`, 추가 페이지는 `first()`로 읽으면 페이지별 동기화 수준이 달라져 UX 불일치가 발생한다.
- 로드한 모든 페이지를 동일한 구독 모델로 다루고, 병합 규칙을 한 곳에서 관리해야 한다.

3. 목록 화면에서 N+1은 기능 버그로 이어질 수 있다.
- Notes에서 개별 `getCaptureById()` 호출은 단순 성능 문제가 아니라 soft-delete 필터 누락으로 노출 버그를 만들었다.
- 화면 요구사항(삭제 제외, 정렬)을 DAO 조인 쿼리로 명시하는 편이 안전하다.

4. FTS 검색은 사용자 입력을 신뢰하면 안 된다.
- MATCH 연산자는 특수문자를 쿼리 문법으로 해석하므로, 입력 sanitize/escape가 기본값이어야 한다.
- 예외 처리만으로는 "검색 실패"는 막아도 "예상치 못한 결과"는 막지 못한다.

5. Domain 계층의 Android 의존성은 테스트성과 설계를 동시에 악화시킨다.
- UseCase에서 `WorkManager`, `Trace`를 직접 참조하면 계층 경계가 무너진다.
- 실행 트리거는 Repository 추상 메서드로 올리고, Android 구현은 Data 계층으로 내리는 구조가 유지보수에 유리하다.

6. 기능 토글은 하드코딩보다 BuildConfig가 안전하다.
- `USE_MOCK_API = true` 같은 상수는 릴리즈 오배포 위험을 만든다.
- Build type별 `BuildConfig` 플래그로 운영/개발 동작을 분리하면 실수가 줄어든다.

7. DB 스키마 변경은 "엔티티 등록 + 마이그레이션 + 환경별 정책"이 한 세트다.
- 엔티티만 추가하거나 `fallbackToDestructiveMigration`만 유지하면 리스크가 남는다.
- 버전 증가, 명시적 Migration, destructive 허용 범위(예: debug 한정)를 동시에 관리해야 한다.
