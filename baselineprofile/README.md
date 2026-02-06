# Baseline Profile Module

Generate baseline profile on connected device/emulator:

```bash
./gradlew :baselineprofile:connectedBenchmarkAndroidTest
```

Then build app with profile applied:

```bash
./gradlew :app:assembleRelease
```
