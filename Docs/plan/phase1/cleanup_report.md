# Phase 1-0: 레거시 코드 정리 리포트

## 삭제된 파일 목록

### Processors (Phase 1 불필요)
| 파일 | 이유 |
|------|------|
| `data/processor/OcrProcessor.kt` | Phase 1에서 OCR 기능 불필요 |
| `data/processor/WebClipper.kt` | Phase 1에서 웹 클리핑 불필요 |
| `data/processor/VoiceRecognizer.kt` | Phase 1에서 음성 인식 불필요 |

> `data/processor/` 디렉토리 자체도 삭제 (빈 디렉토리)

### DTOs — `data/remote/dto/ai/` (전체 디렉토리 삭제)
| 파일 | 이유 |
|------|------|
| `data/remote/dto/ai/SummarizeRequest.kt` | 사용 안 함 |
| `data/remote/dto/ai/SummarizeResponse.kt` | 사용 안 함 |
| `data/remote/dto/ai/TagSuggestRequest.kt` | 사용 안 함 |
| `data/remote/dto/ai/TagSuggestResponse.kt` | 사용 안 함 |

> `data/remote/dto/ai/` 디렉토리 자체도 삭제

### DTOs — `data/remote/dto/v2/` (NoteCreate*, Ocr*, Stt*, Clip* 관련)
| 파일 | 이유 |
|------|------|
| `data/remote/dto/v2/NoteCreateRequest.kt` | Phase 1 API 스펙에서 제거 |
| `data/remote/dto/v2/NoteCreateResponse.kt` | Phase 1 API 스펙에서 제거 |
| `data/remote/dto/v2/OcrRequest.kt` | Phase 1 불필요 |
| `data/remote/dto/v2/OcrResponse.kt` | Phase 1 불필요 |
| `data/remote/dto/v2/SttRequest.kt` | Phase 1 불필요 |
| `data/remote/dto/v2/SttResponse.kt` | Phase 1 불필요 |
| `data/remote/dto/v2/ClipRequest.kt` | Phase 1 불필요 |
| `data/remote/dto/v2/ClipMetadataDto.kt` | Phase 1 불필요 |
| `data/remote/dto/v2/ClipResponse.kt` | Phase 1 불필요 |

> `data/remote/dto/v2/`에 남은 파일: `ClassifyRequest.kt`, `ClassifyResponse.kt`, `HealthResponse.kt`, `TodoMetadataDto.kt` (Phase 1-3에서 재작성 예정)

### UseCases — Capture (Phase 1 불필요)
| 파일 | 이유 |
|------|------|
| `domain/usecase/capture/SubmitImageCaptureUseCase.kt` | Phase 1에서 이미지 캡처 불필요 |
| `domain/usecase/capture/SubmitVoiceCaptureUseCase.kt` | Phase 1에서 음성 캡처 불필요 |
| `domain/usecase/capture/SubmitWebClipUseCase.kt` | Phase 1에서 웹 클립 불필요 |

### UseCases — Notifications (전체 디렉토리 삭제)
| 파일 | 이유 |
|------|------|
| `domain/usecase/notifications/AddNotificationUseCase.kt` | AI Status Sheet로 대체 |
| `domain/usecase/notifications/MarkNotificationAsReadUseCase.kt` | AI Status Sheet로 대체 |
| `domain/usecase/notifications/GetNotificationsUseCase.kt` | AI Status Sheet로 대체 |

> `domain/usecase/notifications/` 디렉토리 자체도 삭제

### Presentation — Notifications (전체 디렉토리 삭제, AI Status Sheet로 대체)
| 파일 | 이유 |
|------|------|
| `presentation/notifications/NotificationsScreen.kt` | AI Status Sheet로 대체 |
| `presentation/notifications/NotificationsViewModel.kt` | AI Status Sheet로 대체 |
| `presentation/notifications/NotificationsUiState.kt` | AI Status Sheet로 대체 |

> `presentation/notifications/` 디렉토리 자체도 삭제

### Presentation — Capture (Phase 1 불필요)
| 파일 | 이유 |
|------|------|
| `presentation/capture/QuickCaptureOverlay.kt` | Phase 1 불필요 |
| `presentation/capture/QuickCapturePopup.kt` | Phase 1 불필요 |

### 테스트 파일 (삭제된 프로덕션 코드의 테스트)
| 파일 | 이유 |
|------|------|
| `test/.../data/processor/VoiceRecognizerTest.kt` | 프로덕션 코드 삭제됨 |
| `test/.../presentation/notifications/NotificationsViewModelTest.kt` | 프로덕션 코드 삭제됨 |

---

## 삭제하지 않은 파일 (다른 담당자가 처리)

| 파일 | 이유 |
|------|------|
| `entities/BookmarkEntity.kt` | data-model 담당 |
| `entities/NotificationEntity.kt` | data-model 담당 |
| `dao/BookmarkDao.kt` | data-model 담당 |
| `dao/NotificationDao.kt` | data-model 담당 |
| `domain/model/*` (Bookmark, Destination 등) | feature 담당 |
| `domain/repository/BookmarkRepository.kt` | feature 담당 |
| `domain/repository/NotificationRepository.kt` | feature 담당 |
| `data/repository/BookmarkRepositoryImpl.kt` | Phase 1-3에서 처리 |
| `data/repository/NotificationRepositoryImpl.kt` | Phase 1-3에서 처리 |
| `data/mapper/BookmarkMapper.kt` | Phase 1-3에서 처리 |

---

## 삭제로 인해 발생하는 컴파일 에러 (예상)

### `data/repository/CaptureRepositoryImpl.kt`
- `OcrProcessor` import 및 사용 (line 9, 50)
- `WebClipper` import 및 사용 (line 10, 51)
- `NoteCreateRequest` import 및 사용 (line 14, 180)

### `data/remote/api/KairosApi.kt`
- `ClipRequest`, `ClipResponse` import 및 메서드 시그니처 (line 5-6, 67-68)
- `NoteCreateRequest`, `NoteCreateResponse` import 및 메서드 시그니처 (line 8-9, 40-41)
- `OcrRequest`, `OcrResponse` import 및 메서드 시그니처 (line 10-11, 49-50)
- `SttRequest`, `SttResponse` import 및 메서드 시그니처 (line 12-13, 58-59)

### `data/remote/api/MockKairosApi.kt`
- `ClipMetadataDto`, `ClipRequest`, `ClipResponse` import 및 사용 (line 5-7, 219-232)
- `NoteCreateRequest`, `NoteCreateResponse` import 및 사용 (line 9-10, 143-161)
- `OcrRequest`, `OcrResponse` import 및 사용 (line 11-12, 178-183)
- `SttRequest`, `SttResponse` import 및 사용 (line 13-14, 199-204)

### `navigation/NavGraph.kt`
- `QuickCaptureOverlay` import 및 사용 (line 13, 194)
- `NotificationsScreen` import 및 사용 (line 17, 160)

> 위 컴파일 에러는 다른 서브페이즈(Phase 1-3: DTO/API 재작성, Phase 1-6: UI 재작성)에서 해결될 예정
