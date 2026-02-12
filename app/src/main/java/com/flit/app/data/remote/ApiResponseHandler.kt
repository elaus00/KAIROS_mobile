package com.flit.app.data.remote

import com.flit.app.data.remote.dto.v2.ApiEnvelope
import com.flit.app.data.remote.dto.v2.ApiErrorResponse
import com.flit.app.domain.model.ApiException
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException

/**
 * API 응답 처리 유틸리티
 * Response<ApiEnvelope<T>> → T 또는 ApiException 변환
 */
object ApiResponseHandler {

    /**
     * API 응답에서 데이터를 추출하거나 적절한 ApiException을 throw
     */
    fun <T> unwrap(response: Response<ApiEnvelope<T>>): T {
        val body = response.body()

        if (response.isSuccessful && body?.status == "ok" && body.data != null) {
            return body.data
        }

        // 응답 본문에 에러 코드가 있는 경우
        val error = body?.error ?: tryParseErrorBody(response)
        throw mapToException(response.code(), error)
    }

    /**
     * API 호출을 안전하게 실행 (네트워크 에러 포함)
     */
    suspend fun <T> safeCall(block: suspend () -> Response<ApiEnvelope<T>>): T {
        return try {
            unwrap(block())
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw ApiException.NetworkError(cause = e)
        }
    }

    /**
     * HTTP 상태 코드 + 에러 코드 → ApiException 매핑
     */
    fun mapToException(httpCode: Int, error: ApiErrorResponse?): ApiException {
        val code = error?.code
        val message = error?.message

        return when {
            // 서버 에러 코드 기반 매핑 (우선)
            code == "RATE_LIMITED" -> ApiException.RateLimited(message ?: "요청 한도를 초과했습니다.")
            code == "AI_SERVICE_UNAVAILABLE" -> ApiException.ServiceUnavailable(message ?: "AI 서비스를 사용할 수 없습니다.")
            code == "CLASSIFICATION_TIMEOUT" -> ApiException.ClassificationTimeout(message ?: "분류 시간이 초과되었습니다.")
            code in setOf("TEXT_EMPTY", "TEXT_TOO_LONG", "INVALID_REQUEST") ->
                ApiException.InvalidRequest(message ?: "잘못된 요청입니다.", code)

            // HTTP 상태 코드 기반 폴백
            httpCode == 401 -> ApiException.Unauthorized(message ?: "인증이 필요합니다.")
            httpCode == 403 -> ApiException.SubscriptionRequired(message ?: "프리미엄 구독이 필요합니다.")
            httpCode == 429 -> ApiException.RateLimited(message ?: "요청 한도를 초과했습니다.")
            httpCode in 400..499 -> ApiException.InvalidRequest(message ?: "요청 처리에 실패했습니다.", code)
            httpCode == 503 -> ApiException.ServiceUnavailable(message ?: "서비스를 일시적으로 사용할 수 없습니다.")
            httpCode == 504 -> ApiException.ClassificationTimeout(message ?: "요청 시간이 초과되었습니다.")
            httpCode in 500..599 -> ApiException.ServerError(message ?: "서버 오류가 발생했습니다.", code)

            else -> ApiException.ServerError(message ?: "알 수 없는 오류가 발생했습니다.", code)
        }
    }

    /**
     * errorBody에서 ApiErrorResponse 파싱 시도
     */
    private fun <T> tryParseErrorBody(response: Response<T>): ApiErrorResponse? {
        return try {
            val errorString = response.errorBody()?.string() ?: return null
            val envelope = Gson().fromJson(errorString, ErrorOnlyEnvelope::class.java)
            envelope?.error
        } catch (_: Exception) {
            null
        }
    }

    /** errorBody 파싱용 내부 클래스 */
    private data class ErrorOnlyEnvelope(
        val status: String? = null,
        val error: ApiErrorResponse? = null
    )
}
