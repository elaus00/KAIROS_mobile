# Mobile Design System

모바일 우선 디자인 사고와 iOS/Android 앱 의사결정을 위한 스킬입니다.

## 핵심 철학

**"Touch-first. Battery-conscious. Platform-respectful. Offline-capable."**

모바일은 작은 데스크톱이 아닙니다. 모바일 특유의 제약과 가능성을 이해하고 설계해야 합니다.

## 필수 참조 원칙

### 터치 심리학 (Touch Psychology)

**최소 터치 타겟:**
- iOS: 44px × 44px 최소
- Android: 48dp × 48dp 최소
- 타겟 간 간격: 8-12px 최소

**Thumb Zone (엄지 영역):**
- 주요 CTA는 화면 하단 엄지가 닿는 영역에 배치
- 자주 사용하는 버튼은 접근하기 쉬운 위치에

**제스처 지원:**
- 스와이프, 핀치, 롱프레스 등 자연스러운 제스처
- 햅틱 피드백으로 사용자 경험 향상

### 성능 최적화 (Mobile Performance)

**금지된 안티패턴:**
- ❌ ScrollView로 긴 리스트 렌더링 → ✅ LazyColumn/LazyRow 사용
- ❌ 인라인 람다 함수 남용 → ✅ remember + derivedStateOf 사용
- ❌ 인덱스 기반 키 → ✅ 고유하고 안정적인 ID 사용
- ❌ 프로덕션에 Log.d 남기기 → ✅ BuildConfig.DEBUG로 조건부 로깅

**60FPS 목표:**
- Jetpack Compose에서 derivedStateOf 활용
- 리컴포지션 최소화
- remember와 memoization 적극 활용

### 플랫폼별 차별화

**통합 (모든 플랫폼 동일):**
- 비즈니스 로직
- 데이터 레이어
- 핵심 기능

**차별화 (플랫폼별 다르게):**
- Navigation 패턴 (iOS: 탭, Android: 드로어/바텀 네비)
- 제스처 (iOS: 뒤로가기 스와이프, Android: 뒤로가기 버튼)
- 아이콘 (iOS: SF Symbols, Android: Material Icons)
- 타이포그래피 (iOS: SF Pro, Android: Roboto)
- 모달/다이얼로그 스타일

### Android Material Design 3 준수

**컬러 시스템:**
- Primary, Secondary, Tertiary 컬러 정의
- Dynamic Color 지원 고려
- 다크모드 대응

**컴포넌트:**
- Material 3 디자인 토큰 사용
- Elevation과 Surface 변형 활용
- 일관된 패딩과 마진 (8dp 그리드 시스템)

### 오프라인 지원

**필수 구현:**
- 오프라인 상태 감지 및 UI 표시
- 로컬 캐싱 (Room Database)
- 오프라인 큐잉 (WorkManager)
- 재시도 메커니즘

**사용자 피드백:**
- 로딩 상태 명확히 표시
- 에러 상태 + 재시도 버튼
- 오프라인 모드 안내

### 보안 필수사항

**절대 금지:**
- SharedPreferences에 토큰 저장 → EncryptedSharedPreferences 사용
- 코드에 API 키 하드코딩 → BuildConfig 또는 환경 변수
- 민감 데이터 로깅
- SSL 인증서 검증 우회

## 개발 전 체크리스트

코드 작성 전 반드시 확인:

1. **타겟 플랫폼**: [ ] iOS [ ] Android [ ] 둘 다
2. **프레임워크**: [ ] Jetpack Compose [ ] React Native [ ] Flutter [ ] Native
3. **적용할 3가지 원칙**:
   - [ ] _______________
   - [ ] _______________
   - [ ] _______________
4. **피해야 할 2가지 안티패턴**:
   - [ ] _______________
   - [ ] _______________
5. **모든 필수 파일 리뷰 완료**: [ ]

## Jetpack Compose 최적화 예시

```kotlin
// ❌ 나쁜 예: 매 리컴포지션마다 새로운 람다 생성
LazyColumn {
    items(list) { item ->
        ItemRow(item = item, onClick = { handleClick(item) })
    }
}

// ✅ 좋은 예: remember와 key 사용
LazyColumn {
    items(
        items = list,
        key = { it.id }  // 안정적인 키
    ) { item ->
        val onClick = remember(item.id) {
            { handleClick(item) }
        }
        ItemRow(item = item, onClick = onClick)
    }
}
```

## 배터리 고려 디자인

- 불필요한 위치 업데이트 최소화
- 백그라운드 작업 배칭
- WorkManager로 지연 가능한 작업 스케줄링
- 애니메이션 과도하게 사용하지 않기

## 접근성 (Accessibility)

- 모든 터치 타겟은 최소 크기 준수
- Talkback/VoiceOver 지원
- 충분한 색상 대비 (WCAG AA 이상)
- 텍스트 크기 조절 대응

---

이 스킬을 사용하여 모바일 앱을 구현할 때는 항상 위 원칙들을 먼저 검토하고, 플랫폼별 특성을 존중하며, 사용자 경험을 최우선으로 고려하세요.
