package com.example.printerfeature

import com.example.printerfeature.data.ApiErrorMessages
import java.io.IOException
import java.net.SocketTimeoutException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ApiErrorMessagesTest {
    @Test
    fun httpErrorsUseResponseBodyMessage() {
        for (status in listOf(400, 401, 403, 422, 500)) {
            val response = Response.error<Any>(status, """{"message":" 无权获取打印数据 "}""".toResponseBody())
            assertEquals("无权获取打印数据", ApiErrorMessages.fromResponse(response, null, "获取失败"))
        }
    }

    @Test
    fun invalidMessagesDoNotExposeHttpReasonPhrase() {
        for (body in listOf("", "<html>Bad Gateway</html>", "{", "null", "[]", "{}",
            """{"message":null}""", """{"message":" "}""", """{"message":403}""",
            """{"message":true}""", """{"message":{}}""", """{"message":[]}""")) {
            val response = Response.error<Any>(403, body.toResponseBody())
            assertEquals("获取失败", ApiErrorMessages.fromResponse(response, null, "获取失败"))
        }
    }

    @Test
    fun businessErrorsUseMessageOrFallback() {
        assertEquals("数据不可用", ApiErrorMessages.fromResponse(Response.success<Any>(null), " 数据不可用 ", "获取失败"))
        for (message in listOf(null, "", " ")) {
            assertEquals("获取失败", ApiErrorMessages.fromResponse(Response.success<Any>(null), message, "获取失败"))
        }
    }

    @Test
    fun callbackFailuresHaveUsefulMessages() {
        assertEquals("请先登录", ApiErrorMessages.fromThrowable(IOException("请先登录"), "获取失败"))
        assertEquals("获取失败", ApiErrorMessages.fromThrowable(IOException(), "获取失败"))
        assertEquals("请求超时，请稍后重试", ApiErrorMessages.fromThrowable(SocketTimeoutException(), "获取失败"))
        val error = HttpException(Response.error<Any>(401, """{"message":"登录已过期"}""".toResponseBody()))
        assertEquals("登录已过期", ApiErrorMessages.fromThrowable(error, "获取失败"))
    }
}
