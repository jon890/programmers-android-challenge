package com.programmers.flomusicplayer.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiProvider {

    private const val BASE_URL =
        "https://grepp-programmers-challenges.s3.ap-northeast-2.amazonaws.com/2020-flo/";

    fun provideFloApi(): FloRestful = getRetrofitBuild().create(FloRestful::class.java)

    private fun getRetrofitBuild() = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(getOkHttpClient()) // Http Client
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // Call => Observable
        .addConverterFactory(getGsonConverter()) // Gson Converter
        .build()

    private fun getGsonConverter() = GsonConverterFactory.create()


    private fun getOkHttpClient() = OkHttpClient.Builder().apply {
        // Timeout 시간 설정
        readTimeout(60, TimeUnit.SECONDS)
        connectTimeout(60, TimeUnit.SECONDS)
        writeTimeout(5, TimeUnit.SECONDS)

        // 네트워크 요청 / 응답 로그 표시
        addInterceptor(getLoggingInterceptor())
    }.build()

    private fun getLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
}