# Material Design 커스텀 현황 점검 보고서

> 작성일: 2026-02-08
> 대상: KAIROS Mobile — presentation/ 전체 UI 코드
> 목적: Material3 기본 컴포넌트의 커스텀 적용 현황 파악 및 브랜딩 일관성 점검

---

## 1. 테마 시스템 현황 요약

### 1.1 커스텀 색상 시스템 (잘 구축됨)

| 항목 | 상태 | 비고 |
|------|------|------|
| KairosColors 전용 색상 클래스 | 완료 | 19개 시맨틱 색상 토큰 (라이트/다크) |
| Material3 ColorScheme 매핑 | 완료 | KairosLight/Dark → lightColorScheme/darkColorScheme |
| CompositionLocal 제공 | 완료 | `LocalKairosColors` + `KairosTheme.colors` |
| 무채색 모노크롬 기조 | 완료 | PRD v4.0 준수 |

### 1.2 타이포그래피 (부분 커스텀)

| 항목 | 상태 | 비고 |
|------|------|------|
| bodyLarge | 커스텀 | 16sp, lineHeight 24sp |
| 기타 스타일 (titleLarge, labelSmall 등) | **기본값** | 주석 처리되어 Material3 기본값 사용 중 |

### 1.3 Shape 시스템

| 항목 | 상태 | 비고 |
|------|------|------|
| MaterialTheme shapes | **기본값** | Theme.kt에서 shapes 파라미터 미지정 |
| 개별 컴포넌트 | 인라인 지정 | RoundedCornerShape(8~24.dp) 하드코딩 |

---

## 2. 파일별 상세 점검 결과

### 심각도 기준
- **High**: 앱 브랜딩과 명백히 불일치하거나 UX 품질을 저하시키는 항목
- **Medium**: 커스텀은 없지만 현재 테마와 어울리지 않을 수 있는 항목
- **Low**: 기본값이 허용되나 브랜딩 일관성을 위해 커스텀 권장

---

### 2.1 다이얼로그 (AlertDialog)

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| `CreateFolderDialog.kt` | AlertDialog | 배경색, 제목/버튼 색상 모두 기본 M3 팔레트 | **High** | containerColor, titleContentColor, textContentColor를 KairosTheme.colors 적용 |
| `RenameFolderDialog.kt` | AlertDialog | 위와 동일 | **High** | 위와 동일 |
| `SettingsScreen.kt:528` | AlertDialog (CalendarCodeExchangeDialog) | 배경색, 텍스트 색상 기본값 | **High** | containerColor, titleContentColor 적용 |
| `SettingsScreen.kt:561` | AlertDialog (CalendarTokenSaveDialog) | 위와 동일 | **High** | 위와 동일 |
| `SettingsScreen.kt:282` | AlertDialog (calendarAuthMessage) | 배경색, 텍스트 색상 기본값 | **High** | containerColor, titleContentColor 적용 |

**상세 설명**: 총 5개 AlertDialog가 M3 기본 색상(보라색 계열 Primary)으로 표시됨. 앱의 모노크롬 디자인 기조와 충돌. 특히 title, confirmButton/dismissButton의 TextButton 색상이 M3 기본 Primary(보라색)를 사용.

---

### 2.2 OutlinedTextField

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| `CreateFolderDialog.kt:31` | OutlinedTextField | colors 미지정 — M3 기본 보더/라벨 색상 | **High** | OutlinedTextFieldDefaults.colors() 커스텀 |
| `RenameFolderDialog.kt:31` | OutlinedTextField | 위와 동일 | **High** | 위와 동일 |
| `SettingsScreen.kt:533-546` | OutlinedTextField x2 (CodeExchangeDialog) | colors 미지정 | **High** | focusedBorderColor, labelColor 등 적용 |
| `SettingsScreen.kt:575-595` | OutlinedTextField x3 (TokenSaveDialog) | colors 미지정 | **High** | 위와 동일 |
| `NoteDetailScreen.kt:148` | OutlinedTextField (제목) | **커스텀 적용됨** | - | 참고 모범 사례 |
| `NoteDetailScreen.kt:196` | OutlinedTextField (본문) | **커스텀 적용됨** | - | 참고 모범 사례 |

**상세 설명**: NoteDetailScreen은 `OutlinedTextFieldDefaults.colors()`를 올바르게 적용한 모범 사례. 반면 다이얼로그 내 5개 OutlinedTextField는 기본 M3 색상(보라색 포커스 보더, 보라색 라벨)을 사용.

---

### 2.3 TextButton

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| `CreateFolderDialog.kt:42,48` | TextButton (생성/취소) | 텍스트 색상이 M3 기본 Primary | **Medium** | contentColor를 colors.accent 또는 colors.text로 지정 |
| `RenameFolderDialog.kt:42,50` | TextButton (변경/취소) | 위와 동일 | **Medium** | 위와 동일 |
| `SettingsScreen.kt:549-556` | TextButton (요청/취소) | 위와 동일 | **Medium** | 위와 동일 |
| `SettingsScreen.kt:599-606` | TextButton (저장/취소) | 위와 동일 | **Medium** | 위와 동일 |
| `SettingsScreen.kt:288` | TextButton (확인) | 위와 동일 | **Medium** | 위와 동일 |

**상세 설명**: 총 10개 TextButton이 M3 기본 Primary 색상으로 텍스트 표시. Material3 ColorScheme에서 primary를 KairosLight.accent(#111111)로 매핑했으므로, 실제로는 검정색으로 표시될 수 있으나 명시적 커스텀이 없어 테마 변경 시 불안정.

---

### 2.4 OutlinedButton

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| `TrashScreen.kt:223` | OutlinedButton (복원) | border 기본값 (M3 outline 색상) | **Medium** | border = BorderStroke(1.dp, colors.accent) 지정 |
| `TrashScreen.kt:236` | OutlinedButton (완전 삭제) | border 기본값 | **Medium** | border = BorderStroke(1.dp, colors.danger) 지정 |

**상세 설명**: contentColor는 커스텀 적용했으나 border 색상은 M3 기본 outline을 사용. colors.accent/danger에 맞는 보더 색상 지정 권장.

---

### 2.5 Card

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| `TrashScreen.kt:188` | Card | elevation, shape 기본값 | **Low** | elevation=0.dp, shape=RoundedCornerShape(12.dp) 적용하여 앱 디자인 기조(플랫)와 일치시킬 것 |

**상세 설명**: 앱 전체적으로 Card를 거의 사용하지 않고 Box+clip+background 조합으로 카드를 구성. TrashScreen만 M3 Card를 사용하며 containerColor는 커스텀했으나 elevation/shape은 기본값.

---

### 2.6 DropdownMenu / DropdownMenuItem

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| `NotesScreen.kt:250-268` | DropdownMenu + DropdownMenuItem | 메뉴 배경색 기본값 | **Medium** | containerColor를 colors.card로 지정 |
| `ClassificationDropdown.kt:65-86` | DropdownMenu + DropdownMenuItem | 메뉴 배경은 colors.card 적용, 아이템 기본 스타일 | **Low** | DropdownMenuItem에 hover/pressed 색상 추가 가능 |

**상세 설명**: ClassificationDropdown은 배경색을 커스텀했으나, NotesScreen의 폴더 컨텍스트 메뉴는 기본 Surface 색상 사용.

---

### 2.7 ModalBottomSheet

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| `NoteDetailScreen.kt:437` | ModalBottomSheet | containerColor 커스텀 적용됨 | - | dragHandle 색상, scrimColor 커스텀 가능 |

**상세 설명**: containerColor는 올바르게 적용. dragHandle(M3 기본 회색)과 scrimColor는 기본값이나 현재 디자인과 큰 충돌 없음.

---

### 2.8 TopAppBar

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| `SettingsScreen.kt:57` | TopAppBar | containerColor 커스텀 적용 | - | 양호 |
| `CaptureDetailScreen.kt:49` | TopAppBar | containerColor 커스텀 적용 | - | 양호 |
| `NoteDetailScreen.kt:54` | TopAppBar | containerColor 커스텀 적용 | - | 양호 |
| `LegalWebViewScreen.kt:37` | TopAppBar | containerColor, titleContentColor, navigationIconContentColor 커스텀 | - | 양호 |

**상세 설명**: 모든 TopAppBar가 적절히 커스텀됨. 양호.

---

### 2.9 Switch

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| `SettingsScreen.kt:457` | Switch | **커스텀 적용됨** | - | SwitchDefaults.colors() 6개 색상 모두 지정. 모범 사례 |

---

### 2.10 CircularProgressIndicator

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| `SettingsScreen.kt:512-514` | CircularProgressIndicator (CalendarActionItem) | color 미지정 — M3 기본 Primary | **Low** | color = colors.textMuted 지정 |
| 기타 모든 CPI | CircularProgressIndicator | **color 커스텀 적용됨** | - | 양호 |

**상세 설명**: 대부분의 CircularProgressIndicator가 color를 명시적으로 지정. CalendarActionItem의 비활성 상태 로딩만 기본값 사용.

---

### 2.11 Scaffold

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| 전체 | Scaffold | containerColor 커스텀 적용 | - | 양호. 모든 Scaffold가 colors.background 사용 |

---

### 2.12 HorizontalDivider

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| 전체 | HorizontalDivider | **커스텀 적용됨** | - | color, thickness 모두 지정. 양호 |

---

### 2.13 BadgedBox / Badge

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| `CaptureContent.kt:228` | BadgedBox + Badge | containerColor, contentColor 커스텀 적용 | - | 양호 |

---

### 2.14 IconButton

| 파일 | 컴포넌트 | 기본값 사용 내용 | 심각도 | 권장 커스텀 |
|------|----------|------------------|--------|-------------|
| 전체 | IconButton | ripple/pressed 색상 기본값 | **Low** | M3 기본 ripple 사용 중. colors 파라미터로 커스텀 가능하나 현재 큰 문제 없음 |

---

### 2.15 하드코딩된 색상값

| 파일 | 위치 | 하드코딩 색상 | 심각도 | 권장 변경 |
|------|------|---------------|--------|-----------|
| `CaptureDetailScreen.kt:327` | CalendarSyncSection | `Color(0xFF4CAF50)`, `Color(0xFFFFA726)`, `Color(0xFFEF5350)` | **Medium** | colors.success, colors.warning, colors.danger 토큰 사용 |
| `ScheduleTimeline.kt:286` | SyncStatusBadge | `Color(0xFF4CAF50)`, `Color(0xFFFFA726)`, `Color(0xFFEF5350)` | **Medium** | 위와 동일 |
| `OnboardingScreen.kt:259,264` | OnboardingPageGoogle | `Color(0xFF4CAF50)` | **Medium** | colors.success 사용 |
| `KairosBottomNav.kt:57` | shadow | `Color.Black.copy(alpha = 0.15f)` | **Low** | 그림자 전용이므로 허용 가능 |

**상세 설명**: KairosTheme.colors에 이미 success(#10B981), warning(#F59E0B), danger(#EF4444) 토큰이 정의되어 있으나, 일부 화면에서 다른 색상값(#4CAF50, #FFA726, #EF5350)을 하드코딩. 색상 불일치 발생.

---

### 2.16 하드코딩된 dp/sp 값

| 패턴 | 사용 빈도 | 심각도 | 비고 |
|------|-----------|--------|------|
| fontSize 직접 지정 (10~40.sp) | 전체 파일 | **Low** | Typography 토큰 미사용. MaterialTheme.typography 대신 직접 sp 값 지정 |
| padding 직접 지정 (2~48.dp) | 전체 파일 | **Low** | 일관된 간격 시스템 없이 하드코딩 |
| RoundedCornerShape 직접 지정 (4~24.dp) | 전체 파일 | **Low** | MaterialTheme.shapes 미사용. 인라인 하드코딩 |

**상세 설명**: 앱 전체에서 Typography 토큰과 Shape 토큰을 사용하지 않고 직접 sp/dp 값을 지정. 현재 일관성이 유지되고 있으나, 향후 디자인 시스템 변경 시 모든 파일을 수동 수정해야 함.

---

## 3. 요약 통계

### 3.1 심각도별 분류

| 심각도 | 건수 | 주요 항목 |
|--------|------|-----------|
| **High** | **10건** | AlertDialog 5개 기본 스타일, OutlinedTextField 5개 기본 색상 |
| **Medium** | **10건** | TextButton 5개, OutlinedButton 2개, DropdownMenu 1개, 하드코딩 색상 3개소 |
| **Low** | **5건** | Card 1개, CPI 1개, IconButton ripple, Typography/Shape/Spacing 시스템 부재 |

### 3.2 컴포넌트별 커스텀 적용률

| 컴포넌트 | 사용 수 | 커스텀 적용 | 기본값 사용 | 적용률 |
|----------|---------|-------------|-------------|--------|
| Scaffold | 6 | 6 | 0 | 100% |
| TopAppBar | 4 | 4 | 0 | 100% |
| Switch | 1 | 1 | 0 | 100% |
| HorizontalDivider | 5+ | 5+ | 0 | 100% |
| BadgedBox/Badge | 1 | 1 | 0 | 100% |
| ModalBottomSheet | 1 | 1 | 0 | 100% |
| OutlinedTextField | 7 | 2 | **5** | 29% |
| AlertDialog | 5 | 0 | **5** | 0% |
| TextButton | 10 | 0 | **10** | 0% |
| OutlinedButton | 2 | 1 (부분) | **1 (부분)** | 50% |
| Card | 1 | 1 (부분) | **1 (부분)** | 50% |
| DropdownMenu | 2 | 1 | **1** | 50% |
| CircularProgressIndicator | 10+ | 9+ | **1** | 90% |

### 3.3 전체 평가

- **커스텀 색상 시스템**: 우수. KairosTheme.colors를 통해 19개 시맨틱 토큰 일관 적용
- **Material3 ColorScheme 매핑**: 우수. lightColorScheme/darkColorScheme 모두 Kairos 색상으로 매핑
- **Typography 시스템**: 미흡. bodyLarge만 커스텀, 나머지는 M3 기본값. 실제 사용 시 직접 sp 지정으로 우회
- **Shape 시스템**: 미흡. MaterialTheme shapes 미설정, 인라인 하드코딩
- **다이얼로그 계열**: 미흡. AlertDialog, TextButton이 전면적으로 기본 스타일

---

## 4. 우선순위별 개선 권장사항

### P0 (즉시 개선 — High)

1. **AlertDialog 5개 커스텀화**
   - `containerColor = colors.card`
   - `titleContentColor = colors.text`
   - `textContentColor = colors.textSecondary`
   - 대상: CreateFolderDialog, RenameFolderDialog, CalendarCodeExchangeDialog, CalendarTokenSaveDialog, calendarAuthMessage

2. **OutlinedTextField 5개 커스텀화**
   - `OutlinedTextFieldDefaults.colors(focusedBorderColor, unfocusedBorderColor, focusedTextColor, unfocusedTextColor, cursorColor, focusedLabelColor, unfocusedLabelColor)` 적용
   - NoteDetailScreen의 구현을 참조 패턴으로 사용

### P1 (단기 개선 — Medium)

3. **하드코딩 색상 제거**
   - `Color(0xFF4CAF50)` → `colors.success`
   - `Color(0xFFFFA726)` → `colors.warning`
   - `Color(0xFFEF5350)` → `colors.danger`
   - 대상: CaptureDetailScreen, ScheduleTimeline, OnboardingScreen

4. **TextButton 색상 명시화**
   - `colors = ButtonDefaults.textButtonColors(contentColor = colors.accent)` 적용

5. **OutlinedButton border 커스텀**
   - TrashScreen의 복원/삭제 버튼에 border 색상 지정

6. **DropdownMenu 배경색 통일**
   - NotesScreen 폴더 메뉴에 `containerColor = colors.card` 적용

### P2 (중기 개선 — Low)

7. **Typography 토큰 체계화**
   - Type.kt에 titleLarge, titleMedium, bodyMedium, labelSmall 등 주요 스타일 정의
   - 공통 패턴 추출: 화면 제목(24sp/SemiBold), 섹션 라벨(12sp/SemiBold), 본문(15sp), 보조(13sp), 힌트(11sp)

8. **Shape 토큰 정의**
   - Theme.kt에 MaterialTheme shapes 설정 또는 KairosShapes 객체 정의
   - small=8.dp, medium=12.dp, large=20.dp 등

9. **Spacing 토큰 정의**
   - 공통 패딩 값 정의 (horizontal=16/20dp, vertical=12/16dp 등)

10. **Card elevation/shape 통일**
    - TrashScreen Card에 elevation=0.dp + shape=RoundedCornerShape(12.dp) 적용

---

## 5. 잘 적용된 사례 (참조 패턴)

| 패턴 | 파일 | 설명 |
|------|------|------|
| OutlinedTextField 커스텀 | `NoteDetailScreen.kt:158,213` | focusedBorderColor, unfocusedBorderColor, textColor, cursorColor 모두 지정 |
| Switch 커스텀 | `SettingsScreen.kt:461` | checked/unchecked 6개 색상 모두 KairosTheme.colors 적용 |
| TopAppBar 커스텀 | `LegalWebViewScreen.kt:53` | containerColor, titleContentColor, navigationIconContentColor 지정 |
| ModalBottomSheet 커스텀 | `NoteDetailScreen.kt:437` | containerColor = colors.background |
| Scaffold 커스텀 | 전체 | containerColor = colors.background 일관 적용 |
| KairosChip 래퍼 | `KairosChip.kt` | Material3 기본 Chip 대신 Box 기반 자체 컴포넌트로 완전 커스텀 |
| KairosBottomNav 커스텀 | `KairosBottomNav.kt` | BottomAppBar 대신 Box+Row 기반 자체 컴포넌트 |

---

## 6. 결론

KAIROS Mobile은 **커스텀 색상 시스템(KairosTheme.colors)이 잘 구축**되어 있으며, Scaffold/TopAppBar/HorizontalDivider 등 주요 레이아웃 컴포넌트는 적절히 커스텀되어 있다. 그러나 **AlertDialog, OutlinedTextField, TextButton 등 입력/인터랙션 컴포넌트**에서 M3 기본 스타일이 그대로 노출되어 모노크롬 디자인 기조와 충돌한다.

가장 시급한 개선은 **P0의 AlertDialog/OutlinedTextField 10건**이며, 이는 사용자에게 직접 보이는 UI 요소이므로 앱 전체 브랜딩 일관성에 큰 영향을 미친다. NoteDetailScreen과 SettingsScreen(Switch)에 이미 모범 패턴이 존재하므로, 해당 패턴을 나머지 컴포넌트에 확산 적용하면 효율적으로 개선할 수 있다.
