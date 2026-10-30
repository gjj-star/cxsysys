package com.example.cxsysys.utils

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

class BusinessAuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenProvider()
        val isLoginRequest = request.method == "POST" && request.url.encodedPath in setOf(
            "/plantingApi/auth/smsCode",
            "/plantingApi/auth/login/sms",
            "/plantingApi/auth/login/password"
        )

        if (token.isNullOrBlank() && !isLoginRequest) {
            throw IOException("请先登录")
        }

        val authenticatedRequest = request.newBuilder().apply {
            if (token.isNullOrBlank()) removeHeader("token") else header("token", token)
        }.build()
        return chain.proceed(authenticatedRequest)
    }
}
