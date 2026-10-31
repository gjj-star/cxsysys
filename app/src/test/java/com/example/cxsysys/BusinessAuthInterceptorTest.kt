package com.example.cxsysys

import com.example.cxsysys.api.AuthApiService
import com.example.cxsysys.model.PasswordLoginRequest
import com.example.cxsysys.model.SendSmsCodeRequest
import com.example.cxsysys.model.SmsLoginRequest
import com.example.cxsysys.utils.BusinessAuthInterceptor
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BusinessAuthInterceptorTest {
    private class Fixture(
        val token: String?,
        basePath: String = "planting/api/v1/",
        baseUrl: String = "https://example.test/$basePath"
    ) {
        val requests = mutableListOf<Request>()
        val client = OkHttpClient.Builder()
            .addInterceptor(BusinessAuthInterceptor { token })
            .addInterceptor { chain ->
                requests.add(chain.request())
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("""{"code":200,"message":"ok","data":null}""".toResponseBody())
                    .build()
            }
            .build()
        val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    @Test
    fun loginEndpointsUseConfiguredBusinessBaseUrl() = runBlocking {
        val fixture = Fixture(null, baseUrl = BuildConfig.BUSINESS_BASE_URL)

        fixture.api.sendSmsCode(SendSmsCodeRequest("13800000000"))
        fixture.api.loginBySms(SmsLoginRequest("13800000000", "123456"))
        fixture.api.loginByPassword(PasswordLoginRequest("test", "test"))

        assertEquals(
            listOf("auth/smsCode", "auth/login/sms", "auth/login/password")
                .map { BuildConfig.BUSINESS_BASE_URL + it },
            fixture.requests.map { it.url.toString() }
        )
    }

    @Test
    fun anonymousLoginEndpointsWorkAcrossBasePaths() = runBlocking {
        for (basePath in listOf("plantingApi/", "planting/api/v1/")) {
            for (token in listOf(null, "", " ")) {
                val fixture = Fixture(token, basePath)

                assertEquals(200, fixture.api.sendSmsCode(SendSmsCodeRequest("13800000000")).code)
                assertEquals(200, fixture.api.loginBySms(SmsLoginRequest("13800000000", "123456")).code)
                assertEquals(200, fixture.api.loginByPassword(PasswordLoginRequest("test", "test")).code)

                assertEquals(
                    listOf("/${basePath}auth/smsCode", "/${basePath}auth/login/sms", "/${basePath}auth/login/password"),
                    fixture.requests.map { it.url.encodedPath }
                )
                assertTrue(fixture.requests.all { it.method == "POST" && it.header("token") == null })
            }
        }
    }

    @Test
    fun currentUserStillRequiresLogin() = runBlocking {
        for (token in listOf(null, "", " ")) {
            val fixture = Fixture(token)

            try {
                fixture.api.getCurrentUser()
                fail("Expected anonymous business request to be blocked")
            } catch (error: IOException) {
                assertEquals("请先登录", error.message)
            }

            assertTrue(fixture.requests.isEmpty())
        }
    }

    @Test
    fun unmarkedRequestsAreBlockedEvenAtOldLoginPath() {
        val fixture = Fixture(null)
        val request = Request.Builder()
            .url("https://example.test/plantingApi/auth/smsCode")
            .post("{}".toRequestBody())
            .header("token", "stale-token")
            .build()

        val error = assertThrows(IOException::class.java) {
            fixture.client.newCall(request).execute().close()
        }

        assertEquals("请先登录", error.message)
        assertTrue(fixture.requests.isEmpty())
    }

    @Test
    fun authenticatedRequestsReceiveToken() = runBlocking {
        val fixture = Fixture("session-token")

        fixture.api.getCurrentUser()
        fixture.api.loginByPassword(PasswordLoginRequest("test", "test"))

        assertEquals(2, fixture.requests.size)
        assertTrue(fixture.requests.all { it.header("token") == "session-token" })
    }
}
