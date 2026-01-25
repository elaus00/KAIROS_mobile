package com.example.kairos_mobile.data.processor

import android.content.Context
import android.net.Uri
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.domain.model.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M05: OCR 처리 프로세서
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
            // URI에서 임시 파일 생성
            val tempFile = createTempFileFromUri(imageUri)
                ?: return@withContext Result.Error(Exception("이미지 파일을 읽을 수 없습니다"))

            // Multipart 요청 생성
            val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData(
                "image",
                tempFile.name,
                requestBody
            )

            // 서버 API 호출
            val response = api.extractTextFromImage(multipartBody)

            // 임시 파일 삭제
            tempFile.delete()

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
     * URI에서 임시 파일 생성
     */
    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("ocr_", ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}
