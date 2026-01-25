package com.example.kairos_mobile.presentation.capture

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.data.processor.VoiceRecognizer
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.SyncStatus
import com.example.kairos_mobile.domain.repository.PreferencesRepository
import com.example.kairos_mobile.domain.usecase.GenerateSummaryUseCase
import com.example.kairos_mobile.domain.usecase.GetPendingCapturesUseCase
import com.example.kairos_mobile.domain.usecase.SubmitCaptureUseCase
import com.example.kairos_mobile.domain.usecase.SubmitImageCaptureUseCase
import com.example.kairos_mobile.domain.usecase.SubmitVoiceCaptureUseCase
import com.example.kairos_mobile.domain.usecase.SubmitWebClipUseCase
import com.example.kairos_mobile.domain.usecase.SuggestTagsUseCase
import kotlinx.coroutines.flow.first
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Capture 화면 ViewModel
 */
@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val submitCaptureUseCase: SubmitCaptureUseCase,
    private val submitImageCaptureUseCase: SubmitImageCaptureUseCase,  // Phase 2: M05
    private val submitVoiceCaptureUseCase: SubmitVoiceCaptureUseCase,  // Phase 2: M06
    private val submitWebClipUseCase: SubmitWebClipUseCase,            // Phase 2: M08
    private val getPendingCapturesUseCase: GetPendingCapturesUseCase,
    private val voiceRecognizer: VoiceRecognizer,                       // Phase 2: M06
    private val generateSummaryUseCase: GenerateSummaryUseCase,        // Phase 3: M09
    private val suggestTagsUseCase: SuggestTagsUseCase                 // Phase 3: M10
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    init {
        observePendingCaptures()
    }

    /**
     * 텍스트 입력 변경 처리
     */
    fun onTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    /**
     * 캡처 제출
     */
    fun onSubmit() {
        val content = _uiState.value.inputText

        // 입력 검증
        if (content.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "내용을 입력해주세요")
            }
            return
        }

        viewModelScope.launch {
            // 로딩 시작
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 캡처 제출
            when (val result = submitCaptureUseCase(content)) {
                is Result.Success -> {
                    val capture = result.data

                    // 성공
                    _uiState.update {
                        it.copy(
                            inputText = "",  // 입력 필드 클리어
                            isLoading = false,
                            showSuccessFeedback = true,
                            isOffline = capture.syncStatus != SyncStatus.SYNCED,
                            // Phase 3: 이전 스마트 처리 결과 초기화
                            latestSummary = null,
                            suggestedTags = emptyList()
                        )
                    }

                    // Phase 3: 스마트 처리 기능 (비동기 실행)
                    // M09: AI 요약 생성 (긴 콘텐츠인 경우)
                    if (generateSummaryUseCase.shouldSummarize(content)) {
                        generateSummaryForCapture(capture.id, content)
                    }

                    // M10: 스마트 태그 제안
                    if (suggestTagsUseCase.canSuggestTags(content)) {
                        suggestTagsForCapture(content, capture.classification?.type?.name)
                    }

                    // 피드백 표시 후 자동 숨김 (2초)
                    delay(2000)
                    _uiState.update { it.copy(showSuccessFeedback = false) }
                }
                is Result.Error -> {
                    // 에러
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "오류가 발생했습니다"
                        )
                    }
                }
                is Result.Loading -> {
                    // 로딩 상태는 위에서 이미 처리
                }
            }
        }
    }

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 대기중인 캡처 개수 관찰
     */
    private fun observePendingCaptures() {
        viewModelScope.launch {
            getPendingCapturesUseCase()
                .collect { captures ->
                    _uiState.update {
                        it.copy(pendingCount = captures.size)
                    }
                }
        }
    }

    // ========== Phase 2: 멀티모달 캡처 기능 ==========

    /**
     * 캡처 모드 변경
     */
    fun onCaptureModeChanged(mode: CaptureMode) {
        _uiState.update {
            it.copy(
                captureMode = mode,
                errorMessage = null,
                // 모드 변경 시 상태 초기화
                selectedImageUri = null,
                isRecording = false
            )
        }
    }

    /**
     * M05: 이미지 선택/촬영 후 OCR 처리
     */
    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            // 로딩 시작
            _uiState.update {
                it.copy(
                    selectedImageUri = uri,
                    isLoading = true,
                    errorMessage = null
                )
            }

            // OCR + 캡처 제출
            when (val result = submitImageCaptureUseCase(uri)) {
                is Result.Success -> {
                    // 성공
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showSuccessFeedback = true,
                            isOffline = result.data.syncStatus != SyncStatus.SYNCED,
                            selectedImageUri = null
                        )
                    }

                    // 피드백 표시 후 자동 숨김
                    delay(2000)
                    _uiState.update { it.copy(showSuccessFeedback = false) }
                }
                is Result.Error -> {
                    // 에러
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "이미지 처리 실패",
                            selectedImageUri = null
                        )
                    }
                }
                is Result.Loading -> {
                    // 로딩 상태는 위에서 이미 처리
                }
            }
        }
    }

    /**
     * M06: 음성 녹음 시작 (서버 중심 아키텍처)
     * MediaRecorder를 사용하여 오디오 파일 녹음만 수행
     */
    fun onStartVoiceRecording() {
        val result = voiceRecognizer.startRecording()

        result.fold(
            onSuccess = {
                _uiState.update {
                    it.copy(
                        isRecording = true,
                        errorMessage = null
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isRecording = false,
                        errorMessage = error.message ?: "녹음 시작 실패"
                    )
                }
            }
        )
    }

    /**
     * M06: 음성 녹음 중지 및 서버 STT 처리
     * 녹음 중지 후 오디오 파일을 서버로 업로드하여 텍스트 변환
     */
    fun onStopVoiceRecording() {
        viewModelScope.launch {
            // 녹음 중지
            val stopResult = voiceRecognizer.stopRecording()

            stopResult.fold(
                onSuccess = {
                    // 녹음 중지 성공 → 서버 STT 처리
                    _uiState.update {
                        it.copy(
                            isRecording = false,
                            isLoading = true,  // 서버 처리 중 로딩 표시
                            errorMessage = null
                        )
                    }

                    // 서버로 오디오 업로드 및 STT 처리
                    when (val result = voiceRecognizer.uploadAndTranscribe()) {
                        is Result.Success -> {
                            // STT 성공 → 캡처 제출
                            onVoiceRecognitionResult(result.data)
                        }
                        is Result.Error -> {
                            // STT 실패
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = result.exception.message ?: "음성 인식 실패"
                                )
                            }
                        }
                        is Result.Loading -> {
                            // 로딩 상태는 이미 설정됨
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isRecording = false,
                            errorMessage = error.message ?: "녹음 중지 실패"
                        )
                    }
                    // 리소스 정리
                    voiceRecognizer.cleanup()
                }
            )
        }
    }

    /**
     * M06: 음성 녹음 취소
     * 녹음 중 취소 시 리소스 정리
     */
    fun onCancelVoiceRecording() {
        voiceRecognizer.cleanup()
        _uiState.update {
            it.copy(
                isRecording = false,
                errorMessage = null
            )
        }
    }

    /**
     * 음성 인식 결과 처리
     */
    private fun onVoiceRecognitionResult(text: String) {
        viewModelScope.launch {
            // 로딩 시작
            _uiState.update {
                it.copy(
                    isRecording = false,
                    isLoading = true
                )
            }

            // 음성 캡처 제출
            when (val result = submitVoiceCaptureUseCase(text, null)) {
                is Result.Success -> {
                    // 성공
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showSuccessFeedback = true,
                            isOffline = result.data.syncStatus != SyncStatus.SYNCED
                        )
                    }

                    // 피드백 표시 후 자동 숨김
                    delay(2000)
                    _uiState.update { it.copy(showSuccessFeedback = false) }
                }
                is Result.Error -> {
                    // 에러
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "음성 처리 실패"
                        )
                    }
                }
                is Result.Loading -> {
                    // 로딩 상태는 위에서 이미 처리
                }
            }
        }
    }

    /**
     * M08: 웹 URL 입력 후 메타데이터 추출
     */
    fun onWebUrlEntered(url: String) {
        // URL 검증
        if (url.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "URL을 입력해주세요")
            }
            return
        }

        viewModelScope.launch {
            // 로딩 시작
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            // 웹 클립 제출
            when (val result = submitWebClipUseCase(url)) {
                is Result.Success -> {
                    // 성공
                    _uiState.update {
                        it.copy(
                            inputText = "",  // URL 필드 클리어
                            isLoading = false,
                            showSuccessFeedback = true,
                            isOffline = result.data.syncStatus != SyncStatus.SYNCED
                        )
                    }

                    // 피드백 표시 후 자동 숨김
                    delay(2000)
                    _uiState.update { it.copy(showSuccessFeedback = false) }
                }
                is Result.Error -> {
                    // 에러
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "웹 페이지 처리 실패"
                        )
                    }
                }
                is Result.Loading -> {
                    // 로딩 상태는 위에서 이미 처리
                }
            }
        }
    }

    /**
     * 공유 인텐트로 받은 텍스트 처리 (M07)
     */
    fun onSharedTextReceived(text: String) {
        // URL인 경우 WEB_CLIP 모드로 처리
        if (text.startsWith("http://") || text.startsWith("https://")) {
            _uiState.update { it.copy(captureMode = CaptureMode.WEB_CLIP) }
            onWebUrlEntered(text)
        } else {
            // 일반 텍스트는 TEXT 모드로 처리
            _uiState.update {
                it.copy(
                    captureMode = CaptureMode.TEXT,
                    inputText = text
                )
            }
        }
    }

    /**
     * 공유 인텐트로 받은 이미지 처리 (M07)
     */
    fun onSharedImageReceived(uri: Uri) {
        _uiState.update { it.copy(captureMode = CaptureMode.IMAGE) }
        onImageSelected(uri)
    }

    // ========== Phase 3: 스마트 처리 기능 ==========

    /**
     * M09: AI 요약 생성 (비동기)
     * 캡처 성공 후 백그라운드에서 요약 생성
     */
    private fun generateSummaryForCapture(captureId: String, content: String) {
        viewModelScope.launch {
            when (val result = generateSummaryUseCase(captureId, content)) {
                is Result.Success -> {
                    // 요약 결과를 UI 상태에 업데이트
                    _uiState.update {
                        it.copy(latestSummary = result.data)
                    }
                }
                is Result.Error -> {
                    // 요약 실패는 치명적이지 않으므로 로그만 남김
                    // 사용자에게 에러를 표시하지 않음
                }
                is Result.Loading -> {
                    // 이미 비동기로 처리 중
                }
            }
        }
    }

    /**
     * M10: 스마트 태그 제안 (비동기)
     * 캡처 성공 후 백그라운드에서 태그 제안
     */
    private fun suggestTagsForCapture(content: String, classificationType: String?) {
        viewModelScope.launch {
            when (val result = suggestTagsUseCase(content, classificationType)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(suggestedTags = result.data)
                    }
                }
                is Result.Error -> {
                    // 태그 제안 실패는 치명적이지 않으므로 로그만 남김
                }
                is Result.Loading -> {
                    // 이미 비동기로 처리 중
                }
            }
        }
    }

    /**
     * M10: 제안된 태그 선택
     * 사용자가 제안된 태그를 탭하면 호출
     */
    fun onSuggestedTagSelected(tagName: String) {
        // 선택된 태그를 제거하여 UI 업데이트
        _uiState.update {
            it.copy(
                suggestedTags = it.suggestedTags.filter { tag -> tag.name != tagName }
            )
        }
        // TODO: 태그를 Capture에 적용하는 API 호출 (서버 구현 후)
    }

    /**
     * M09: 요약 펼침/접기 토글
     */
    fun onToggleSummaryExpanded() {
        _uiState.update {
            it.copy(isSummaryExpanded = !it.isSummaryExpanded)
        }
    }

    /**
     * M09/M10: 스마트 처리 결과 닫기
     */
    fun onDismissSmartFeatures() {
        _uiState.update {
            it.copy(
                latestSummary = null,
                suggestedTags = emptyList()
            )
        }
    }

    /**
     * ViewModel 정리 시 VoiceRecognizer 리소스 해제
     */
    override fun onCleared() {
        super.onCleared()
        voiceRecognizer.cleanup()
    }
}
