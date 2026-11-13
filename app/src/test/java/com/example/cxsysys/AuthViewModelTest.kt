package com.example.cxsysys

import androidx.lifecycle.ViewModelStore
import com.example.cxsysys.model.*
import com.example.cxsysys.utils.*
import com.example.cxsysys.viewmodel.AuthState
import com.example.cxsysys.viewmodel.AuthViewModel
import java.io.IOException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import okhttp3.RequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val stores = mutableListOf<ViewModelStore>()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        TokenManager.init(InMemoryPreferences())
    }

    @After fun tearDown() {
        stores.forEach { it.clear() }
        Dispatchers.resetMain()
    }

    private fun user(id: Int = 1, avatar: String? = "https://example.test/files/avatar.png", superAdmin: Boolean = false) =
        UserInfo(userId = id, userName = "user-$id", realName = "Name $id", enterpriseId = 4,
            deptId = null, avatarUrl = avatar, enterpriseName = "Enterprise", roles = listOf(UserRole(2, "Member")),
            status = 1, superAdmin = superAdmin)

    private fun signIn(token: String = "account-a", user: UserInfo = user()) {
        TokenManager.saveToken(token)
        TokenManager.saveUserInfo(user)
    }

    private fun model(api: StubAuthApiService): AuthViewModel =
        AuthViewModel(api).also { vm -> stores += ViewModelStore().also { it.put("auth", vm) } }

    @Test fun refreshUpdatesSharedStateAndCacheAndClearsNullableOldFields() = runTest {
        signIn(user = user().copy(deptId = 99))
        val next = user(avatar = null).copy(realName = null, phoneNumber = null)
        val vm = model(object : StubAuthApiService() {
            override suspend fun getCurrentUser(session: BusinessSession?) = BaseResponse(200, "success", next)
        })
        runCurrent()
        vm.refreshProfile()
        runCurrent()

        val state = vm.authState.value as AuthState.LoggedIn
        assertNull(state.avatarUrl)
        assertNull(state.realName)
        assertEquals("Enterprise", state.enterpriseName)
        assertEquals("Member", state.roles.single().roleName)
        assertEquals(1, state.status)
        assertNull(TokenManager.getDeptId())
        assertEquals(next, TokenManager.getUserInfo())
        assertFalse(vm.profileState.value.isLoading)
    }

    @Test fun oldProfileResponseCannotOverwriteAnotherAccount() = runTest {
        signIn()
        lateinit var reply: Continuation<BaseResponse<UserInfo>>
        val vm = model(object : StubAuthApiService() {
            override suspend fun getCurrentUser(session: BusinessSession?): BaseResponse<UserInfo> =
                suspendCoroutine { reply = it }
        })
        runCurrent()
        vm.refreshProfile()
        runCurrent()
        signIn("account-b", user(2))
        runCurrent()
        reply.resume(BaseResponse(200, "success", user(1).copy(realName = "Late A")))
        runCurrent()

        assertEquals("account-b", TokenManager.getToken())
        assertEquals(2, (vm.authState.value as AuthState.LoggedIn).userId)
        assertEquals("Name 2", TokenManager.getRealName())
    }

    @Test fun protectedHttpAndBusinessUnauthorizedClearCurrentSession() = runTest {
        for (http in listOf(false, true)) {
            signIn()
            val vm = model(object : StubAuthApiService() {
                override suspend fun getCurrentUser(session: BusinessSession?): BaseResponse<UserInfo> {
                    if (http) throw HttpException(Response.error<Any>(401, """{"message":"expired"}""".toResponseBody()))
                    return BaseResponse(401, "expired")
                }
            })
            runCurrent()
            vm.refreshProfile()
            runCurrent()
            assertNull(TokenManager.getToken())
            assertEquals(AuthState.NotLoggedIn, vm.authState.value)
            assertTrue(vm.notice.value.orEmpty().contains("重新登录"))
        }
    }

    @Test fun avatarClearIsSingleSubmissionSendsNullAndRefreshesBothViews() = runTest {
        signIn()
        var calls = 0
        var wireBody = ""
        lateinit var reply: Continuation<BaseResponse<UserInfo>>
        val vm = model(object : StubAuthApiService() {
            override suspend fun updateAvatar(request: RequestBody, session: BusinessSession?): BaseResponse<UserInfo> {
                calls++
                wireBody = Buffer().also { request.writeTo(it) }.readUtf8()
                return suspendCoroutine { reply = it }
            }
        })
        runCurrent()
        vm.clearAvatar()
        vm.clearAvatar()
        runCurrent()
        assertEquals(1, calls)
        assertEquals("""{"avatar_url":null}""", wireBody)
        assertTrue(vm.profileState.value.isSavingAvatar)
        reply.resume(BaseResponse(200, "success", user(avatar = null)))
        runCurrent()
        assertNull((vm.authState.value as AuthState.LoggedIn).avatarUrl)
        assertNull(TokenManager.getAvatarUrl())
        assertFalse(vm.profileState.value.isSavingAvatar)
    }

    @Test fun normalPasswordChangeClearsSessionButSuperAdminKeepsIt() = runTest {
        for (superAdmin in listOf(false, true)) {
            signIn(user = user(superAdmin = superAdmin))
            var calls = 0
            val vm = model(object : StubAuthApiService() {
                override suspend fun changePassword(request: ChangePasswordRequest, session: BusinessSession?): BaseResponse<Any> {
                    calls++
                    assertEquals("old-pass", request.oldPassword)
                    assertEquals("new-pass", request.newPassword)
                    return BaseResponse(200, "success")
                }
            })
            runCurrent()
            vm.changePassword("old-pass", "new-pass", "new-pass")
            vm.changePassword("old-pass", "new-pass", "new-pass")
            runCurrent()
            assertEquals(1, calls)
            assertEquals(superAdmin, TokenManager.isLoggedIn())
            if (superAdmin) assertTrue(vm.passwordState.value.success)
            else assertEquals(AuthState.NotLoggedIn, vm.authState.value)
            assertFalse(vm.passwordState.value.isSubmitting)
        }
    }

    @Test fun passwordFailureKeepsSessionAndCanBeRetried() = runTest {
        signIn()
        var calls = 0
        val vm = model(object : StubAuthApiService() {
            override suspend fun changePassword(request: ChangePasswordRequest, session: BusinessSession?): BaseResponse<Any> {
                calls++
                return BaseResponse(400, "旧密码不正确")
            }
        })
        runCurrent()
        vm.changePassword("old-pass", "12345", "12345")
        runCurrent()
        assertEquals(0, calls)
        vm.changePassword("old-pass", "new-pass", "new-pass")
        runCurrent()
        assertEquals("旧密码不正确", vm.passwordState.value.errorMessage)
        assertEquals("account-a", TokenManager.getToken())
        vm.changePassword("old-pass", "new-pass", "new-pass")
        runCurrent()
        assertEquals(2, calls)
    }

    @Test fun offlineLogoutClearsLocallyAfterAttemptAndReportsUnconfirmedRevocation() = runTest {
        signIn()
        val vm = model(object : StubAuthApiService() {
            override suspend fun logout(session: BusinessSession?): BaseResponse<Any> {
                assertEquals("account-a", TokenManager.getToken())
                assertEquals("account-a", session?.token)
                throw IOException("offline")
            }
        })
        runCurrent()
        vm.logout()
        runCurrent()
        assertNull(TokenManager.getToken())
        assertEquals(AuthState.NotLoggedIn, vm.authState.value)
        assertFalse(vm.isLoggingOut.value)
        assertTrue(vm.notice.value.orEmpty().contains("本地"))
        assertTrue(vm.notice.value.orEmpty().contains("未确认"))
    }

    @Test fun logoutUnauthorizedStillClearsLocally() = runTest {
        signIn()
        val vm = model(object : StubAuthApiService() {
            override suspend fun logout(session: BusinessSession?): BaseResponse<Any> =
                throw HttpException(Response.error<Any>(401, """{"message":"expired"}""".toResponseBody()))
        })
        runCurrent()
        vm.logout()
        runCurrent()
        assertNull(TokenManager.getToken())
        assertEquals("已退出登录", vm.notice.value)
    }

    @Test fun oldLogoutCannotClearANewerLoginOrItsWaitingState() = runTest {
        signIn()
        lateinit var reply: Continuation<BaseResponse<Any>>
        val vm = model(object : StubAuthApiService() {
            override suspend fun logout(session: BusinessSession?): BaseResponse<Any> =
                suspendCoroutine { reply = it }
        })
        runCurrent()
        vm.logout()
        runCurrent()
        assertTrue(vm.isLoggingOut.value)
        signIn("account-b", user(2))
        runCurrent()
        reply.resume(BaseResponse(200, "success"))
        runCurrent()
        assertEquals("account-b", TokenManager.getToken())
        assertEquals(2, (vm.authState.value as AuthState.LoggedIn).userId)
        assertFalse(vm.isLoggingOut.value)
    }

    @Test fun malformedLoginDoesNotRemainBusyAndPreservesForm() = runTest {
        val vm = model(object : StubAuthApiService() {
            override suspend fun loginByPassword(request: PasswordLoginRequest): BaseResponse<LoginResponse> =
                BaseResponse(200, "success")
        })
        runCurrent()
        vm.updateAccount("tester")
        vm.updatePassword("test-password")
        vm.loginByPassword()
        runCurrent()
        assertFalse(vm.formState.value.isLoggingIn)
        assertEquals("tester", vm.formState.value.account)
        assertEquals("test-password", vm.formState.value.password)
        assertNotNull(vm.formState.value.errorMessage)
        assertNull(TokenManager.getToken())
    }

    @Test fun successfulLoginStartsProfileRefreshWithoutLeavingBusyState() = runTest {
        val vm = model(object : StubAuthApiService() {
            override suspend fun loginByPassword(request: PasswordLoginRequest) =
                BaseResponse(200, "success", LoginResponse("new-session", 3600, user()))
            override suspend fun getCurrentUser(session: BusinessSession?) =
                BaseResponse(200, "success", user().copy(realName = "Fresh profile"))
        })
        runCurrent()
        vm.updateAccount("tester")
        vm.updatePassword("test-password")
        vm.loginByPassword()
        runCurrent()
        assertEquals("new-session", TokenManager.getToken())
        assertFalse(vm.formState.value.isLoggingIn)
        assertFalse(vm.profileState.value.isLoading)
        assertEquals("Fresh profile", (vm.authState.value as AuthState.LoggedIn).realName)
    }
}
