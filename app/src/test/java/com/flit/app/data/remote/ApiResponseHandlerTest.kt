package com.flit.app.data.remote

import com.flit.app.data.remote.dto.v2.ApiEnvelope
import com.flit.app.data.remote.dto.v2.ApiErrorResponse
import com.flit.app.domain.model.ApiException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * ApiResponseHandler 유닛 테스트
 * API 응답 추출 + 에러 매핑 검증
 */
class ApiResponseHandlerTest {

    // === unwrap 성공 케이스 ===

    @Test
    fun `성공 응답에서 data 추출`() {
        val envelope = ApiEnvelope(status = "ok", data = "hello")
        val response = Response.success(envelope)
        val result = ApiResponseHandler.unwrap(response)
        assertEquals("hello", result)
    }

    @Test
    fun `성공 응답 - 복합 데이터 추출`() {
        data class TestData(val count: Int)
        val envelope = ApiEnvelope(status = "ok", data = TestData(count = 42))
        val response = Response.success(envelope)
        val result = ApiResponseHandler.unwrap(response)
        assertEquals(42, result.count)
    }

    // === unwrap 에러 케이스 ===

    @Test(expected = ApiException.ServerError::class)
    fun `status ok지만 data null이면 예외`() {
        val envelope = ApiEnvelope<String>(status = "ok", data = null)
        val response = Response.success(envelope)
        ApiResponseHandler.unwrap(response)
    }

    @Test(expected = ApiException::class)
    fun `status error면 예외`() {
        val envelope = ApiEnvelope<String>(
            status = "error",
            data = null,
            error = ApiErrorResponse("INVALID_REQUEST", "잘못된 요청")
        )
        val response = Response.success(envelope)
        ApiResponseHandler.unwrap(response)
    }

    @Test
    fun `HTTP 400 + TEXT_EMPTY → InvalidRequest`() {
        val errorBody = """{"status":"error","error":{"code":"TEXT_EMPTY","message":"텍스트가 비어있습니다"}}"""
        val response = Response.error<ApiEnvelope<String>>(400, errorBody.toResponseBody())
        try {
            ApiResponseHandler.unwrap(response)
        } catch (e: ApiException.InvalidRequest) {
            assertEquals("TEXT_EMPTY", e.errorCode)
            return
        }
        throw AssertionError("InvalidRequest 예외가 발생해야 합니다")
    }

    @Test
    fun `HTTP 400 + TEXT_TOO_LONG → InvalidRequest`() {
        val errorBody = """{"status":"error","error":{"code":"TEXT_TOO_LONG","message":"텍스트가 너무 깁니다"}}"""
        val response = Response.error<ApiEnvelope<String>>(400, errorBody.toResponseBody())
        try {
            ApiResponseHandler.unwrap(response)
        } catch (e: ApiException.InvalidRequest) {
            assertEquals("TEXT_TOO_LONG", e.errorCode)
            return
        }
        throw AssertionError("InvalidRequest 예외가 발생해야 합니다")
    }

    @Test
    fun `HTTP 429 + RATE_LIMITED → RateLimited`() {
        val errorBody = """{"status":"error","error":{"code":"RATE_LIMITED","message":"요청 한도 초과"}}"""
        val response = Response.error<ApiEnvelope<String>>(429, errorBody.toResponseBody())
        try {
            ApiResponseHandler.unwrap(response)
        } catch (e: ApiException.RateLimited) {
            return
        }
        throw AssertionError("RateLimited 예외가 발생해야 합니다")
    }

    @Test
    fun `HTTP 503 + AI_SERVICE_UNAVAILABLE → ServiceUnavailable`() {
        val errorBody = """{"status":"error","error":{"code":"AI_SERVICE_UNAVAILABLE","message":"AI 서비스 불가"}}"""
        val response = Response.error<ApiEnvelope<String>>(503, errorBody.toResponseBody())
        try {
            ApiResponseHandler.unwrap(response)
        } catch (e: ApiException.ServiceUnavailable) {
            return
        }
        throw AssertionError("ServiceUnavailable 예외가 발생해야 합니다")
    }

    @Test
    fun `HTTP 504 → ClassificationTimeout`() {
        val errorBody = """{"status":"error","error":{"code":"CLASSIFICATION_TIMEOUT","message":"타임아웃"}}"""
        val response = Response.error<ApiEnvelope<String>>(504, errorBody.toResponseBody())
        try {
            ApiResponseHandler.unwrap(response)
        } catch (e: ApiException.ClassificationTimeout) {
            return
        }
        throw AssertionError("ClassificationTimeout 예외가 발생해야 합니다")
    }

    @Test
    fun `HTTP 500 → ServerError`() {
        val errorBody = """{"status":"error","error":{"code":"INTERNAL_ERROR","message":"서버 오류"}}"""
        val response = Response.error<ApiEnvelope<String>>(500, errorBody.toResponseBody())
        try {
            ApiResponseHandler.unwrap(response)
        } catch (e: ApiException.ServerError) {
            return
        }
        throw AssertionError("ServerError 예외가 발생해야 합니다")
    }

    @Test
    fun `에러 본문 파싱 실패 시 HTTP 코드 기반 폴백`() {
        val response = Response.error<ApiEnvelope<String>>(500, "not json".toResponseBody())
        try {
            ApiResponseHandler.unwrap(response)
        } catch (e: ApiException.ServerError) {
            return
        }
        throw AssertionError("ServerError 예외가 발생해야 합니다")
    }

    // === mapToException 직접 테스트 ===

    @Test
    fun `서버 에러 코드가 HTTP 코드보다 우선`() {
        // HTTP 400이지만 서버 코드가 RATE_LIMITED이면 RateLimited로 매핑
        val error = ApiErrorResponse("RATE_LIMITED", "한도 초과")
        val exception = ApiResponseHandler.mapToException(400, error)
        assertTrue(exception is ApiException.RateLimited)
    }

    @Test
    fun `서버 코드 없으면 HTTP 코드로 폴백`() {
        val exception = ApiResponseHandler.mapToException(429, null)
        assertTrue(exception is ApiException.RateLimited)
    }

    @Test
    fun `HTTP 401 → Unauthorized`() {
        val exception = ApiResponseHandler.mapToException(401, null)
        assertTrue(exception is ApiException.Unauthorized)
    }

    @Test
    fun `HTTP 403 → SubscriptionRequired`() {
        val exception = ApiResponseHandler.mapToException(403, null)
        assertTrue(exception is ApiException.SubscriptionRequired)
    }

    @Test
    fun `Unauthorized는 재시도 불가`() {
        assertFalse(ApiException.Unauthorized().isRetryable)
    }

    @Test
    fun `SubscriptionRequired는 재시도 불가`() {
        assertFalse(ApiException.SubscriptionRequired().isRetryable)
    }

    // === isRetryable 검증 ===

    @Test
    fun `InvalidRequest는 재시도 불가`() {
        assertFalse(ApiException.InvalidRequest().isRetryable)
    }

    @Test
    fun `RateLimited는 재시도 불가`() {
        assertFalse(ApiException.RateLimited().isRetryable)
    }

    @Test
    fun `ServiceUnavailable은 재시도 가능`() {
        assertTrue(ApiException.ServiceUnavailable().isRetryable)
    }

    @Test
    fun `ClassificationTimeout은 재시도 가능`() {
        assertTrue(ApiException.ClassificationTimeout().isRetryable)
    }

    @Test
    fun `ServerError는 재시도 가능`() {
        assertTrue(ApiException.ServerError().isRetryable)
    }

    @Test
    fun `NetworkError는 재시도 가능`() {
        assertTrue(ApiException.NetworkError().isRetryable)
    }
}
