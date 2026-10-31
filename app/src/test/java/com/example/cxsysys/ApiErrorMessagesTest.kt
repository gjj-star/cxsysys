package com.example.cxsysys

import com.example.cxsysys.api.AuthApiService
import com.example.cxsysys.model.PasswordLoginRequest
import com.example.cxsysys.model.SendSmsCodeRequest
import com.example.cxsysys.model.SmsLoginRequest
import com.example.cxsysys.utils.orErrorMessage
import com.example.cxsysys.utils.toUserMessage
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiErrorMessagesTest {
    private fun httpError(body: String, status: Int = 401) =
        HttpException(Response.error<Any>(status, body.toResponseBody()))

    @Test
    fun extractsMessageForAllHttpFailures() {
        for (status in listOf(400, 401, 403, 422, 500)) {
            assertEquals("账号或密码错误", httpError("""{"message":" 账号或密码错误 "}""", status).toUserMessage("登录失败"))
        }
    }

    @Test
    fun malformedOrMissingMessagesUseFallback() {
        for (body in listOf("", "<html>Bad Gateway</html>", "{", "[]", "null", "{}",
            """{"message":null}""", """{"message":" "}""", """{"message":401}""",
            """{"message":true}""", """{"message":{}}""", """{"message":[]}""")) {
            assertEquals("登录失败", httpError(body).toUserMessage("登录失败"))
        }
    }

    @Test
    fun businessMessagesHandleNullAndBlankValues() {
        assertEquals("验证码错误", " 验证码错误 ".orErrorMessage("登录失败"))
        for (message in listOf(null, "", " ")) {
            assertEquals("登录失败", message.orErrorMessage("登录失败"))
        }
    }

    @Test
    fun networkFailuresAndLocalAuthHaveUsefulMessages() {
        assertEquals("请先登录", IOException("请先登录").toUserMessage("请求失败"))
        assertEquals("请求失败", IOException().toUserMessage("请求失败"))
        assertEquals("请求超时，请稍后重试", SocketTimeoutException("timeout").toUserMessage("请求失败"))
        assertEquals("网络连接失败，请检查网络后重试", UnknownHostException("example.test").toUserMessage("请求失败"))
    }

    @Test
    fun cancellationIsNotShownAsAnError() {
        val cancellation = CancellationException("cancelled")
        assertSame(cancellation, assertThrows(CancellationException::class.java) {
            cancellation.toUserMessage("请求失败")
        })
    }

    @Test
    fun retrofitLoginAndBusinessHttpErrorsKeepServerMessage() = runBlocking {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            okhttp3.Response.Builder().request(chain.request())
                .protocol(Protocol.HTTP_1_1).code(401).message("Unauthorized")
                .body("""{"code":401,"message":"服务器返回的具体原因","data":null}""".toResponseBody())
                .build()
        }.build()
        val api = Retrofit.Builder().baseUrl("https://example.test/api/").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(AuthApiService::class.java)
        val requests: List<suspend () -> Any> = listOf(
            { api.sendSmsCode(SendSmsCodeRequest("13800000000")) },
            { api.loginBySms(SmsLoginRequest("13800000000", "123456")) },
            { api.loginByPassword(PasswordLoginRequest("test", "test")) },
            { api.getCurrentUser() }
        )
        for (request in requests) {
            try {
                request()
                fail("Expected HTTP error")
            } catch (error: HttpException) {
                assertEquals("服务器返回的具体原因", error.toUserMessage("请求失败"))
            }
        }
    }
}
