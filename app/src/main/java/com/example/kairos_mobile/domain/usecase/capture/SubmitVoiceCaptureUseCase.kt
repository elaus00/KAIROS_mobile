package com.example.kairos_mobile.domain.usecase.capture

import android.net.Uri
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 음성 캡처 제출 Use Case
 *
 * 비즈니스 로직:
 * - 음성 인식 결과 텍스트로 캡처 제출
 */
@Singleton
class SubmitVoiceCaptureUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    /**
     * 음성 캡처 제출
     */
    suspend operator fun invoke(audioText: String, audioUri: Uri? = null): Result<Capture> {
        if (audioText.isBlank()) {
            return Result.Error(IllegalArgumentException("Audio text cannot be empty"))
        }
        return repository.submitVoiceCapture(audioText, audioUri)
    }
}
