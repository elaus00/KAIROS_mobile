# Flit.

**떠오른 순간, 바로 던지면 끝** — 멀티모달 캡처 앱

Flit.은 텍스트, 이미지, 음성 등 다양한 형태의 생각을 빠르게 캡처하고, AI가 자동으로 분류·정리해주는 Android 앱입니다.

## 문서 안내

| 섹션 | 설명 |
|------|------|
| [제품 방향](direction/flit_prd_v10.md) | PRD, 철학 및 설계 원칙 |
| [명세서](specs/functional_spec.md) | 기능, 데이터모델, API, 디자인 가이드 |
| [개발 계획](plan/ui-review-plan.md) | UI 검토안, Phase 4 연구 |
| [인사이트](insights/architecture_conventions.md) | 아키텍처 컨벤션, 개발 회고 |
| [분석](analysis/ui-improvement-report.md) | UI 개선, Material Design 점검, QA 회고 |
| [성능](performance/README.md) | 성능 기준, 벤치마크, 리포트 템플릿 |

## 기술 스택

- **아키텍처**: Clean Architecture + MVVM
- **UI**: Jetpack Compose + Material 3
- **로컬 DB**: Room (v16)
- **DI**: Hilt
- **네트워크**: Retrofit + OkHttp
- **비동기**: Kotlin Coroutines + Flow
