package com.example.cxsysys.utils

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

fun String?.orErrorMessage(fallback: String): String =
    this?.trim()?.takeIf { it.isNotEmpty() } ?: fallback

fun Throwable.toUserMessage(fallback: String): String {
    if (this is CancellationException) throw this
    return when (this) {
        is HttpException -> {
            val serverMessage = try {
                response()?.errorBody()?.use { body ->
                    val json = JsonParser.parseString(body.string())
                    val value = if (json.isJsonObject) json.asJsonObject.get("message") else null
                    if (value?.isJsonPrimitive == true && value.asJsonPrimitive.isString) value.asString else null
                }
            } catch (_: Exception) {
                null
            }
            serverMessage.orErrorMessage(fallback)
        }
        is SocketTimeoutException -> "请求超时，请稍后重试"
        is UnknownHostException, is ConnectException -> "网络连接失败，请检查网络后重试"
        is JsonParseException -> fallback
        else -> message.orErrorMessage(fallback)
    }
}
