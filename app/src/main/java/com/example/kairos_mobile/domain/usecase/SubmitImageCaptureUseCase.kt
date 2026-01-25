package com.example.kairos_mobile.domain.usecase

import android.net.Uri
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.model.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M05: 이미지 캡처 + OCR Use Case
 *
 * 이미지를 받아 OCR로 텍스트를 추출하고 Capture를 생성합니다.
 */
@Singleton
class SubmitImageCaptureUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {

    /**
     * 이미지 URI를 받아 OCR 처리 후 Capture 제출
     *
     * @param imageUri 이미지 URI
     * @return OCR 처리 및 제출 결과
     */
    suspend operator fun invoke(imageUri: Uri): Result<Capture> {
        return captureRepository.submitImageCapture(imageUri)
    }
}
