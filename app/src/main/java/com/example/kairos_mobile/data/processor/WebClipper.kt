package com.example.kairos_mobile.data.processor

import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.WebClipRequest
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.WebMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M08: 웹 클립 프로세서
 *
 * 서버 API를 사용하여 웹 페이지에서 메타데이터를 추출합니다.
 */
@Singleton
class WebClipper @Inject constructor(
    private val api: KairosApi
) {

    /**
     * URL에서 메타데이터 추출
     *
     * @param url 웹 페이지 URL
     * @return 추출된 메타데이터 또는 에러
     */
    suspend fun extractMetadata(url: String): Result<WebMetadata> = withContext(Dispatchers.IO) {
        try {
            // URL 유효성 검증
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return@withContext Result.Error(Exception("유효하지 않은 URL입니다"))
            }

            // 서버 API 호출
            val request = WebClipRequest(url = url)
            val response = api.extractWebClip(request)

            if (response.isSuccessful && response.body() != null) {
                val webClipResponse = response.body()!!
                if (webClipResponse.success) {
                    Result.Success(
                        WebMetadata(
                            url = url,
                            title = webClipResponse.title,
                            description = webClipResponse.description ?: webClipResponse.content,
                            imageUrl = webClipResponse.imageUrl
                        )
                    )
                } else {
                    val errorMsg = webClipResponse.error ?: "웹 페이지 정보를 가져올 수 없습니다"
                    Result.Error(Exception(errorMsg))
                }
            } else {
                Result.Error(Exception("WebClip API 호출 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(Exception("웹 메타데이터 추출 실패: ${e.message}", e))
        }
    }
}
