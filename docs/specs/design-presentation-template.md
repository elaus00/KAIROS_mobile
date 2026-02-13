# 디자인 프레젠테이션 HTML 템플릿

디자인 시안을 HTML로 제시할 때 사용하는 표준 양식.

## 구조

```
<섹션 라벨> — 대문자 + 자간 2px
<제목 h2> — 18px Bold
<카드 그리드> — grid-template-columns: repeat(N, 1fr), gap: 20px
  <카드>
    <디스플레이> — 라이트/다크 프리뷰
    <인포> — 번호, 제목, 설명
  </카드>
```

## 핵심 CSS 규칙

### 배경
- **페이지 배경**: `#F5F5F5`
- **카드**: `#fff`, border: `1px solid #eee`, border-radius: `16px`
- **다크 프리뷰**: `#0A0A0A`

### 타이포그래피
- **시스템 폰트**: `-apple-system, BlinkMacSystemFont, 'SF Pro Display', sans-serif`
- **섹션 라벨**: 10px, weight 600, color `#aaa`, uppercase, letter-spacing `2px`
- **제목 h2**: 18px, weight 600, center
- **카드 번호**: 10px, weight 600, color `#bbb`, letter-spacing `1px`
- **카드 제목**: 13px, weight 600
- **카드 설명**: 11px, color `#888`, line-height `1.5`

### 카드 레이아웃
```css
.card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #eee;
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.card:hover {
  box-shadow: 0 8px 32px rgba(0,0,0,0.08);
}
```

### 디스플레이 영역
```css
.display {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120px;   /* 기본 */
  padding: 24px;
}
.display.tall { height: 160px; }
.display.short { height: 90px; }
.display.dark { background: #0A0A0A; }
```

### 인포 영역
```css
.info {
  padding: 14px 18px;
  border-top: 1px solid #f0f0f0;
}
```

### 그리드 시스템
```css
.grid-2 { grid-template-columns: repeat(2, 1fr); gap: 20px; }
.grid-3 { grid-template-columns: repeat(3, 1fr); gap: 20px; }
.grid-4 { grid-template-columns: repeat(4, 1fr); gap: 20px; }
.grid-6 { grid-template-columns: repeat(6, 1fr); gap: 20px; }
.card.span-2 { grid-column: span 2; }
.card.span-3 { grid-column: span 3; }
```

### 배지
```css
.badge {
  display: inline-block;
  font-size: 9px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 6px;
  vertical-align: middle;
}
.badge.rec { background: #111; color: #fff; }   /* 추천 */
.badge.alt { background: #eee; color: #666; }   /* 대안 */
```

## 섹션 유형

### 1. 후보 비교 (Candidate Comparison)
- 3열 그리드, 각 카드에 라이트+다크 프리뷰
- 추천 항목에 `.badge.rec` 태그

### 2. 아이콘 그리드 (Icon Grid)
- 6열 그리드, icon-pair로 라이트/다크 페어 표시
- 아이콘 크기: 64×64px, border-radius: 14px

### 3. 사이즈 테스트 (Scale Test)
- flex row, 큰 것부터 작은 것까지 나열
- 각 사이즈 아래에 px 라벨

### 4. 사용 시나리오 (Usage Scenario)
- 스플래시: aspect-ratio 9/19.5
- 앱바: nav-bar + placeholder content
- 스토어 리스팅: icon + name + subtitle + button

### 5. 홈스크린 시뮬레이션 (Home Screen)
- gradient 배경, 4열 아이콘 그리드
- 경쟁 앱과 나란히 배치

### 6. 최종 세트 (Final Pair)
- 2열, 워드마크 + 아이콘을 같은 카드에 나란히 표시

## 색상 팔레트

| 용도 | 값 |
|------|------|
| 페이지 배경 | `#F5F5F5` |
| 카드 배경 | `#FFFFFF` |
| 카드 보더 | `#EEEEEE` |
| 다크 프리뷰 | `#0A0A0A` |
| 텍스트 기본 | `#111111` |
| 텍스트 보조 | `#888888` |
| 텍스트 약함 | `#AAAAAA` |
| 구분선 | `#F0F0F0` |
| 호버 섀도 | `rgba(0,0,0,0.08)` |

## 템플릿 보일러플레이트

```html
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>[제목]</title>
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body {
    font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', sans-serif;
    background: #F5F5F5; color: #111; padding: 40px 20px 100px;
  }
  h1 { text-align: center; font-size: 26px; font-weight: 600; margin-bottom: 4px; }
  .subtitle { text-align: center; color: #888; font-size: 13px; margin-bottom: 56px; }
  .section { max-width: 1200px; margin: 0 auto 64px; }
  .section-label {
    font-size: 10px; font-weight: 600; color: #aaa; text-transform: uppercase;
    letter-spacing: 2px; text-align: center; margin-bottom: 6px;
  }
  h2 { font-size: 18px; font-weight: 600; margin-bottom: 28px; text-align: center; }
  .grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
  .card {
    background: #fff; border-radius: 16px; border: 1px solid #eee;
    overflow: hidden; transition: box-shadow 0.2s;
  }
  .card:hover { box-shadow: 0 8px 32px rgba(0,0,0,0.08); }
  .display {
    display: flex; align-items: center; justify-content: center;
    height: 120px; padding: 24px;
  }
  .display.dark { background: #0A0A0A; }
  .info { padding: 14px 18px; border-top: 1px solid #f0f0f0; }
  .num { font-size: 10px; font-weight: 600; color: #bbb; letter-spacing: 1px; margin-bottom: 2px; }
  .title { font-size: 13px; font-weight: 600; margin-bottom: 3px; }
  .desc { font-size: 11px; color: #888; line-height: 1.5; }
  .badge {
    display: inline-block; font-size: 9px; font-weight: 600; padding: 2px 6px;
    border-radius: 4px; margin-left: 6px; vertical-align: middle;
  }
  .badge.rec { background: #111; color: #fff; }
  .badge.alt { background: #eee; color: #666; }
</style>
</head>
<body>

<h1>[프로젝트명] — [주제]</h1>
<p class="subtitle">[설명]</p>

<div class="section">
  <div class="section-label">[섹션 라벨]</div>
  <h2>[섹션 제목]</h2>
  <div class="grid-3">
    <div class="card">
      <div class="display">[내용]</div>
      <div class="display dark">[다크 내용]</div>
      <div class="info">
        <div class="num">[번호]</div>
        <div class="title">[제목] <span class="badge rec">추천</span></div>
        <div class="desc">[설명]</div>
      </div>
    </div>
  </div>
</div>

</body>
</html>
```
