# Performance Log

## Baseline v0

- Date: 2026-02-13
- Author: Codex + elaus
- Device: SM-S931N
- Android OS: 16 (SDK 36)
- Build Variant: benchmark
- Network Condition: Wi-Fi
- Iterations: 10
- Warmup Excluded: 0
- Source: `benchmark/build/outputs/connected_android_test_additional_output/benchmark/connected/SM-S931N - 16/com.flit.app.benchmark-benchmarkData.json`

| Date | Area | Scenario | Metric | Before (avg/p95) | After (avg/p95) | Delta | Conditions | Notes |
|---|---|---|---|---|---|---|---|---|
| 2026-02-13 | Startup | `StartupBenchmark.coldStart` | `timeToInitialDisplayMs` | - | 159.54ms / 189.25ms | baseline | SM-S931N, benchmark, n=10 | First measured baseline |
| 2026-02-13 | Startup | `StartupBenchmark.firstInputLatency` | `first_input_latencySumMs` | - | 0.0357ms / 0.0626ms | baseline | SM-S931N, benchmark, n=10 | Trace metric (n=10) |

## Pending Baseline Metrics

- `capture_save_completion_ms`
- `ai_classification_completion_ms`
- `search_scroll_jank_percent`
- `notes_scroll_jank_percent`

Reason: current UI selector mismatch in benchmark utils (`None of the selectors matched` at `BenchmarkTestUtils.kt:135`) for capture/search scenarios.
