package com.example.kairos_mobile.data.processor

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.domain.model.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M06: 음성 인식 프로세서 (서버 중심 아키텍처)
 *
 * MediaRecorder를 사용하여 음성을 녹음하고,
 * 서버 STT API로 업로드하여 텍스트를 추출합니다.
 *
 * 아키텍처 원칙:
 * - 클라이언트: 음성 녹음만 담당
 * - 서버: STT 처리 담당 (Claude API 또는 Whisper API 사용)
 */
@Singleton
class VoiceRecognizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: KairosApi
) {

    /**
     * 음성 인식 상태
     */
    sealed class VoiceRecognitionState {
        object Ready : VoiceRecognitionState()
        object Recording : VoiceRecognitionState()
        object Processing : VoiceRecognitionState()
        data class Result(val text: String) : VoiceRecognitionState()
        data class Error(val message: String) : VoiceRecognitionState()
    }

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false

    /**
     * 음성 녹음 시작
     *
     * @return 성공 여부
     */
    fun startRecording(): kotlin.Result<Unit> {
        return try {
            if (isRecording) {
                return kotlin.Result.failure(Exception("이미 녹음 중입니다"))
            }

            // 임시 오디오 파일 생성
            audioFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")

            // MediaRecorder 초기화
            mediaRecorder = createMediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile!!.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            kotlin.Result.success(Unit)
        } catch (e: Exception) {
            cleanup()
            kotlin.Result.failure(Exception("녹음 시작 실패: ${e.message}"))
        }
    }

    /**
     * 음성 녹음 중지
     */
    fun stopRecording(): kotlin.Result<Unit> {
        return try {
            if (!isRecording) {
                return kotlin.Result.failure(Exception("녹음 중이 아닙니다"))
            }

            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            kotlin.Result.success(Unit)
        } catch (e: Exception) {
            cleanup()
            kotlin.Result.failure(Exception("녹음 중지 실패: ${e.message}"))
        }
    }

    /**
     * 녹음된 오디오 파일을 서버로 업로드하고 STT 처리
     *
     * @return 변환된 텍스트 또는 에러
     */
    suspend fun uploadAndTranscribe(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = audioFile
            if (file == null || !file.exists()) {
                return@withContext Result.Error(Exception("오디오 파일이 없습니다"))
            }

            // Multipart 요청 생성
            val requestBody = file.asRequestBody("audio/m4a".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData(
                "audioFile",
                file.name,
                requestBody
            )

            // 서버 STT API 호출
            val response = api.extractTextFromAudio(multipartBody)

            if (response.isSuccessful && response.body()?.success == true) {
                val text = response.body()?.text ?: ""
                if (text.isBlank()) {
                    Result.Error(Exception("음성을 인식하지 못했습니다"))
                } else {
                    Result.Success(text)
                }
            } else {
                val errorMessage = response.body()?.error ?: "음성 인식 실패"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(Exception("서버 통신 오류: ${e.message}"))
        } finally {
            // 임시 파일 삭제
            audioFile?.delete()
            audioFile = null
        }
    }

    /**
     * 녹음 중 여부 확인
     */
    fun isCurrentlyRecording(): Boolean = isRecording

    /**
     * 녹음된 파일이 존재하는지 확인
     */
    fun hasRecordedFile(): Boolean = audioFile?.exists() == true

    /**
     * 리소스 정리
     */
    fun cleanup() {
        try {
            mediaRecorder?.apply {
                if (isRecording) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            // 정리 중 오류 무시
        }
        mediaRecorder = null
        isRecording = false
        audioFile?.delete()
        audioFile = null
    }

    /**
     * Android 버전에 따른 MediaRecorder 생성
     */
    @Suppress("DEPRECATION")
    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }
}
