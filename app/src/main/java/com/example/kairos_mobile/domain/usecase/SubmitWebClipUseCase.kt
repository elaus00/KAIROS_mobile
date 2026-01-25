package com.example.kairos_mobile.domain.usecase

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.model.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M08: 웹 클립 Use Case
 *
 * URL을 받아 메타데이터를 추출하고 Capture를 생성합니다.
 */
@Singleton
class SubmitWebClipUseCase @Inject constructor(
    private val captureRepository: CaptureRepository
) {

    /**
     * URL에서 메타데이터 추출 후 Capture 제출
     *
     * @param url 웹 페이지 URL
     * @return 메타데이터 추출 및 제출 결과
     */
    suspend operator fun invoke(url: String): Result<Capture> {
        return captureRepository.submitWebClip(url)
    }
}
