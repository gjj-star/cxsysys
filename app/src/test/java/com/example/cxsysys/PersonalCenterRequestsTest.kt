package com.example.cxsysys

import com.example.cxsysys.api.AuthApiService
import com.example.cxsysys.api.PlantingApiService
import com.example.cxsysys.model.ChangePasswordRequest
import com.example.cxsysys.utils.BusinessAuthInterceptor
import com.example.cxsysys.utils.BusinessSession
import com.example.cxsysys.utils.avatarUpdateBody
import com.example.cxsysys.utils.passwordChangeError
import com.google.gson.JsonParser
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PersonalCenterRequestsTest {
    private fun RequestBody.text(): String = Buffer().also { writeTo(it) }.readUtf8()

    @Test
    fun clearingAvatarSendsExplicitNullAsTheOnlyJsonField() {
        val request = avatarUpdateBody(null)

        assertEquals("application/json; charset=utf-8", request.contentType().toString())
        assertEquals("""{"avatar_url":null}""", request.text())
        val json = JsonParser.parseString(request.text()).asJsonObject
        assertEquals(setOf("avatar_url"), json.keySet())
        assertTrue(json.get("avatar_url").isJsonNull)
    }

    @Test
    fun avatarBodyPreservesTheOriginalUrlAndEscapesJsonCharacters() {
        val url = "https://example.test/api/files/头像\"图片.png"
        val json = JsonParser.parseString(avatarUpdateBody(url).text()).asJsonObject

        assertEquals(setOf("avatar_url"), json.keySet())
        assertEquals(url, json.get("avatar_url").asString)
    }

    @Test
    fun passwordValidationRequiresOldPasswordMatchingConfirmationAndADifferentNewPassword() {
        assertEquals("请输入旧密码", passwordChangeError("", "new-password", "new-password"))
        assertEquals("新密码至少6位", passwordChangeError("old-password", "12345", "12345"))
        assertEquals("两次新密码不一致", passwordChangeError("old-password", "new-password", "another-password"))
        assertEquals("新密码不能与旧密码相同", passwordChangeError("old-password", "old-password", "old-password"))
        assertNull(passwordChangeError("old-password", "new-password", "new-password"))
    }

    @Test
    fun passwordLimitCountsUtf8BytesInsteadOfCharacters() {
        for (valid in listOf("a".repeat(72), "中".repeat(24), "😀".repeat(18))) {
            assertNull(passwordChangeError("old-password", valid, valid))
        }
        for (invalid in listOf("a".repeat(73), "中".repeat(25), "😀".repeat(19))) {
            assertEquals("新密码不能超过72字节", passwordChangeError("old-password", invalid, invalid))
        }
    }

    private class Fixture {
        val requests = mutableListOf<Request>()
        val session = BusinessSession("test-session", 9L)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://example.test/planting/api/v1/")
            .client(OkHttpClient.Builder()
                .addInterceptor(BusinessAuthInterceptor(versionProvider = { 9L }) { "test-session" })
                .addInterceptor { chain ->
                    val request = chain.request()
                    requests += request
                    val response = when {
                        request.url.encodedPath.endsWith("memberList") -> """{"code":200,"message":"success","data":[],"hasMore":false}"""
                        request.url.encodedPath.endsWith("/upload") -> """{"code":200,"message":"success","data":{"file_id":"1","file_name":"avatar.png","file_url":"https://example.test/planting/api/v1/files/avatar.png","file_size":4}}"""
                        else -> """{"code":200,"message":"success","data":null}"""
                    }
                    Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
                        .code(200).message("OK").body(response.toResponseBody()).build()
                }.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val authApi = retrofit.create(AuthApiService::class.java)
        val plantingApi = retrofit.create(PlantingApiService::class.java)
    }

    @Test
    fun personalCenterUsesExistingBasePathHeaderAndSnakeCasePasswordBody() = runBlocking {
        val fixture = Fixture()
        fixture.authApi.getCurrentUser(fixture.session)
        fixture.authApi.updateAvatar(avatarUpdateBody(null), fixture.session)
        fixture.authApi.changePassword(ChangePasswordRequest("old-password", "new-password", "new-password"), fixture.session)
        fixture.authApi.getEnterprise()
        fixture.authApi.getEnterpriseMembers(lastId = 25, limit = 10)
        fixture.authApi.logout(fixture.session)

        assertEquals(
            listOf("GET /planting/api/v1/user", "PUT /planting/api/v1/user", "PUT /planting/api/v1/user/password",
                "GET /planting/api/v1/user/enterprise", "GET /planting/api/v1/user/enterprise/memberList", "POST /planting/api/v1/user/logout"),
            fixture.requests.map { "${it.method} ${it.url.encodedPath}" }
        )
        assertTrue(fixture.requests.all { it.header("token") == "test-session" && it.url.queryParameter("token") == null })
        assertEquals("25", fixture.requests[4].url.queryParameter("lastid"))
        assertEquals("10", fixture.requests[4].url.queryParameter("limit"))
        assertEquals("""{"avatar_url":null}""", requireNotNull(fixture.requests[1].body).text())
        val passwordJson = JsonParser.parseString(requireNotNull(fixture.requests[2].body).text()).asJsonObject
        assertEquals(setOf("old_password", "new_password", "confirm_password"), passwordJson.keySet())
        assertEquals("old-password", passwordJson.get("old_password").asString)
        assertEquals("new-password", passwordJson.get("new_password").asString)
        assertEquals("new-password", passwordJson.get("confirm_password").asString)
    }

    @Test
    fun avatarUploadAddsTypeWithoutChangingOrdinaryUpload() = runBlocking {
        val fixture = Fixture()
        val file = MultipartBody.Part.createFormData("file", "avatar.png", "png!".toRequestBody("image/png".toMediaType()))
        fixture.plantingApi.uploadFile(file)
        fixture.plantingApi.uploadFile(file, "avatar".toRequestBody("text/plain".toMediaType()), fixture.session)

        val ordinary = requireNotNull(fixture.requests[0].body).text()
        val avatar = requireNotNull(fixture.requests[1].body).text()
        assertTrue(ordinary.contains("name=\"file\"; filename=\"avatar.png\""))
        assertFalse(ordinary.contains("name=\"type\""))
        assertTrue(avatar.contains("name=\"type\""))
        assertTrue(avatar.contains("\r\n\r\navatar\r\n"))
        assertTrue(fixture.requests.all { it.method == "POST" && it.url.encodedPath == "/planting/api/v1/upload" })
    }

    @Test
    fun staleSessionTagBlocksMutationBeforeAnyNetworkRequest() = runBlocking {
        val fixture = Fixture()
        for (staleSession in listOf(BusinessSession("another-session", 9L), BusinessSession("test-session", 8L))) {
            try {
                fixture.authApi.updateAvatar(avatarUpdateBody(null), staleSession)
                fail("A stale session must not reach the transport")
            } catch (expected: IOException) {
                assertEquals("登录状态已变更，请重试", expected.message)
            }
        }
        assertTrue(fixture.requests.isEmpty())
    }
}
