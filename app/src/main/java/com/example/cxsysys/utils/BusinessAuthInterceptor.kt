package com.example.cxsysys.utils

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AllowAnonymous

class BusinessSession(val token: String, val version: Long)

class BusinessAuthInterceptor(
    private val versionProvider: () -> Long = { TokenManager.sessionVersion.value },
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

        request.tag(BusinessSession::class.java)?.let { session ->
            if (session.token != token || session.version != versionProvider()) {
                throw IOException("登录状态已变更，请重试")
            }
        }

        val authenticatedRequest = request.newBuilder().apply {
            if (token.isNullOrBlank()) removeHeader("token") else header("token", token)
        }.build()
        return chain.proceed(authenticatedRequest)
    }
}
