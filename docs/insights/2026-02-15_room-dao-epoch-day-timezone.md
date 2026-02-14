# Room DAO에서 epoch day 계산 시 타임존 함정

## 날짜: 2026-02-15

## 문제

Room DAO SQL에서 `start_time / 86400000`으로 epoch day를 계산하면 **UTC 기준** 날짜가 됨.
시스템 타임존(KST = UTC+9)과 불일치 발생.

```sql
-- 잘못된 방식: UTC 기준 날짜 계산
SELECT DISTINCT (s.start_time / 86400000) AS epoch_day ...
```

예: 2월 15일 오전 2시 KST = 2월 14일 17시 UTC → epoch_day가 2월 14일로 계산됨.

## 증상

- 캘린더 dot(일정 표시)이 실제 일정 날짜와 하루 차이
- dot 있는 날짜 클릭 시 "일정 없음"

## 해결

SQL에서 날짜 계산하지 않고 원시 ms 반환 → Kotlin에서 타임존 적용:

```sql
-- 올바른 방식: start_time ms 그대로 반환
SELECT DISTINCT s.start_time FROM schedules s ...
```

```kotlin
// Kotlin에서 로컬 타임존 변환
val dates = startTimes.mapNotNull { ms ->
    Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
}.toSet()
```

## 원칙

- **SQL에서 시간 → 날짜 변환 금지** (타임존 정보 없음)
- 날짜 변환은 항상 타임존을 알고 있는 애플리케이션 레이어에서 수행
- 동일 데이터를 다른 쿼리로 조회하는 경우 경계 조건(`<` vs `<=`) 통일 확인
