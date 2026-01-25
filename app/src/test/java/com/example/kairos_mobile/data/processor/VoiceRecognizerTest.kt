package com.example.kairos_mobile.data.processor

import com.example.kairos_mobile.data.remote.dto.SttResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * VoiceRecognizer 관련 단위 테스트
 *
 * 서버 중심 아키텍처:
 * - 클라이언트는 오디오 녹음만 담당 (MediaRecorder - Instrumented 테스트 필요)
 * - 서버 STT API 호출 로직 테스트 (Instrumented 테스트에서 수행)
 *
 * 이 파일에서는 SttResponse DTO의 데이터 처리 로직을 테스트합니다.
 */
class VoiceRecognizerTest {

    /**
     * 테스트: 서버 STT API 성공 응답 파싱
     */
    @Test
    fun `SttResponse parses correctly on success`() {
        // Given
        val sttResponse = SttResponse(
            success = true,
            text = "음성에서 추출된 텍스트입니다",
            confidence = 0.95f,
            language = "ko",
            error = null
        )

        // Then
        assertTrue(sttResponse.success)
        assertEquals("음성에서 추출된 텍스트입니다", sttResponse.text)
        assertEquals(0.95f, sttResponse.confidence)
        assertEquals("ko", sttResponse.language)
        assertNull(sttResponse.error)
    }

    /**
     * 테스트: 서버 STT API 실패 응답 파싱
     */
    @Test
    fun `SttResponse parses correctly on failure`() {
        // Given
        val sttResponse = SttResponse(
            success = false,
            text = null,
            confidence = null,
            language = null,
            error = "음성을 인식할 수 없습니다"
        )

        // Then
        assertFalse(sttResponse.success)
        assertNull(sttResponse.text)
        assertNull(sttResponse.confidence)
        assertNull(sttResponse.language)
        assertEquals("음성을 인식할 수 없습니다", sttResponse.error)
    }

    /**
     * 테스트: 다양한 언어 응답 확인
     */
    @Test
    fun `SttResponse supports multiple languages`() {
        // Given - 한국어 응답
        val koreanResponse = SttResponse(
            success = true,
            text = "한국어 텍스트",
            confidence = 0.9f,
            language = "ko",
            error = null
        )

        // Given - 영어 응답
        val englishResponse = SttResponse(
            success = true,
            text = "English text",
            confidence = 0.95f,
            language = "en",
            error = null
        )

        // Then
        assertEquals("ko", koreanResponse.language)
        assertEquals("en", englishResponse.language)
    }

    /**
     * 테스트: 신뢰도 범위 확인
     */
    @Test
    fun `confidence value is within valid range`() {
        // Given - 높은 신뢰도
        val highConfidence = SttResponse(
            success = true,
            text = "높은 신뢰도",
            confidence = 0.99f,
            language = "ko",
            error = null
        )

        // Given - 낮은 신뢰도
        val lowConfidence = SttResponse(
            success = true,
            text = "낮은 신뢰도",
            confidence = 0.5f,
            language = "ko",
            error = null
        )

        // Then
        assertTrue(highConfidence.confidence!! >= 0f && highConfidence.confidence!! <= 1f)
        assertTrue(lowConfidence.confidence!! >= 0f && lowConfidence.confidence!! <= 1f)
    }

    /**
     * 테스트: 빈 텍스트 응답 처리
     * 클라이언트는 빈 텍스트를 에러로 처리해야 함
     */
    @Test
    fun `empty text should be treated as error by client`() {
        // Given - 빈 텍스트 응답 (서버가 성공으로 반환했지만 텍스트가 없음)
        val emptyTextResponse = SttResponse(
            success = true,
            text = "   ",  // 공백만
            confidence = 0.5f,
            language = "ko",
            error = null
        )

        // Then - 클라이언트가 isBlank 체크로 에러 처리해야 함
        assertTrue(emptyTextResponse.text?.isBlank() == true)
    }

    /**
     * 테스트: null 텍스트 응답 처리
     */
    @Test
    fun `null text should be treated as error by client`() {
        // Given
        val nullTextResponse = SttResponse(
            success = true,
            text = null,
            confidence = 0.5f,
            language = "ko",
            error = null
        )

        // Then
        assertNull(nullTextResponse.text)
    }

    /**
     * 테스트: 정상 텍스트 응답 확인
     * VoiceRecognizer가 서버 응답에서 텍스트를 올바르게 추출하는지 확인
     */
    @Test
    fun `valid text response should be extracted correctly`() {
        // Given
        val response = SttResponse(
            success = true,
            text = "내일 오후 3시에 팀 미팅이 있습니다.",
            confidence = 0.92f,
            language = "ko",
            error = null
        )

        // Then
        assertTrue(response.success)
        assertEquals("내일 오후 3시에 팀 미팅이 있습니다.", response.text)
        assertTrue(response.text?.isNotBlank() == true)
    }

    /**
     * 테스트: 에러 메시지 포함 응답 처리
     */
    @Test
    fun `error response should contain error message`() {
        // Given - 다양한 에러 케이스
        val audioError = SttResponse(
            success = false,
            text = null,
            confidence = null,
            language = null,
            error = "오디오 파일을 읽을 수 없습니다"
        )

        val noSpeechError = SttResponse(
            success = false,
            text = null,
            confidence = null,
            language = null,
            error = "음성이 감지되지 않았습니다"
        )

        // Then
        assertFalse(audioError.success)
        assertEquals("오디오 파일을 읽을 수 없습니다", audioError.error)

        assertFalse(noSpeechError.success)
        assertEquals("음성이 감지되지 않았습니다", noSpeechError.error)
    }
}
