# Performance Log Template

## 사용 규칙
- 한 성능 변경 단위(커밋/PR)당 최소 1행 기록
- 동일 측정 조건에서 Before/After 비교
- 최소 `avg`, `p95` 기록

## 메타 정보
- Date:
- Author:
- Branch/PR:
- Device:
- Android OS:
- Build Variant (e.g. benchmarkRelease):
- Network Condition (wifi/lte/offline):
- Iterations:
- Warmup Excluded:

## 변경 기록
| Date | Area | Scenario | Metric | Before (avg/p95) | After (avg/p95) | Delta | Conditions | Notes |
|---|---|---|---|---|---|---|---|---|
| 2026-02-12 | Search | Search keyword input | local_search_execution | 180ms / 420ms | 130ms / 280ms | -33% p95 | Pixel 8, Android 15, benchmark | FTS query path optimize |

## 지표별 스냅샷 (주간)
| Week | Startup p95 | Capture Save p95 | AI Classify p95 | Local Search p95 | Semantic Search p95 | Scroll Jank |
|---|---:|---:|---:|---:|---:|---:|
| 2026-W07 |  |  |  |  |  |  |

## 회귀 판단 기준(예시)
- p95가 기준 대비 10% 이상 악화되면 회귀 후보
- 2회 이상 재측정에서도 동일하면 이슈 등록
- 릴리즈 브랜치에서는 기준 초과 시 머지 차단(정책 선택)
