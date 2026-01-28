package com.example.kairos_mobile.data.processor

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.dto.v2.OcrRequest
import com.example.kairos_mobile.domain.model.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M05: OCR 처리 프로세서 (API v2.1)
 *
 * 서버 API를 사용하여 이미지에서 텍스트를 추출합니다.
 */
@Singleton
class OcrProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: KairosApi
) {

    /**
     * 이미지 URI에서 텍스트 추출
     *
     * @param imageUri 이미지 URI
     * @return 추출된 텍스트 또는 에러
     */
    suspend fun extractText(imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            // URI에서 이미지 데이터 읽기
            val imageBytes = readImageBytes(imageUri)
                ?: return@withContext Result.Error(Exception("이미지 파일을 읽을 수 없습니다"))

            // Base64 인코딩
            val base64Data = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            // 이미지 타입 추출
            val imageType = getImageType(imageUri)

            // OCR 요청 생성 (API v2.1)
            val request = OcrRequest(
                imageData = base64Data,
                imageType = imageType,
                languageHint = "ko"  // 기본 언어 힌트: 한국어
            )

            // 서버 API 호출
            val response = api.ocr(request)

            if (response.isSuccessful && response.body() != null) {
                val ocrResponse = response.body()!!
                if (ocrResponse.success && !ocrResponse.text.isNullOrBlank()) {
                    Result.Success(ocrResponse.text)
                } else {
                    val errorMsg = ocrResponse.error ?: "이미지에서 텍스트를 찾을 수 없습니다"
                    Result.Error(Exception(errorMsg))
                }
            } else {
                Result.Error(Exception("OCR API 호출 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(Exception("OCR 처리 실패: ${e.message}", e))
        }
    }

    /**
     * URI에서 이미지 바이트 배열 읽기
     */
    private fun readImageBytes(uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * URI에서 이미지 타입 추출
     */
    private fun getImageType(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return when {
            mimeType?.contains("png") == true -> "png"
            mimeType?.contains("webp") == true -> "webp"
            else -> "jpeg"
        }
    }
}
