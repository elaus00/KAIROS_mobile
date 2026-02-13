# Flit. UX 라이팅 감사 보고서

> 작성일: 2026-02-08
> 대상: Presentation 레이어 전체 (.kt 49개 파일 + 위젯 XML 3개)
> 목적: 앱 내 모든 사용자 노출 텍스트 전수 조사 + UX 라이팅 개선 제안

---

## 1. UX 라이팅 가이드라인

### 1.1 원칙

| 원칙 | 설명 |
|------|------|
| **명확성** | 사용자가 한 번에 이해할 수 있는 문장. 전문 용어 최소화 |
| **간결성** | 모바일은 화면이 작으므로 한 줄로 전달. CTA는 2-4글자 |
| **행동 유도** | 빈 상태/에러 메시지에는 반드시 "다음 행동"을 안내 |
| **일관성** | 같은 개념에 같은 단어 사용 (할 일/할일 혼용 금지) |
| **친근함** | 딱딱한 시스템 메시지가 아닌, 사람이 말하는 듯한 톤 |

### 1.2 한국어 톤앤매너 가이드

| 항목 | 기준 |
|------|------|
| **문체** | 해요체 (친근하면서 정중) — "저장되었습니다" 대신 "저장했어요" 권장 |
| **숫자** | 한글 표기 ("3분" 대신 "3분" — 아라비아 숫자 사용) |
| **영문** | 고유명사(Flit., Google Calendar)만 영문 유지, 나머지 한글화 |
| **존칭** | 사용자 지칭 불필요 (주어 생략이 자연스러운 한국어 특성 활용) |
| **부정** | "~할 수 없습니다" 대신 "~해주세요" (긍정적 안내 우선) |
| **느낌표** | 최소 사용 (온보딩/성공 시만 허용) |

### 1.3 유형별 가이드

#### 빈 상태 (Empty State)
- 왜 비어있는지 설명 + 어떻게 채울 수 있는지 안내
- 예: "아직 노트가 없어요" + "캡처하면 AI가 자동으로 분류해요"

#### 에러 메시지
- 무엇이 잘못되었는지 + 어떻게 해결하는지
- 예: "저장에 실패했어요. 다시 시도해주세요"

#### 버튼/액션 라벨
- 동사형 2-4글자: "저장", "삭제", "확인", "전송"
- 결과를 예측할 수 있는 라벨: "삭제" 대신 "휴지통으로 이동"

#### Placeholder
- 동사형 권유문: "떠오르는 생각을 적어보세요..."
- 말줄임표(...)로 입력 유도

---

## 2. 현재 텍스트 목록

### 2.1 홈 화면 (CaptureContent)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 상단바 제목 | `"Flit."` | 제목 | CaptureContent.kt:216 | - (브랜드명) |
| 벨 아이콘 | `"AI 분류 현황"` | contentDescription | CaptureContent.kt:245 | - |
| 히스토리 아이콘 | `"전체 기록"` | contentDescription | CaptureContent.kt:254 | - |
| 설정 아이콘 | `"설정"` | contentDescription | CaptureContent.kt:263 | - |
| 뱃지 텍스트 | `"99+"` / `"$unconfirmedCount"` | 뱃지 | CaptureContent.kt:236 | - |
| 입력 placeholder | `"떠오르는 생각을 캡처하세요..."` | placeholder | CaptureContent.kt:150 | - (좋은 문구) |
| 이미지 미리보기 | `"첨부 이미지"` | contentDescription | CaptureContent.kt:314 | - |
| 이미지 제거 | `"이미지 제거"` | contentDescription | CaptureContent.kt:330 | - |
| 이미지 첨부 | `"이미지 첨부"` | contentDescription | CaptureContent.kt:366 | - |
| 전송 버튼 | `"전송"` | contentDescription | CaptureContent.kt:397 | - |
| 스낵바 (저장 성공) | `"캡처가 저장되었습니다"` | snackbar | CaptureContent.kt:71 | P1: "캡처가 저장되었어요" |
| 이미지 캡처 기본 텍스트 | `"이미지 캡처"` | 시스템 텍스트 | CaptureViewModel.kt:113 | - |
| 에러 (저장 실패) | `"저장에 실패했습니다."` | 에러 | CaptureViewModel.kt:132 | P1: "저장에 실패했어요. 다시 시도해주세요" |

### 2.2 캘린더 화면 (CalendarScreen)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 삭제 스낵바 | `"삭제되었습니다"` | snackbar | CalendarScreen.kt:44 | P1: "삭제했어요" |
| 실행 취소 | `"실행 취소"` | 액션 라벨 | CalendarScreen.kt:45 | - |
| 캘린더 추가 성공 | `"캘린더에 추가되었습니다"` | snackbar | CalendarScreen.kt:54 | P1: "캘린더에 추가했어요" |
| 실행 취소 실패 | `"실행 취소에 실패했습니다."` | 에러 | CalendarViewModel.kt:235 | P1: "되돌리기에 실패했어요" |
| 삭제 실패 | `"삭제에 실패했습니다."` | 에러 | CalendarViewModel.kt:248 | P1: "삭제에 실패했어요" |
| 캘린더 동기화 실패 | `"캘린더 동기화에 실패했습니다."` | 에러 | CalendarViewModel.kt:265 | P1: "캘린더 동기화에 실패했어요" |
| 캘린더 거부 실패 | `"캘린더 동기화 거부에 실패했습니다."` | 에러 | CalendarViewModel.kt:283 | P1: "캘린더 동기화 거부에 실패했어요" |

### 2.3 캘린더 컴포넌트 (ScheduleTimeline / TaskList / CalendarCard)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 일정 섹션 헤더 | `"일정"` | 섹션 제목 | ScheduleTimeline.kt:47 | - |
| 일정 빈 상태 | `"일정이 없습니다"` | 빈 상태 | ScheduleTimeline.kt:343 | P2: "오늘은 일정이 없어요" |
| 캘린더 추가 버튼 | `"캘린더 추가"` | 버튼 | ScheduleTimeline.kt:256 | - |
| 무시 버튼 | `"무시"` | 버튼 | ScheduleTimeline.kt:270 | - |
| 동기화됨 배지 | `"동기화됨"` | 배지 | ScheduleTimeline.kt:293 | - |
| 제안 배지 | `"제안"` | 배지 | ScheduleTimeline.kt:294 | - |
| 실패 배지 | `"실패"` | 배지 | ScheduleTimeline.kt:295 | - |
| 거부됨 배지 | `"거부됨"` | 배지 | ScheduleTimeline.kt:296 | - |
| 종일 시간 텍스트 | `"종일"` | 시간 | ScheduleTimeline.kt:319 | - |
| 일정 삭제 아이콘 | `"일정 삭제"` | contentDescription | ScheduleTimeline.kt:224 | - |
| 할 일 섹션 헤더 | `"할 일"` | 섹션 제목 | TaskList.kt:53 | - |
| 할 일 빈 상태 | `"할 일이 없습니다"` | 빈 상태 | TaskList.kt:262 | P2: "할 일이 없어요" |
| AI 마감일 배지 | `"AI"` | 배지 | TaskList.kt:140 | - |
| 마감 텍스트 | `"M/d HH:mm 마감"` | 날짜 포맷 | TaskList.kt:243 | - |
| 확장 마감 텍스트 | `"마감: ${formatDeadline(...)}"` | 날짜 | TaskList.kt:169 | - |
| 체크박스 완료됨 | `"완료됨"` | stateDescription | TaskList.kt:211 | - |
| 체크박스 미완료 | `"미완료"` | stateDescription | TaskList.kt:211 | - |
| 완료됨 아이콘 | `"완료됨"` | contentDescription | TaskList.kt:226 | - |
| 월 헤더 | `"${monthValue}월"` / `"${monthValue}월 ${weekOfMonth}주차"` | 헤더 | CalendarCard.kt:125 | - |
| 월 드롭다운 아이템 | `"${month}월"` | 메뉴 | CalendarCard.kt:144 | - |
| 접기/펼치기 | `"접기"` / `"펼치기"` | contentDescription | CalendarCard.kt:157 | - |
| 요일 | `"일"` `"월"` `"화"` `"수"` `"목"` `"금"` `"토"` | 요일 | CalendarCard.kt:228, 334 | - |

### 2.4 노트 화면 (NotesScreen)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 헤더 제목 | `"Notes"` | 제목 | NotesScreen.kt:179 | P0: "노트" (한글 통일) |
| 휴지통 아이콘 | `"휴지통"` | contentDescription | NotesScreen.kt:189 | - |
| 검색 아이콘 | `"검색"` | contentDescription | NotesScreen.kt:196 | - |
| 전체 필터 칩 | `"전체"` | 칩 | NotesScreen.kt:250 | - |
| 새 폴더 아이콘 | `"새 폴더"` | contentDescription | NotesScreen.kt:318 | - |
| 폴더 이름 변경 메뉴 | `"이름 변경"` | 메뉴 | NotesScreen.kt:282 | - |
| 폴더 삭제 메뉴 | `"삭제"` | 메뉴 | NotesScreen.kt:289 | - |
| 빈 상태 (필터 있음) | `"이 폴더에 노트가 없습니다"` | 빈 상태 | NotesScreen.kt:460 | P2: "이 폴더에 노트가 없어요" |
| 빈 상태 (필터 없음) | `"아직 노트가 없습니다"` | 빈 상태 | NotesScreen.kt:461 | P2: "아직 노트가 없어요" |
| 빈 상태 안내 | `"캡처한 내용 중 노트로 분류된 항목이 여기에 표시됩니다"` | 보조 텍스트 | NotesScreen.kt:467 | P2: "캡처하면 노트로 분류된 항목이 여기에 나타나요" |

### 2.5 폴더 다이얼로그

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 새 폴더 다이얼로그 제목 | `"새 폴더"` | 다이얼로그 제목 | CreateFolderDialog.kt:29 | - |
| 폴더 이름 라벨 | `"폴더 이름"` | 입력 라벨 | CreateFolderDialog.kt:34 | - |
| 생성 버튼 | `"생성"` | 버튼 | CreateFolderDialog.kt:46 | - |
| 취소 버튼 | `"취소"` | 버튼 | CreateFolderDialog.kt:51 | - |
| 이름 변경 제목 | `"폴더 이름 변경"` | 다이얼로그 제목 | RenameFolderDialog.kt:29 | - |
| 새 이름 라벨 | `"새 이름"` | 입력 라벨 | RenameFolderDialog.kt:34 | - |
| 변경 버튼 | `"변경"` | 버튼 | RenameFolderDialog.kt:46 | - |
| 취소 버튼 | `"취소"` | 버튼 | RenameFolderDialog.kt:51 | - |

### 2.6 노트 상세 화면 (NoteDetailScreen)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 상단바 제목 | `"노트"` | 제목 | NoteDetailScreen.kt:60 | - |
| 뒤로가기 아이콘 | `"뒤로가기"` | contentDescription | NoteDetailScreen.kt:70 | - |
| 저장 아이콘 | `"저장"` | contentDescription | NoteDetailScreen.kt:91 | - |
| 에러 텍스트 | `uiState.error` (동적) | 에러 | NoteDetailScreen.kt:128 | - |
| 다시 시도 버튼 | `"다시 시도"` | 버튼 | NoteDetailScreen.kt:135 | - |
| 제목 placeholder | `"제목"` | placeholder | NoteDetailScreen.kt:179 | P2: "제목을 입력하세요" |
| 본문 placeholder | `"내용을 입력하세요..."` | placeholder | NoteDetailScreen.kt:217 | - |
| 첨부 이미지 라벨 | `"첨부 이미지"` | 라벨 | NoteDetailScreen.kt:232 | - |
| 첨부 이미지 | `"첨부 이미지"` | contentDescription | NoteDetailScreen.kt:245 | - |
| 폴더 라벨 | `"폴더"` | 라벨 | NoteDetailScreen.kt:254 | - |
| 폴더 없음 | `"없음"` | 텍스트 | NoteDetailScreen.kt:348 | P2: "폴더 없음" |
| 폴더 선택 아이콘 | `"폴더 선택"` | contentDescription | NoteDetailScreen.kt:371 | - |
| 폴더 선택 바텀시트 | `"폴더 선택"` | 바텀시트 제목 | NoteDetailScreen.kt:404 | - |
| 선택됨 아이콘 | `"선택됨"` | contentDescription | NoteDetailScreen.kt:433 | - |
| 생성일 | `"생성일: ${formatDateTime(...)}"` | 날짜 | NoteDetailScreen.kt:271 | - |
| 수정일 | `"수정일: ${formatDateTime(...)}"` | 날짜 | NoteDetailScreen.kt:277 | - |
| 노트 없음 에러 | `"노트를 찾을 수 없습니다"` | 에러 | NoteDetailViewModel.kt:72 | P1: "노트를 찾을 수 없어요" |
| 분류 칩 (아이디어) | `"아이디어"` | 칩 | NoteDetailScreen.kt:314 | - |
| 분류 칩 (북마크) | `"북마크"` | 칩 | NoteDetailScreen.kt:315 | - |
| 분류 칩 (노트) | `"노트"` | 칩 | NoteDetailScreen.kt:316 | - |
| 분류 칩 (할 일) | `"할 일"` | 칩 | NoteDetailScreen.kt:317 | - |
| 분류 칩 (일정) | `"일정"` | 칩 | NoteDetailScreen.kt:318 | - |
| 분류 칩 (임시) | `"임시"` | 칩 | NoteDetailScreen.kt:319 | - |

### 2.7 설정 화면 (SettingsScreen)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 상단바 제목 | `"Settings"` | 제목 | SettingsScreen.kt:60 | P0: "설정" (한글 통일) |
| 뒤로가기 | `"뒤로가기"` | contentDescription | SettingsScreen.kt:70 | - |
| 테마 섹션 | `"테마"` | 섹션 헤더 | SettingsScreen.kt:92 | - |
| 시스템 설정 | `"시스템 설정"` | 옵션 제목 | SettingsScreen.kt:97 | - |
| 시스템 설정 설명 | `"기기 설정에 따름"` | 설명 | SettingsScreen.kt:98 | - |
| 라이트 모드 | `"라이트 모드"` | 옵션 제목 | SettingsScreen.kt:106 | - |
| 다크 모드 | `"다크 모드"` | 옵션 제목 | SettingsScreen.kt:114 | - |
| Google Calendar 섹션 | `"Google Calendar"` | 섹션 헤더 | SettingsScreen.kt:123 | - (고유명사) |
| Google Calendar 연동 | `"Google Calendar 연동"` | 옵션 제목 | SettingsScreen.kt:127 | - |
| 연동 설명 | `"일정을 Google Calendar에 동기화"` | 설명 | SettingsScreen.kt:128 | - |
| OAuth 시작 | `"Google OAuth 시작 (자동)"` | 옵션 제목 | SettingsScreen.kt:136 | P1: "Google 계정 연결" |
| OAuth 설명 | `"브라우저 인증 후 앱으로 자동 복귀"` | 설명 | SettingsScreen.kt:137 | P1: "브라우저에서 인증 후 자동으로 돌아와요" |
| 자동 추가 | `"자동 추가"` | 옵션 제목 | SettingsScreen.kt:156 | - |
| 자동 추가 설명 | `"신뢰도 높은 일정은 자동 추가"` | 설명 | SettingsScreen.kt:157 | - |
| 제안 모드 | `"제안 모드"` | 옵션 제목 | SettingsScreen.kt:165 | - |
| 제안 모드 설명 | `"일정 추가 전 승인 요청"` | 설명 | SettingsScreen.kt:166 | - |
| 알림 섹션 | `"알림"` | 섹션 헤더 | SettingsScreen.kt:206 | - |
| 알림 설명 | `"일정 추가 확인 및 제안 알림"` | 설명 | SettingsScreen.kt:211 | - |
| 정보 섹션 | `"정보"` | 섹션 헤더 | SettingsScreen.kt:220 | - |
| 개인정보 처리방침 | `"개인정보 처리방침"` | 항목 | SettingsScreen.kt:224 | - |
| 이용약관 | `"이용약관"` | 항목 | SettingsScreen.kt:231 | - |
| 앱 버전 | `"앱 버전"` | 항목 | SettingsScreen.kt:238 | - |
| 버전 번호 | `"1.0.0"` | 텍스트 | SettingsScreen.kt:239 | - |
| 선택됨 아이콘 | `"선택됨"` | contentDescription | SettingsScreen.kt:343 | - |
| 개발자 도구 섹션 | `"개발자 도구"` | 섹션 헤더 | SettingsScreen.kt:248 | - |
| 이미지 캡처 테스트 | `"이미지 캡처 테스트"` | 항목 | SettingsScreen.kt:709 | - (디버그) |
| 테스트 설명 | `"갤러리에서 이미지를 선택하여 캡처로 제출"` | 설명 | SettingsScreen.kt:716 | - (디버그) |
| 캘린더 연동 결과 | `"캘린더 연동 결과"` | 다이얼로그 제목 | SettingsScreen.kt:291 | - |
| 확인 버튼 | `"확인"` | 버튼 | SettingsScreen.kt:295 | - |

### 2.8 설정 ViewModel 에러 메시지

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| OAuth 미설정 | `"GOOGLE_OAUTH_CLIENT_ID를 설정해야 합니다."` | 에러 | SettingsScreen.kt:144 | - (개발자용) |
| code 미입력 | `"code/redirect_uri를 입력해주세요."` | 검증 | SettingsViewModel.kt:131 | - (디버그) |
| access_token 미입력 | `"access_token을 입력해주세요."` | 검증 | SettingsViewModel.kt:161 | - (디버그) |
| 연결 성공 | `"Google Calendar 연결 성공"` | 성공 | SettingsViewModel.kt:145 | - |
| 연결 실패 | `"연결 실패"` | 에러 | SettingsViewModel.kt:145 | P1: "연결에 실패했어요. 다시 시도해주세요" |
| 토큰 저장 성공 | `"토큰 저장 성공"` | 성공 | SettingsViewModel.kt:181 | - (디버그) |
| 토큰 저장 실패 | `"토큰 저장 실패"` | 에러 | SettingsViewModel.kt:181 | - (디버그) |
| Google 인증 필요 | `"Google 계정 연결이 필요합니다."` | 에러 | SettingsViewModel.kt:260 | - |
| 토큰 만료 | `"Google 토큰이 만료되었습니다. 다시 연결해주세요."` | 에러 | SettingsViewModel.kt:261 | - |
| Google API 오류 | `"Google Calendar API 오류가 발생했습니다."` | 에러 | SettingsViewModel.kt:262 | - |
| 캘린더 이벤트 조회 | `"캘린더 이벤트 ${events.size}건 조회됨"` | 성공 | SettingsViewModel.kt:208 | - (디버그) |

### 2.9 AI 분류 현황 (AIStatusSheet)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 패널 제목 | `"AI 분류 현황"` | 제목 | AIStatusSheet.kt:173 | - |
| 부제 | `"24시간 이내 분류된 항목"` | 보조 텍스트 | AIStatusSheet.kt:181 | - |
| 전체 확인 버튼 | `"전체 확인"` | 버튼 | AIStatusSheet.kt:189 | - |
| 빈 상태 | `"미확인 분류가 없습니다"` | 빈 상태 | AIStatusSheet.kt:98 | P2: "미확인 분류가 없어요" |
| 전체 기록 보기 | `"전체 기록 보기"` | 링크 | AIStatusSheet.kt:143 | - |
| 확인 버튼 (개별) | `"확인"` | 버튼 | AIStatusSheet.kt:257 | - |
| 상대 시간 | `"방금"` / `"N분 전"` / `"N시간 전"` / `"어제"` | 시간 | AIStatusSheet.kt:275-279 | - |

### 2.10 분류 드롭다운 (ClassificationDropdown)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 일정 | `"일정"` | 분류 옵션 | ClassificationDropdown.kt:102 | - |
| 할 일 | `"할 일"` | 분류 옵션 | ClassificationDropdown.kt:103 | - |
| 노트 | `"노트"` | 분류 옵션 | ClassificationDropdown.kt:104 | - |
| 아이디어 | `"아이디어"` | 분류 옵션 | ClassificationDropdown.kt:105 | - |
| 미분류 | `"미분류"` | 분류 라벨 | ClassificationDropdown.kt:121 | - |
| 북마크 | `"북마크"` | 분류 라벨 | ClassificationDropdown.kt:117 | - |

### 2.11 온보딩 (OnboardingScreen)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 건너뛰기 | `"건너뛰기"` | 링크 | OnboardingScreen.kt:77 | - |
| 1페이지 브랜드 | `"Flit."` | 브랜드 | OnboardingScreen.kt:140 | - |
| 1페이지 메인 | `"적으면\n알아서 정리됩니다"` | 제목 | OnboardingScreen.kt:150 | - (좋은 문구) |
| 1페이지 설명 | `"떠오르는 순간, 바로 던지면 끝.\n정리는 AI가 알아서 합니다."` | 설명 | OnboardingScreen.kt:161 | - (좋은 문구) |
| 2페이지 칩 | `"할 일"` `"일정"` `"노트"` | 칩 | OnboardingScreen.kt:187-189 | - |
| 2페이지 메인 | `"AI가 자동으로\n분류합니다"` | 제목 | OnboardingScreen.kt:195 | - |
| 2페이지 설명 | `"틀리면 탭 한 번으로 수정하세요.\n당신은 기록만 하면 됩니다."` | 설명 | OnboardingScreen.kt:206 | P1: "당신은" 생략 → "기록만 하면 돼요" |
| Google 페이지 제목 | `"Google Calendar\n연동"` | 제목 | OnboardingScreen.kt:232 | - |
| Google 페이지 설명 | `"일정을 캘린더에 자동으로 추가하고\n알림을 받을 수 있습니다."` | 설명 | OnboardingScreen.kt:243 | P2: "~받을 수 있어요" |
| 연결됨 | `"연결됨"` | 상태 | OnboardingScreen.kt:262 | - |
| Google Calendar 연결 | `"Google Calendar 연결"` | 버튼 | OnboardingScreen.kt:278 | - |
| 나중에 설정 안내 | `"나중에 설정에서 연결할 수도 있습니다."` | 보조 텍스트 | OnboardingScreen.kt:289 | P2: "나중에 설정에서 연결할 수도 있어요" |
| 3페이지 제목 | `"첫 번째 생각을\n적어보세요"` | 제목 | OnboardingScreen.kt:339 | - (좋은 문구) |
| 3페이지 설명 | `"무엇이든 좋습니다.\n할 일, 일정, 아이디어 — 그냥 적으세요."` | 설명 | OnboardingScreen.kt:350 | - (좋은 문구) |
| 3페이지 placeholder | `"첫 번째 생각을 적어보세요..."` | placeholder | OnboardingScreen.kt:386 | - |
| 전송 아이콘 | `"전송"` | contentDescription | OnboardingScreen.kt:422 | - |
| 다음 버튼 | `"다음"` | 버튼 | OnboardingScreen.kt:466 | - |
| 시작하기 버튼 | `"시작하기"` | 버튼 | OnboardingScreen.kt:466 | - |

### 2.12 검색 화면 (SearchScreen)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 뒤로가기 | `"뒤로가기"` | contentDescription | SearchScreen.kt:86 | - |
| 검색 placeholder | `"캡처 검색"` | placeholder | SearchScreen.kt:110 | P2: "검색어를 입력하세요" |
| 지우기 아이콘 | `"지우기"` | contentDescription | SearchScreen.kt:137 | - |
| 초기 상태 | `"캡처를 검색하세요"` | 빈 상태 | SearchScreen.kt:176 | P2: "캡처를 검색해보세요" |
| 결과 없음 제목 | `"결과 없음"` | 빈 상태 | SearchScreen.kt:200 | - |
| 결과 없음 설명 | `"다른 키워드로 검색해보세요"` | 보조 텍스트 | SearchScreen.kt:206 | - |
| 분류 칩 (할 일) | `"할 일"` | 칩 | SearchScreen.kt:280 | P0: "할일" (FilterChipRow와 불일치) |
| 분류 칩 (일정/노트) | `"일정"` `"노트"` | 칩 | SearchScreen.kt:281-282 | - |

### 2.13 휴지통 (TrashScreen)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 뒤로 아이콘 | `"뒤로"` | contentDescription | TrashScreen.kt:198 | P0: "뒤로가기" (일관성) |
| 휴지통 제목 | `"휴지통"` | 제목 | TrashScreen.kt:204 | - |
| 비우기 버튼 | `"비우기"` | 버튼 | TrashScreen.kt:214 | - |
| 빈 상태 | `"휴지통이 비어 있습니다"` | 빈 상태 | TrashScreen.kt:88 | P2: "휴지통이 비어 있어요" |
| 비우기 다이얼로그 제목 | `"휴지통 비우기"` | 다이얼로그 제목 | TrashScreen.kt:120 | - |
| 비우기 경고 | `"모든 항목이 영구적으로 삭제됩니다.\n이 작업은 되돌릴 수 없습니다."` | 경고 | TrashScreen.kt:123 | - (좋은 문구) |
| 삭제 버튼 | `"삭제"` | 버튼 | TrashScreen.kt:132 | - |
| 취소 버튼 | `"취소"` | 버튼 | TrashScreen.kt:137 | - |
| 완전 삭제 제목 | `"완전 삭제"` | 다이얼로그 제목 | TrashScreen.kt:150 | - |
| 완전 삭제 경고 | `"이 항목을 영구적으로 삭제합니다.\n이 작업은 되돌릴 수 없습니다."` | 경고 | TrashScreen.kt:153 | - |
| 복원 버튼 | `"복원"` | 버튼 | TrashScreen.kt:282 | - |
| 완전 삭제 버튼 | `"완전 삭제"` | 버튼 | TrashScreen.kt:295 | - |
| 삭제 시각 | `"삭제: $trashedDate"` | 날짜 | TrashScreen.kt:263 | P1: "삭제일: $trashedDate" |

### 2.14 전체 기록 (HistoryScreen)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 뒤로 아이콘 | `"뒤로"` | contentDescription | HistoryScreen.kt:231 | P0: "뒤로가기" (일관성) |
| 전체 기록 제목 | `"전체 기록"` | 제목 | HistoryScreen.kt:236 | - |
| 삭제 스낵바 | `"삭제되었습니다"` | snackbar | HistoryScreen.kt:50 | P1: "삭제했어요" |
| 실행 취소 | `"실행 취소"` | 액션 라벨 | HistoryScreen.kt:51 | - |
| 빈 상태 제목 | `"기록이 없습니다"` | 빈 상태 | HistoryScreen.kt:149 | P2: "기록이 없어요" |
| 빈 상태 설명 | `"캡처한 내용이 여기에 시간순으로 표시됩니다"` | 보조 텍스트 | HistoryScreen.kt:154 | P2: "캡처하면 시간순으로 여기에 나타나요" |
| 날짜 필터 (전체) | `"전체"` | 칩 | HistoryScreen.kt:305 | - |
| 날짜 필터 (오늘) | `"오늘"` | 칩 | HistoryScreen.kt:310 | - |
| 날짜 필터 (이번 주) | `"이번 주"` | 칩 | HistoryScreen.kt:315 | - |
| 날짜 필터 (이번 달) | `"이번 달"` | 칩 | HistoryScreen.kt:320 | - |
| 상대 시간 | `"방금"` / `"N분 전"` / `"N시간 전"` / `"어제"` / `"N일 전"` | 시간 | HistoryItem.kt:126-134 | - |
| 분류 중 | `"분류 중"` | 상태 | HistoryItem.kt:97 | - |

### 2.15 캡처 상세 (CaptureDetailScreen)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 상단바 제목 (기본) | `"캡처 상세"` | 제목 | CaptureDetailScreen.kt:51 | - |
| 뒤로가기 | `"뒤로가기"` | contentDescription | CaptureDetailScreen.kt:63 | - |
| 에러 텍스트 | `uiState.errorMessage` | 에러 | CaptureDetailScreen.kt:95 | - |
| 다시 시도 | `"다시 시도"` | 버튼 | CaptureDetailScreen.kt:101 | - |
| 분류 라벨 | `"분류"` | 라벨 | CaptureDetailScreen.kt:122 | - |
| 분류 칩 | `"할 일"` `"일정"` `"노트"` `"아이디어"` | 칩 | CaptureDetailScreen.kt:247-264 | - |
| 첨부 이미지 라벨 | `"첨부 이미지"` | 라벨 | CaptureDetailScreen.kt:156 | - |
| 첨부 이미지 | `"첨부 이미지"` | contentDescription | CaptureDetailScreen.kt:169 | - |
| 생성 시각 라벨 | `"생성 시각"` | 라벨 | CaptureDetailScreen.kt:182 | - |
| 원문 라벨 | `"원문"` | 라벨 | CaptureDetailScreen.kt:201 | - |
| 캡처 없음 에러 | `"캡처를 찾을 수 없습니다"` | 에러 | CaptureDetailViewModel.kt:84 | P1: "캡처를 찾을 수 없어요" |

### 2.16 캘린더 동기화 섹션 (CaptureDetailScreen)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 캘린더 동기화 라벨 | `"캘린더 동기화"` | 라벨 | CaptureDetailScreen.kt:319 | - |
| 동기화됨 | `"Google Calendar에 동기화됨"` | 상태 | CaptureDetailScreen.kt:337 | - |
| 제안 상태 | `"캘린더 추가를 제안합니다"` | 상태 | CaptureDetailScreen.kt:338 | P2: "캘린더에 추가할까요?" |
| 동기화 실패 | `"동기화 실패"` | 상태 | CaptureDetailScreen.kt:339 | - |
| 거부됨 | `"사용자가 거부함"` | 상태 | CaptureDetailScreen.kt:340 | P1: "거부됨" (간결하게) |
| 연결 안됨 | `"연결되지 않음"` | 상태 | CaptureDetailScreen.kt:341 | - |
| 캘린더에 추가 | `"캘린더에 추가"` | 버튼 | CaptureDetailScreen.kt:363 | - |
| 무시 | `"무시"` | 버튼 | CaptureDetailScreen.kt:376 | - |

### 2.17 공통 컴포넌트

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 탭 라벨 (Notes) | `"Notes"` | 탭 contentDescription | FlitBottomNav.kt:125 | P0: "노트" (한글 통일) |
| 탭 라벨 (Home) | `"Home"` | 탭 contentDescription | FlitBottomNav.kt:130 | P0: "홈" (한글 통일) |
| 탭 라벨 (Calendar) | `"Calendar"` | 탭 contentDescription | FlitBottomNav.kt:135 | P0: "캘린더" (한글 통일) |
| 필터 칩 (전체) | `"전체"` | 칩 | FilterChipRow.kt:33 | - |
| 필터 칩 (일정) | `"일정"` | 칩 | FilterChipRow.kt:38 | - |
| 필터 칩 (할일) | `"할일"` | 칩 | FilterChipRow.kt:43 | P0: "할 일" (띄어쓰기 통일) |
| 필터 칩 (노트) | `"노트"` | 칩 | FilterChipRow.kt:48 | - |

### 2.18 위젯

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 캡처 위젯 접근성 | `"Flit."` | contentDescription | widget_capture.xml:15 | P1: "캡처 위젯" |
| 캡처 위젯 텍스트 | `"탭하여 캡처하기"` | 텍스트 | widget_capture.xml:23 | - |
| 할 일 위젯 제목 | `"오늘 할 일"` | 제목 | widget_todo.xml:14 | - |
| 할 일 위젯 빈 상태 | `"할 일이 없습니다"` | 빈 상태 | widget_todo.xml:36 | P2: "할 일이 없어요" |
| 앱 열기 | `"앱 열기"` | 링크 | widget_todo.xml:48 | - |

### 2.19 OAuth 디버그 다이얼로그 (SettingsScreen)

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 코드 교환 제목 | `"OAuth Code 교환"` | 다이얼로그 제목 | SettingsScreen.kt:541 | - (디버그) |
| 라벨 | `"authorization code"` | 입력 라벨 | SettingsScreen.kt:547 | - (디버그) |
| 라벨 | `"redirect_uri"` | 입력 라벨 | SettingsScreen.kt:563 | - (디버그) |
| 요청 버튼 | `"요청"` | 버튼 | SettingsScreen.kt:582 | - (디버그) |
| 취소 버튼 | `"취소"` | 버튼 | SettingsScreen.kt:585 | - (디버그) |
| 토큰 저장 제목 | `"토큰 직접 저장"` | 다이얼로그 제목 | SettingsScreen.kt:614 | - (디버그) |
| 라벨 | `"access_token"` | 입력 라벨 | SettingsScreen.kt:620 | - (디버그) |
| 라벨 | `"refresh_token (optional)"` | 입력 라벨 | SettingsScreen.kt:628 | - (디버그) |
| 라벨 | `"expires_in seconds (optional)"` | 입력 라벨 | SettingsScreen.kt:636 | - (디버그) |
| 저장 버튼 | `"저장"` | 버튼 | SettingsScreen.kt:648 | - (디버그) |
| 취소 버튼 | `"취소"` | 버튼 | SettingsScreen.kt:651 | - (디버그) |

### 2.20 로그인 화면 (LoginScreen) — Phase 3a 추가

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 상단바 제목 | `"로그인"` | 제목 | LoginScreen.kt:99 | - |
| 뒤로가기 | `"뒤로가기"` | contentDescription | LoginScreen.kt:108 | - |
| 태그라인 | `"적으면, 알아서 정리됩니다"` | 부제목 | LoginScreen.kt:136 | - (PRD 공식 태그라인) |
| Google 로그인 버튼 | `"Google로 로그인"` | 버튼 | LoginScreen.kt:165 | - |
| 안내 텍스트 | `"로그인 없이도 기본 기능을 사용할 수 있습니다"` | 안내 | LoginScreen.kt:183 | - |
| 주의 텍스트 | `"다른 Google 계정으로 로그인하면 기존 로컬 데이터는 초기화됩니다"` | 주의 | LoginScreen.kt:191 | - |
| OAuth 미설정 에러 | `"GOOGLE_WEB_CLIENT_ID를 설정해주세요."` | 에러 | LoginScreen.kt:62 | - (개발자용) |
| 로그인 실패 에러 | `"Google 로그인에 실패했습니다. 다시 시도해주세요."` | 에러 | LoginScreen.kt:64 | P1: "Google 로그인에 실패했어요. 다시 시도해주세요" |

### 2.21 구독 화면 (SubscriptionScreen) — Phase 3a 추가

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 상단바 제목 | `"구독 관리"` | 제목 | SubscriptionScreen.kt:66 | - |
| 뒤로가기 | `"뒤로가기"` | contentDescription | SubscriptionScreen.kt:75 | - |
| 기능 섹션 | `"Premium 기능"` | 섹션 제목 | SubscriptionScreen.kt:103 | - |
| 기능 목록 | `"AI 그룹 분류"` `"받은함 자동 분류"` `"의미 검색"` `"노트 재구성"` `"분석 대시보드"` `"이미지 텍스트 인식 (OCR)"` `"분류 프리셋"` `"맞춤 지시어"` | 기능명 | SubscriptionScreen.kt:111-118 | - |
| 업그레이드 버튼 | `"Premium으로 업그레이드"` | 버튼 | SubscriptionScreen.kt:166 | - |
| 플랜 이름 | `"Premium"` / `"Free"` | 라벨 | SubscriptionScreen.kt:213 | - |
| 플랜 설명 (Premium) | `"모든 기능을 사용할 수 있습니다"` | 설명 | SubscriptionScreen.kt:223 | P2: "모든 기능을 사용할 수 있어요" |
| 플랜 설명 (Free) | `"기본 기능만 사용 가능합니다"` | 설명 | SubscriptionScreen.kt:223 | P2: "기본 기능만 사용할 수 있어요" |
| 사용 가능 아이콘 | `"사용 가능"` | contentDescription | SubscriptionScreen.kt:261 | - |
| 프리미엄 전용 아이콘 | `"프리미엄 전용"` | contentDescription | SubscriptionScreen.kt:268 | - |

### 2.22 AI 재구성 화면 (ReorganizeScreen) — Phase 3b 추가

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 상단바 제목 | `"AI 재구성"` | 제목 | ReorganizeScreen.kt:67 | - |
| 뒤로가기 | `"뒤로가기"` | contentDescription | ReorganizeScreen.kt:77 | - |
| 로딩 텍스트 | `"AI가 노트를 분석 중입니다..."` | 로딩 | ReorganizeScreen.kt:104 | - |
| 다시 시도 | `"다시 시도"` | 버튼 | ReorganizeScreen.kt:127 | - |
| 컬럼 헤더 | `"현재"` / `"AI 제안"` | 헤더 | ReorganizeScreen.kt:149,157 | - |
| 액션 라벨 | `"새 폴더"` / `"이동"` | 라벨 | ReorganizeScreen.kt:176-177 | - (action 매핑 적용됨) |
| 적용 버튼 | `"제안 적용 (N건)"` | 버튼 | ReorganizeScreen.kt:210 | - |

### 2.23 분석 대시보드 (AnalyticsDashboardScreen) — Phase 3b 추가

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 상단바 제목 | `"분석"` | 제목 | AnalyticsDashboardScreen.kt:63 | - |
| 뒤로가기 | `"뒤로가기"` | contentDescription | AnalyticsDashboardScreen.kt:73 | - |
| 다시 시도 | `"다시 시도"` | 버튼 | AnalyticsDashboardScreen.kt:101 | - |
| 빈 상태 제목 | `"아직 분석할 데이터가 없어요"` | 빈 상태 | AnalyticsDashboardScreen.kt:127 | - |
| 빈 상태 설명 | `"캡처를 시작하면 통계가 여기에 나타나요"` | 보조 텍스트 | AnalyticsDashboardScreen.kt:134 | - |
| 총 캡처 | `"총 캡처"` | 통계 카드 | AnalyticsDashboardScreen.kt:164 | - |
| 유형별 분포 | `"유형별 분포"` | 섹션 제목 | AnalyticsDashboardScreen.kt:172 | - |
| 카운트 | `"N건"` | 카운트 | AnalyticsDashboardScreen.kt:193 | - |
| 평균 분류 시간 | `"평균 분류 시간"` | 통계 카드 | AnalyticsDashboardScreen.kt:199 | - |
| 인기 태그 | `"인기 태그"` | 섹션 제목 | AnalyticsDashboardScreen.kt:207 | - |
| 태그 카운트 | `"N회"` | 카운트 | AnalyticsDashboardScreen.kt:223 | - |

### 2.24 AI 분류 설정 (AiClassificationSettingsScreen) — Phase 3b 추가

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 상단바 제목 | `"AI 분류 설정"` | 제목 | AiClassificationSettingsScreen.kt:83 | - |
| 뒤로가기 | `"뒤로가기"` | contentDescription | AiClassificationSettingsScreen.kt:93 | - |
| 분류 프리셋 헤더 | `"분류 프리셋"` | 섹션 헤더 | AiClassificationSettingsScreen.kt:115 | - |
| 프리셋 라벨 | `"분류 프리셋"` | 라벨 | AiClassificationSettingsScreen.kt:140 | - |
| 기본 프리셋 | `"기본"` | 옵션 | AiClassificationSettingsScreen.kt:152,296 | - |
| 업무 프리셋 | `"업무"` | 옵션 | AiClassificationSettingsScreen.kt:301 | - |
| 개인 프리셋 | `"개인"` | 옵션 | AiClassificationSettingsScreen.kt:306 | - |
| 프리셋 설명 | `"일반적인 분류 규칙"` / `"업무 중심 분류"` / `"개인 생활 중심 분류"` | 설명 | AiClassificationSettingsScreen.kt:297,302,307 | - |
| 분류 규칙 헤더 | `"분류 규칙"` | 섹션 헤더 | AiClassificationSettingsScreen.kt:197 | - |
| 규칙 placeholder | `"예: 업무 관련 내용은 일정으로 분류"` | placeholder | AiClassificationSettingsScreen.kt:226 | - |
| 저장 버튼 | `"저장"` | 버튼 | AiClassificationSettingsScreen.kt:251 | - |

### 2.25 캘린더 설정 (CalendarSettingsScreen) — Phase 3b 추가

| 위치 | 현재 텍스트 | 유형 | 파일:줄 | 개선 제안 |
|------|------------|------|---------|----------|
| 상단바 제목 | `"캘린더 설정"` | 제목 | CalendarSettingsScreen.kt:98 | - |
| 뒤로가기 | `"뒤로가기"` | contentDescription | CalendarSettingsScreen.kt:108 | - |
| 연동 캘린더 헤더 | `"연동 캘린더"` | 섹션 헤더 | CalendarSettingsScreen.kt:131 | - |
| 연동 캘린더 라벨 | `"연동 캘린더"` | 라벨 | CalendarSettingsScreen.kt:137 | - |
| 선택 필요 | `"선택 필요"` | 기본값 | CalendarSettingsScreen.kt:140 | - |
| 새로고침 | `"캘린더 목록 새로고침"` | 액션 | CalendarSettingsScreen.kt:147 | - |
| 새로고침 설명 | `"기기 캘린더 목록을 다시 불러옵니다"` | 설명 | CalendarSettingsScreen.kt:148 | P2: "기기 캘린더 목록을 다시 불러와요" |
| 일정 추가 헤더 | `"일정 추가"` | 섹션 헤더 | CalendarSettingsScreen.kt:158 | - |
| 자동 추가 | `"자동 추가"` | 토글 라벨 | CalendarSettingsScreen.kt:164 | - |
| 자동 추가 설명 (ON) | `"신뢰도 높은 일정을 자동으로 캘린더에 추가"` | 설명 | CalendarSettingsScreen.kt:165 | - |
| 자동 추가 설명 (OFF) | `"일정 추가 전 사용자 승인을 요청합니다"` | 설명 | CalendarSettingsScreen.kt:166 | P2: "일정 추가 전 승인을 요청해요" |
| 알림 | `"알림"` | 토글 라벨 | CalendarSettingsScreen.kt:174 | - |
| 알림 설명 (ON) | `"자동 추가된 일정을 알림으로 확인"` | 설명 | CalendarSettingsScreen.kt:175 | - |
| 알림 설명 (OFF) | `"승인 대기 중인 일정이 있을 때 알림"` | 설명 | CalendarSettingsScreen.kt:176 | - |
| 바텀시트 제목 | `"연동 캘린더 선택"` | 제목 | CalendarSettingsScreen.kt:253 | - |
| 빈 캘린더 | `"사용 가능한 캘린더가 없습니다"` | 빈 상태 | CalendarSettingsScreen.kt:267 | P2: "사용 가능한 캘린더가 없어요" |
| 선택됨 | `"선택됨"` | contentDescription | CalendarSettingsScreen.kt:294 | - |

---

## 3. 개선 제안 요약

### P0 (즉시 수정 필요) — 일관성 문제 ✅ 전체 반영 완료 (2026-02-12)

| # | 현재 | 개선 | 위치 | 이유 |
|---|------|------|------|------|
| 1 | `"Notes"` (헤더) | `"노트"` | NotesScreen.kt:179 | 앱 내 다른 곳은 "노트"로 한글 사용 |
| 2 | `"Settings"` (헤더) | `"설정"` | SettingsScreen.kt:60 | 앱 내 다른 곳은 "설정"으로 한글 사용 |
| 3 | `"Notes"` (탭 라벨) | `"노트"` | FlitBottomNav.kt:125 | 접근성 라벨은 한글이 적절 |
| 4 | `"Home"` (탭 라벨) | `"홈"` | FlitBottomNav.kt:130 | 접근성 라벨은 한글이 적절 |
| 5 | `"Calendar"` (탭 라벨) | `"캘린더"` | FlitBottomNav.kt:135 | 접근성 라벨은 한글이 적절 |
| 6 | `"할일"` (필터 칩) | `"할 일"` | FilterChipRow.kt:43 | 다른 모든 곳은 "할 일"로 띄어쓰기 |
| 7 | `"뒤로"` (contentDescription) | `"뒤로가기"` | TrashScreen.kt:198, HistoryScreen.kt:231 | 다른 화면은 "뒤로가기"로 통일 |
| 8 | `"할 일"` vs `"할일"` 혼용 | `"할 일"` 통일 | SearchScreen.kt:280, FilterChipRow.kt:43 | 검색 결과 칩에서도 통일 필요 |

### P1 (개선 권장) — 톤앤매너 & 표현 개선 ✅ 대부분 반영 완료 (2026-02-14)

| # | 현재 | 개선 | 위치 | 이유 |
|---|------|------|------|------|
| 1 | `"캡처가 저장되었습니다"` | `"캡처가 저장되었어요"` | CaptureContent.kt:71 | 해요체 통일 |
| 2 | `"저장에 실패했습니다."` | `"저장에 실패했어요. 다시 시도해주세요"` | CaptureViewModel.kt:132 | 해결 방법 안내 |
| 3 | `"삭제되었습니다"` (x2) | `"삭제했어요"` | CalendarScreen.kt:44, HistoryScreen.kt:50 | 해요체, 능동형 |
| 4 | `"캘린더에 추가되었습니다"` | `"캘린더에 추가했어요"` | CalendarScreen.kt:54 | 해요체 |
| 5 | `"실행 취소에 실패했습니다."` | ~~`"되돌리기에 실패했어요"`~~ → `"실행 취소에 실패했어요"` | CalendarViewModel.kt:235 | ✅ 해요체 전환 완료. "실행 취소"는 design-guide 용어 통일표 공식 용어이므로 유지 |
| 6 | `"삭제에 실패했습니다."` | `"삭제에 실패했어요"` | CalendarViewModel.kt:248 | 해요체 |
| 7 | 에러 메시지들 `"~했습니다."` | `"~했어요"` | CalendarViewModel.kt:265,283 | 해요체 통일 |
| 8 | `"Google OAuth 시작 (자동)"` | `"Google 계정 연결"` | SettingsScreen.kt:136 | ⏭️ 코드에서 해당 텍스트 미발견 (이전 리팩토링으로 제거된 것으로 추정) |
| 9 | `"브라우저 인증 후 앱으로 자동 복귀"` | `"브라우저에서 인증 후 자동으로 돌아와요"` | SettingsScreen.kt:137 | ⏭️ 코드에서 해당 텍스트 미발견 |
| 10 | `"연결 실패"` | `"연결에 실패했어요. 다시 시도해주세요"` | SettingsViewModel.kt:145 | ⏭️ 코드에서 해당 텍스트 미발견 |
| 11 | `"노트를 찾을 수 없습니다"` | `"노트를 찾을 수 없어요"` | NoteDetailViewModel.kt:72 | 해요체 |
| 12 | `"캡처를 찾을 수 없습니다"` | `"캡처를 찾을 수 없어요"` | CaptureDetailViewModel.kt:84 | 해요체 |
| 13 | `"사용자가 거부함"` | `"거부됨"` | CaptureDetailScreen.kt:340 | 간결하게 (다른 배지와 동일 형태) |
| 14 | `"당신은 기록만 하면 됩니다."` | `"기록만 하면 돼요."` | OnboardingScreen.kt:206 | "당신은" 생략, 해요체 |
| 15 | `"삭제: $trashedDate"` | `"삭제일: $trashedDate"` | TrashScreen.kt:263 | "삭제:"보다 "삭제일:"이 명확 |
| 16 | `"Flit."` (위젯 접근성) | `"캡처 위젯"` | widget_capture.xml:15 | 브랜드명보다 기능 설명 |

### P2 (향후 고려) — 감성적 표현 & 세련됨 ✅ 대부분 반영 완료 (2026-02-14)

| # | 현재 | 개선 | 위치 | 이유 |
|---|------|------|------|------|
| 1 | `"일정이 없습니다"` | `"오늘은 일정이 없어요"` | ScheduleTimeline.kt:343 | 시간 맥락 추가 |
| 2 | `"할 일이 없습니다"` | `"할 일이 없어요"` | TaskList.kt:262 | 해요체 |
| 3 | `"이 폴더에 노트가 없습니다"` | `"이 폴더에 노트가 없어요"` | NotesScreen.kt:460 | 해요체 |
| 4 | `"아직 노트가 없습니다"` | `"아직 노트가 없어요"` | NotesScreen.kt:461 | 해요체 |
| 5 | `"캡처한 내용 중 노트로 분류된 항목이 여기에 표시됩니다"` | `"캡처하면 노트로 분류된 항목이 여기에 나타나요"` | NotesScreen.kt:467 | 능동형, 해요체 |
| 6 | `"제목"` (placeholder) | `"제목을 입력하세요"` | NoteDetailScreen.kt:179 | 입력 유도 |
| 7 | `"없음"` (폴더 미선택) | `"폴더 없음"` | NoteDetailScreen.kt:348 | 맥락 명확 |
| 8 | `"캡처 검색"` (placeholder) | `"검색어를 입력하세요"` | SearchScreen.kt:110 | 일반적 placeholder |
| 9 | `"캡처를 검색하세요"` | `"캡처를 검색해보세요"` | SearchScreen.kt:176 | "해보세요"가 더 친근 |
| 10 | `"미확인 분류가 없습니다"` | `"미확인 분류가 없어요"` | AIStatusSheet.kt:98 | 해요체 |
| 11 | `"기록이 없습니다"` | `"기록이 없어요"` | HistoryScreen.kt:149 | 해요체 |
| 12 | `"캡처한 내용이 여기에 시간순으로 표시됩니다"` | `"캡처하면 시간순으로 여기에 나타나요"` | HistoryScreen.kt:154 | 능동형 |
| 13 | `"휴지통이 비어 있습니다"` | `"휴지통이 비어 있어요"` | TrashScreen.kt:88 | 해요체 |
| 14 | `"일정을 캘린더에 자동으로 추가하고\n알림을 받을 수 있습니다."` | `"~받을 수 있어요"` | OnboardingScreen.kt:243 | 해요체 |
| 15 | `"나중에 설정에서 연결할 수도 있습니다."` | `"나중에 설정에서 연결할 수도 있어요"` | OnboardingScreen.kt:289 | 해요체 |
| 16 | `"캘린더 추가를 제안합니다"` | `"캘린더에 추가할까요?"` | CaptureDetailScreen.kt:338 | 질문형이 더 자연스러움 |
| 17 | `"할 일이 없습니다"` (위젯) | `"할 일이 없어요"` | widget_todo.xml:36 | ⏭️ widget_todo.xml 파일 미존재 |

---

## 4. 종합 분석

### 4.1 통계

| 항목 | 수량 | 상태 |
|------|------|------|
| 전체 사용자 노출 텍스트 | **약 220개** (Phase 3a/3b 포함) | - |
| P0 이슈 (즉시 수정) | **8건** | ✅ 전체 반영 (2026-02-12) |
| P1 이슈 (개선 권장) | **16건** (13건 반영, 3건 코드 미발견) | ✅ 대부분 반영 (2026-02-14) |
| P2 이슈 (향후 고려) | **17건** (14건 반영, 1건 파일 미존재, 2건 미반영) | ✅ 대부분 반영 (2026-02-14) |
| 신규 화면 추가 텍스트 | **~50개** (6개 화면) | 감사 완료 (2026-02-14) |

### 4.2 주요 문제점 (해결 현황)

1. ~~**한영 혼용 문제**: 헤더 제목이 "Notes", "Settings"로 영문~~ → ✅ P0 전체 반영 (2026-02-12)
2. ~~**문체 불일치 (합니다/해요)**: 스낵바/에러 메시지가 "~했습니다" 체~~ → ✅ 해요체 통일 완료 (2026-02-14)
3. ~~**띄어쓰기 불일치**: "할 일" vs "할일" 혼용~~ → ✅ P0 전체 반영 (2026-02-12)
4. ~~**contentDescription 불일치**: "뒤로" vs "뒤로가기" 혼용~~ → ✅ P0 전체 반영 (2026-02-12)
5. **잔여 합니다체** (신규 화면): LoginScreen "실패했습니다", SubscriptionScreen "사용할 수 있습니다/가능합니다", CalendarSettingsScreen "요청합니다/불러옵니다/없습니다" — 향후 수정 필요

### 4.3 잘 된 점

1. 온보딩 문구가 감성적이고 브랜드 가치를 잘 전달 ("적으면 알아서 정리됩니다", "첫 번째 생각을 적어보세요")
2. 캡처 입력 placeholder가 행동을 유도 ("떠오르는 생각을 캡처하세요...")
3. 삭제 경고 다이얼로그가 결과를 명확히 안내 ("영구적으로 삭제됩니다. 되돌릴 수 없습니다")
4. 빈 상태 메시지에 보조 설명이 포함되어 있어 사용자 안내가 잘 됨
5. 상대 시간 표현이 자연스러움 ("방금", "N분 전", "어제")
