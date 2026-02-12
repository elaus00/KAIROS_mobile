package com.flit.app.domain.model

/**
 * API 에러 도메인 예외 계층
 * HTTP 에러 코드와 서버 에러 코드를 도메인 예외로 변환
 */
sealed class ApiException(
    message: String,
    val errorCode: String? = null
) : RuntimeException(message) {

    /** 400 — 잘못된 요청 (TEXT_EMPTY, TEXT_TOO_LONG, INVALID_REQUEST) */
    class InvalidRequest(
        message: String = "잘못된 요청입니다.",
        errorCode: String? = null
    ) : ApiException(message, errorCode)

    /** 429 — 요청 한도 초과 */
    class RateLimited(
        message: String = "요청 한도를 초과했습니다. 잠시 후 다시 시도하세요."
    ) : ApiException(message, "RATE_LIMITED")

    /** 503 — AI 서비스 불가 */
    class ServiceUnavailable(
        message: String = "서비스를 일시적으로 사용할 수 없습니다."
    ) : ApiException(message, "AI_SERVICE_UNAVAILABLE")

    /** 504 — 분류 타임아웃 */
    class ClassificationTimeout(
        message: String = "분류 처리 시간이 초과되었습니다."
    ) : ApiException(message, "CLASSIFICATION_TIMEOUT")

    /** 500 — 서버 내부 오류 */
    class ServerError(
        message: String = "서버 오류가 발생했습니다.",
        errorCode: String? = null
    ) : ApiException(message, errorCode)

    /** 네트워크 연결 불가 */
    class NetworkError(
        message: String = "네트워크에 연결할 수 없습니다.",
        cause: Throwable? = null
    ) : ApiException(message) {
        init {
            cause?.let { initCause(it) }
        }
    }

    /** 401 — 인증 필요 (토큰 만료 또는 미로그인) */
    class Unauthorized(
        message: String = "인증이 필요합니다."
    ) : ApiException(message, "UNAUTHORIZED")

    /** 403 — 프리미엄 구독 필요 */
    class SubscriptionRequired(
        message: String = "프리미엄 구독이 필요합니다."
    ) : ApiException(message, "SUBSCRIPTION_REQUIRED")

    /** 재시도 가능 여부 */
    val isRetryable: Boolean
        get() = when (this) {
            is ServiceUnavailable, is ClassificationTimeout, is ServerError, is NetworkError -> true
            is InvalidRequest, is RateLimited, is Unauthorized, is SubscriptionRequired -> false
        }
}
