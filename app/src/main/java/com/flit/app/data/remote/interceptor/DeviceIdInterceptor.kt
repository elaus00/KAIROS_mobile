package com.flit.app.data.remote.interceptor

import com.flit.app.data.remote.DeviceIdProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdInterceptor @Inject constructor(
    private val deviceIdProvider: DeviceIdProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        val requiresDeviceId = path.endsWith("/classify") ||
            path.endsWith("/classify/batch") ||
            path.contains("/sync/") ||
            path.endsWith("/analytics/events")

        if (!requiresDeviceId) {
            return chain.proceed(request)
        }

        val deviceId = deviceIdProvider.getOrCreateDeviceId()
        val requestWithDeviceId = request.newBuilder()
            .header("X-Device-ID", deviceId)
            .build()
        return chain.proceed(requestWithDeviceId)
    }
}
