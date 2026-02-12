# Benchmark + APM 운영 정리 (2026-02-12)

## 목적
Flit 앱의 성능을 출시 전까지 점진적으로 개선하기 위해,
"어디서 보고/무엇을 측정하고/어떻게 기록할지"를 팀 기준으로 정리한다.

## 핵심 결론
- 정답은 하나가 아니라 **하이브리드**:
  - 개발/원인분석: Android Studio Profiler + Perfetto + Macrobenchmark
  - 출시 후 실사용 관측: APM(Firebase Performance 등) 또는 GCP 기반 관측
- `Trace.beginSection/endSection` 데이터는 기본적으로 **로컬(기기/캡처 파일)** 기반이다.
- 자동 서버 전송은 별도 구성 없으면 일어나지 않는다.

## APM이란
APM(Application Performance Monitoring)은
실사용자 앱 성능을 수집해서 대시보드/알림으로 보여주는 도구다.

예시:
- Firebase Performance Monitoring
- Datadog/Sentry/New Relic 모바일 APM

## 서버에 심어야 하나?
- 일반적 방식: **앱 SDK가 지표를 전송**하고, 백엔드는 APM 서비스가 담당.
- 자체 운영 방식: 앱 -> 우리 API -> GCP(BigQuery/Monitoring)로 직접 저장/집계.

## Android Studio vs Firebase vs GCP
- Android Studio: 로컬 디버깅/원인 분석
- Firebase: 실사용 지표 모니터링(콘솔)
- GCP: Firebase export(BigQuery) 또는 자체 수집 파이프라인으로 대시보드 구성

## 먼저 볼 지표(Flit 최소 5개)
1. Startup(Cold Start)
2. capture_save_completion
3. ai_classification_completion
4. local_search_execution / semantic_search_execution
5. Scroll Jank(Frame drop)

운영 시 평균보다 **p95** 중심으로 본다.

## ADB 연결 끊김 이슈 정리
- Profiler/System Trace는 실시간 연결 기반이라 ADB가 끊기면 뷰어에서 즉시 관측이 어려워진다.
- 저장 전 끊기면 trace 유실 가능성이 있다.
- Macrobenchmark는 완료 시 결과가 남지만, 실행 중 ADB 끊기면 해당 런 실패/유실 가능.

## 출시 전 점진 최적화 워크플로우
1. Baseline(v0) 측정(동일 기기/조건, 5~10회)
2. 지표별 목표치 정의(예: p95 상한)
3. 변경 전/후 동일 시나리오 측정
4. 성능 로그 기록(Before/After, 개선율)
5. CI에서 benchmark 결과 누적 및 회귀 감지
6. 출시 직전 저사양 실기기 포함 재검증
7. 출시 후 APM으로 실사용 p95/p99 추적

## 측정 품질 체크리스트
- 기기/OS/빌드 타입 고정
- 발열/배터리 상태 안정
- 네트워크 조건 고정(가능하면)
- 워밍업 1~2회 제외
- 평균 + p95 같이 기록
