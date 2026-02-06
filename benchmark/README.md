# Macrobenchmark Module

Run on connected device/emulator:

```bash
./gradlew :benchmark:connectedBenchmarkAndroidTest
```

Main scenarios:
- `StartupBenchmark.coldStart`
- `StartupBenchmark.firstInputLatency`
- `CaptureFlowBenchmark.captureSaveCompletionTime`
- `CaptureFlowBenchmark.aiClassificationCompletionTime`
- `ScrollJankBenchmark.notesScrollJank`
- `ScrollJankBenchmark.searchScrollJank`
