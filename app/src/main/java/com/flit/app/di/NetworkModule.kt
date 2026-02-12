package com.flit.app.di

import com.flit.app.BuildConfig
import com.flit.app.data.remote.api.FlitApi
import com.flit.app.data.remote.interceptor.AuthInterceptor
import com.flit.app.data.remote.interceptor.DeviceIdInterceptor
import com.flit.app.data.remote.interceptor.ErrorInterceptor
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Network Hilt 모듈 (API v2.1)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * OkHttpClient 제공
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        deviceIdInterceptor: DeviceIdInterceptor,
        errorInterceptor: ErrorInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(errorInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(deviceIdInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Retrofit 제공
     * Base URL: BuildConfig.API_BASE_URL (debug: 10.0.2.2:8000, release: production URL)
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL + "/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * FlitApi 제공
     */
    @Provides
    @Singleton
    fun provideFlitApi(
        retrofit: Retrofit
    ): FlitApi {
        return retrofit.create(FlitApi::class.java)
    }
}
