# Flit. Performance Measurement

## Official Metrics (Fixed)

1. `cold_start_ms`
- Definition: app cold start time.
- Source: `StartupTimingMetric` in `:benchmark`.

2. `first_input_latency_ms`
- Definition: latency from first character input handling.
- Source: trace section `first_input_latency` in `CaptureViewModel.updateInput`.

3. `capture_save_completion_ms`
- Definition: latency until capture local-save + classify-queue registration is done.
- Source: trace section `capture_save_completion` in `SubmitCaptureUseCase`.

4. `search_scroll_jank_percent`
- Definition: jank ratio during search results scroll.
- Source: `FrameTimingGfxInfoMetric` in `searchScrollJank` scenario.

5. `ai_classification_completion_ms`
- Definition: latency for one classify worker cycle (API + classification apply).
- Source: trace section `ai_classification_completion` in `ClassifyCaptureWorker`.

## Standard Scenarios

1. App launch
- `StartupBenchmark.coldStart`

2. Home input and save
- `CaptureFlowBenchmark.captureSaveCompletionTime`

3. Notes scroll
- `ScrollJankBenchmark.notesScrollJank`

4. Search scroll
- `ScrollJankBenchmark.searchScrollJank`

## Module Structure

- `:benchmark`: Macrobenchmark test module
- `:baselineprofile`: Baseline Profile generator module
- `:app`: consumes generated baseline profile via `baselineProfile(project(":baselineprofile"))`

## Build/Manifest Policy

- Added `benchmark` buildType in `:app` (release-like for measurement)
- Added `profileable` in:
  - `app/src/release/AndroidManifest.xml`
  - `app/src/benchmark/AndroidManifest.xml`

## Baseline Profile Pipeline

1. Generate baseline profile:
```bash
./gradlew :baselineprofile:connectedBenchmarkAndroidTest
```

2. Build app with generated profile applied:
```bash
./gradlew :app:assembleRelease
```

3. Run macrobenchmark:
```bash
./gradlew :benchmark:connectedBenchmarkAndroidTest
```

## Regression Gate

- Baseline/threshold file: `Docs/performance/perf_gate_baseline.json`
- Gate script: `scripts/perf/perf_gate.py`
- Metric extraction script: `scripts/perf/extract_current_metrics.py`
- Example:
```bash
python3 scripts/perf/extract_current_metrics.py \
  --input benchmark/build/outputs/connected_android_test_additional_output/.../com.flit.app.benchmark-benchmarkData.json \
  --output docs/performance/current_metrics.json

python3 scripts/perf/perf_gate.py \
  --baseline docs/performance/perf_gate_baseline.json \
  --current docs/performance/current_metrics.json \
  --report docs/performance/perf_report.md
```

Rules:
- Time metrics: fail when regression exceeds configured `%`.
- Jank metric: fail when current jank ratio exceeds configured absolute threshold.

## PR Report Format (4 Columns)

- Current
- Baseline
- Delta
- Verdict

Template: `Docs/performance/pr_report_template.md`
