package com.example.kairos_mobile.domain.usecase.capture

import android.net.Uri
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.CaptureRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 이미지 캡처 제출 Use Case
 *
 * 비즈니스 로직:
 * - OCR로 텍스트 추출
 * - 추출된 텍스트로 캡처 제출
 */
@Singleton
class SubmitImageCaptureUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    /**
     * 이미지 캡처 제출
     */
    suspend operator fun invoke(imageUri: Uri): Result<Capture> {
        return repository.submitImageCapture(imageUri)
    }
}
