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
import javax.inject.Singleton

/**
 * Network Hilt 모듈
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
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.kairos.mock/")  // Mock URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * KairosApi 제공
     * Phase 1: Mock API 사용
     * Phase 2: 실제 API로 전환 시 주석 변경
     */
    @Provides
    @Singleton
    fun provideKairosApi(
        // retrofit: Retrofit  // 실제 API 사용 시 필요
    ): KairosApi {
        // Phase 1: Mock API 사용
        return MockKairosApi()

        // Phase 2: 실제 API 사용 시
        // return retrofit.create(KairosApi::class.java)
    }
}
