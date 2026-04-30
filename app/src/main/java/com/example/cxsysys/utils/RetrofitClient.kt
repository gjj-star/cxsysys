package com.example.cxsysys.utils

import com.example.cxsysys.api.WeatherApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val WEATHER_BASE_URL = "https://uapis.cn/"
    // 业务接口的 Base URL，注意末尾必须有 '/'
    private const val BUSINESS_BASE_URL = "https://dbcx.org.cn/plantingApi/"

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // 业务接口统一拦截器，注入 user-enterprise-id
    private val businessInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .header("user-enterprise-id", "1") // 暂时写死为 1 供测试
            .build()
        chain.proceed(newRequest)
    }

    private val weatherOkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val businessOkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(businessInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // 天气 API 实例
    val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .client(weatherOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    // 业务 API Retrofit 实例，供后续创建各个 Service 使用
    val businessRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BUSINESS_BASE_URL)
            .client(businessOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

