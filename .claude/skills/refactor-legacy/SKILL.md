# Refactor Legacy Skill

레거시 코드/디자인 시스템을 제거하고 새로운 시스템으로 마이그레이션합니다.
**핵심**: 전체 프로젝트 범위(main + test + androidTest)에서 누락 없이 100% 완수합니다.

## 사용법

```
/refactor-legacy
/refactor-legacy --dry-run    # 변경 대상만 분석
```

## 작업 프로세스

### Phase 1: 영향 범위 분석 (반드시 먼저 수행)

```markdown
## 삭제/변경 대상 파일 목록
- [ ] 파일명: _______________
- [ ] 파일명: _______________

## 검색 패턴 목록
- [ ] 레거시 색상명: _______________
- [ ] 레거시 modifier: _______________
- [ ] 레거시 함수/클래스: _______________
```

### Phase 2: 전체 프로젝트 검색 (필수)

**검색 범위** - 반드시 전체 프로젝트 (`**/*.kt`)
```bash
# ❌ 잘못된 검색 (main만)
grep -r "Pattern" app/src/main/

# ✅ 올바른 검색 (전체 프로젝트)
grep -r "Pattern" --include="*.kt" .
```

**필수 검색 항목**:
1. 레거시 패턴 직접 참조
2. 삭제된 파일의 import 문
3. 삭제된 파일명 문자열 참조
4. 하드코딩된 값 (Color, dp 등)

### Phase 3: 작업 실행

1. 새 파일 생성 (대체용)
2. 기존 파일 수정
3. 레거시 파일 삭제
4. import 경로 업데이트

### Phase 4: 검증 (체크리스트)

```markdown
## 최종 검증 체크리스트

### 레거시 참조 검색 (전체 프로젝트)
- [ ] `grep -r "레거시패턴" --include="*.kt" .` → 0건

### 삭제된 파일 import 검색
- [ ] `grep -r "import.*삭제된파일" --include="*.kt" .` → 0건

### 하드코딩된 값 검색
- [ ] `grep -r "Color(0x" --include="*.kt" app/src/main/java/**/presentation/` → 0건

### 사용하지 않는 import 검색
- [ ] 새로 수정한 파일들에서 unused import 확인

### 테스트 파일 확인
- [ ] `app/src/test/**/*.kt` 검색 완료
- [ ] `app/src/androidTest/**/*.kt` 검색 완료
```

## 검색 패턴 템플릿

### Android/Compose 레거시 제거 시

```bash
# 색상 관련
grep -r "GlassCard|AiryGlass|TextPrimary|PrimaryNavy" --include="*.kt" .

# Modifier 관련
grep -r "glassPanelThemed|glassCardThemed|glassButton" --include="*.kt" .

# 애니메이션/배경 관련
grep -r "AnimatedBackground|AnimatedGlass" --include="*.kt" .

# 삭제된 파일 import
grep -r "import.*삭제된파일명" --include="*.kt" .

# 하드코딩된 Color
grep -r "Color(0x[A-Fa-f0-9]+)" --include="*.kt" app/src/main/java/**/presentation/
```

## 흔한 누락 케이스

| 누락 케이스 | 원인 | 해결 |
|------------|------|------|
| 테스트 파일 | main 폴더만 검색 | `**/*.kt` 사용 |
| unused import | 검사 항목 누락 | Phase 4에서 체크 |
| 삭제된 파일 import | 파일 삭제 후 미검색 | 삭제 직후 전체 검색 |
| 하드코딩된 값 | presentation만 검색 | 필요시 전체 검색 |

## 주의사항

- **검색 범위 확장**: 항상 `**/*.kt`로 전체 프로젝트 검색
- **삭제 전 검색**: 파일 삭제/이름 변경 전에 참조 검색
- **삭제 후 검색**: 파일 삭제 후 import 검색 재실행
- **테스트 파일 포함**: test, androidTest 폴더 필수 포함
- **unused import**: 리팩토링 후 사용하지 않는 import 정리

## 실행 예시

```
User: 레거시 Glassmorphism 디자인 시스템 제거해줘

Claude:
[Phase 1] 영향 범위 분석 중...
- 삭제 대상: GlassModifiers.kt, AnimatedBackground.kt, GlassSearchBar.kt
- 검색 패턴: GlassCard, AiryGlass, glassPanelThemed, AnimatedGlassBackground

[Phase 2] 전체 프로젝트 검색 (main + test + androidTest)...
- 레거시 참조: 17개 파일 발견
- import 참조: 1개 파일 발견 (androidTest/SearchScreenTest.kt)

[Phase 3] 작업 실행...
- SearchBar.kt 생성 (GlassSearchBar.kt 대체)
- 17개 파일 KairosTheme.colors로 마이그레이션
- 테스트 파일 import 수정

[Phase 4] 검증...
✅ 레거시 참조: 0건
✅ 삭제된 파일 import: 0건
✅ 하드코딩된 Color: 0건
✅ unused import: 정리 완료

작업 완료 (1회차 검증 통과)
```