package com.example.kairos_mobile.di

import com.example.kairos_mobile.BuildConfig
import com.example.kairos_mobile.data.remote.api.KairosApi
import com.example.kairos_mobile.data.remote.api.MockKairosApi
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
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Mock API 사용 여부 Qualifier
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UseMockApi

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
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
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
     * KairosApi 제공
     * USE_MOCK_API 플래그에 따라 Mock/실제 API 선택
     */
    @Provides
    @Singleton
    fun provideKairosApi(
        retrofit: Retrofit
    ): KairosApi {
        return if (BuildConfig.USE_MOCK_API) {
            // Mock API 사용 (로컬 테스트용)
            MockKairosApi()
        } else {
            // 실제 API 사용 (서버 연동)
            retrofit.create(KairosApi::class.java)
        }
    }
}
