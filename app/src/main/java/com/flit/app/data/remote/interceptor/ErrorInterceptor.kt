package com.flit.app.data.remote.interceptor

import com.flit.app.domain.model.ApiException
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 네트워크 에러 인터셉터
 * IOException을 ApiException.NetworkError로 변환하여 일관된 에러 처리 제공
 *
 * 참고: HTTP 4xx/5xx 에러는 Retrofit Response로 전달되므로
 * ApiResponseHandler.unwrap()에서 처리한다.
 * 이 인터셉터는 네트워크 레벨 에러만 담당한다.
 */
@Singleton
class ErrorInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (e: SocketTimeoutException) {
            throw ApiException.NetworkError("요청 시간이 초과되었습니다.", e)
        } catch (e: UnknownHostException) {
            throw ApiException.NetworkError("서버에 연결할 수 없습니다.", e)
        } catch (e: IOException) {
            throw ApiException.NetworkError("네트워크 연결에 실패했습니다.", e)
        }
    }
}
