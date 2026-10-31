package com.example.cxsysys.utils

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AllowAnonymous

class BusinessAuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenProvider()
        val allowsAnonymous = request.tag(Invocation::class.java)
            ?.method()?.isAnnotationPresent(AllowAnonymous::class.java) == true

        if (token.isNullOrBlank() && !allowsAnonymous) {
            throw IOException("请先登录")
        }

        val authenticatedRequest = request.newBuilder().apply {
            if (token.isNullOrBlank()) removeHeader("token") else header("token", token)
        }.build()
        return chain.proceed(authenticatedRequest)
    }
}
