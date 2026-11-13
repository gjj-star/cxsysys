package com.example.cxsysys.utils

import com.example.cxsysys.BuildConfig
import java.net.URI
import java.util.concurrent.TimeUnit
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

internal object AvatarRequests {
    val client: OkHttpClient by lazy { newClient() }

    fun newClient(): OkHttpClient = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .cache(null)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun trustedUrl(
        avatarUrl: String?,
        baseUrl: String = BuildConfig.BUSINESS_BASE_URL
    ): HttpUrl? {
        if (avatarUrl.isNullOrBlank() || avatarUrl != avatarUrl.trim()) return null
        val uri = runCatching { URI(avatarUrl) }.getOrNull() ?: return null
        if (!uri.isAbsolute || uri.rawUserInfo != null || uri.rawQuery != null || uri.rawFragment != null) return null
        val rawPath = uri.rawPath ?: return null
        val decodedPath = uri.path ?: return null
        // Check the original path before HttpUrl normalizes dot segments and backslashes.
        if (Regex("%2f|%5c", RegexOption.IGNORE_CASE).containsMatchIn(rawPath)
            || decodedPath.any { it <= ' ' || it == '\u007f' || it == '\\' || it == '%' }
            || decodedPath.split('/').drop(1).any { it.isEmpty() || it == "." || it == ".." }
        ) return null

        val base = baseUrl.toHttpUrlOrNull() ?: return null
        val url = avatarUrl.toHttpUrlOrNull() ?: return null
        if (url.scheme != base.scheme || url.host != base.host || url.port != base.port) return null
        val filesPath = base.encodedPath.trimEnd('/') + "/files/"
        if (!url.encodedPath.startsWith(filesPath) || url.encodedPath.length == filesPath.length) return null
        return url
    }

    fun forCurrentUser(
        avatarUrl: String?,
        userId: Int,
        sessionUserId: Int,
        token: String?,
        baseUrl: String = BuildConfig.BUSINESS_BASE_URL
    ): Request? {
        if (userId <= 0 || userId != sessionUserId || token.isNullOrBlank()) return null
        val url = trustedUrl(avatarUrl, baseUrl) ?: return null
        return runCatching {
            Request.Builder()
                .url(url)
                .header("token", token)
                .header("Cache-Control", "no-store")
                .build()
        }.getOrNull()
    }
}
