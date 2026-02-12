package com.flit.app.domain.model

/**
 * 비동기 작업의 결과를 나타내는 Sealed Class
 */
sealed class Result<out T> {
    /**
     * 성공 - 데이터 포함
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * 에러 - 예외 정보 포함
     */
    data class Error(val exception: Throwable) : Result<Nothing>()

    /**
     * 로딩 중
     */
    data object Loading : Result<Nothing>()
}
