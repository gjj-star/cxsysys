package com.example.cxsysys

import androidx.lifecycle.ViewModelStore
import com.example.cxsysys.api.AuthApiService
import com.example.cxsysys.model.BaseResponse
import com.example.cxsysys.model.EnterpriseInfo
import com.example.cxsysys.model.EnterpriseMember
import com.example.cxsysys.viewmodel.EnterpriseUiState
import com.example.cxsysys.viewmodel.EnterpriseViewModel
import java.io.IOException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class EnterpriseViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val stores = mutableListOf<ViewModelStore>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        stores.forEach { it.clear() }
        Dispatchers.resetMain()
    }

    private class TestSession {
        var token: String? = "account-a"
        val versions = MutableStateFlow(1L)
        var clearCount = 0

        fun isCurrent(token: String?, version: Long): Boolean =
            this.token == token && versions.value == version

        fun clearIfCurrent(token: String, version: Long): Boolean {
            if (!isCurrent(token, version)) return false
            clearCount++
            switchTo(null)
            return true
        }

        fun switchTo(token: String?) {
            this.token = token
            versions.value++
        }
    }

    private fun viewModel(api: AuthApiService, session: TestSession = TestSession()): EnterpriseViewModel {
        val model = EnterpriseViewModel(
            api = api,
            tokenProvider = { session.token },
            sessionVersions = session.versions,
            isCurrentSession = session::isCurrent,
            clearIfCurrent = session::clearIfCurrent
        )
        stores += ViewModelStore().also { it.put("enterprise", model) }
        return model
    }

    private fun api(
        info: suspend () -> BaseResponse<EnterpriseInfo> = {
            BaseResponse(200, "success", EnterpriseInfo(enterpriseId = 1, enterpriseName = "企业一"))
        },
        members: suspend (Int?, Int?) -> BaseResponse<List<EnterpriseMember>> = { _, _ ->
            BaseResponse(200, "success", emptyList(), hasMore = false)
        }
    ): AuthApiService = object : StubAuthApiService() {
        override suspend fun getEnterprise(): BaseResponse<EnterpriseInfo> = info()

        override suspend fun getEnterpriseMembers(lastId: Int?, limit: Int?): BaseResponse<List<EnterpriseMember>> =
            members(lastId, limit)
    }

    private fun members(vararg ids: Int): List<EnterpriseMember> =
        ids.map { EnterpriseMember(userId = it, userName = "user-$it") }

    @Test
    fun paginationUsesLastUserIdAndStopsAtTopLevelHasMore() = runTest(dispatcher) {
        val requests = mutableListOf<Pair<Int?, Int?>>()
        val model = viewModel(api(members = { lastId, limit ->
            requests += lastId to limit
            if (lastId == null) {
                BaseResponse(200, "success", members(10, 20), hasMore = true)
            } else {
                BaseResponse(200, "success", members(20, 30), hasMore = false)
            }
        }))
        runCurrent()

        model.loadMoreMembers()
        model.loadMoreMembers()
        runCurrent()
        model.loadMoreMembers()
        runCurrent()

        assertEquals(listOf(null to 10, 20 to 10), requests)
        assertEquals(listOf(10, 20, 30), model.state.value.members.map { it.userId })
        assertFalse(model.state.value.hasMore)
        assertTrue(model.state.value.membersLoaded)
    }

    @Test
    fun infoFailureAndRetryDoNotDiscardLoadedMembers() = runTest(dispatcher) {
        var infoAttempts = 0
        val model = viewModel(api(
            info = {
                infoAttempts++
                if (infoAttempts == 1) throw IOException("企业信息暂不可用")
                BaseResponse(200, "success", EnterpriseInfo(enterpriseId = 8, phoneNumber = null, address = null))
            },
            members = { _, _ -> BaseResponse(200, "success", members(7), hasMore = false) }
        ))
        runCurrent()

        assertEquals("企业信息暂不可用", model.state.value.infoError)
        assertEquals(listOf(7), model.state.value.members.map { it.userId })
        assertNull(model.state.value.membersError)
        model.retryInfo()
        model.retryInfo()
        runCurrent()

        assertEquals(2, infoAttempts)
        assertEquals(8, model.state.value.enterpriseInfo?.enterpriseId)
        assertNull(model.state.value.infoError)
        assertEquals(listOf(7), model.state.value.members.map { it.userId })
    }

    @Test
    fun failedNextPageKeepsDataAndRetriesTheSameCursor() = runTest(dispatcher) {
        val cursors = mutableListOf<Int?>()
        var failNextPage = true
        val model = viewModel(api(members = { lastId, _ ->
            cursors += lastId
            when {
                lastId == null -> BaseResponse(200, "success", members(2, 4), hasMore = true)
                failNextPage -> {
                    failNextPage = false
                    BaseResponse(500, "成员加载暂不可用", null)
                }
                else -> BaseResponse(200, "success", members(7), hasMore = false)
            }
        }))
        runCurrent()
        model.loadMoreMembers()
        runCurrent()

        assertEquals(listOf(2, 4), model.state.value.members.map { it.userId })
        assertEquals("成员加载暂不可用", model.state.value.membersError)
        assertTrue(model.state.value.hasMore)
        assertNotNull(model.state.value.enterpriseInfo)
        model.retryMembers()
        runCurrent()

        assertEquals(listOf(null, 4, 4), cursors)
        assertEquals(listOf(2, 4, 7), model.state.value.members.map { it.userId })
        assertNull(model.state.value.membersError)
    }

    @Test
    fun emptyPageNeverStartsAnotherRequestWithoutAValidCursor() = runTest(dispatcher) {
        var memberRequests = 0
        val model = viewModel(api(members = { _, _ ->
            memberRequests++
            BaseResponse(200, "success", emptyList(), hasMore = true)
        }))
        runCurrent()
        model.loadMoreMembers()
        runCurrent()

        assertTrue(model.state.value.members.isEmpty())
        assertTrue(model.state.value.membersLoaded)
        assertFalse(model.state.value.hasMore)
        assertEquals(1, memberRequests)
    }

    @Test
    fun businessUnauthorizedClearsCurrentSessionAndLoadedData() = runTest(dispatcher) {
        val session = TestSession()
        val model = viewModel(api(
            info = { BaseResponse(401, "token已过期", null) }
        ), session)
        runCurrent()

        assertNull(session.token)
        assertEquals(1, session.clearCount)
        assertEquals(EnterpriseUiState(), model.state.value)
    }

    @Test
    fun httpUnauthorizedClearsCurrentSessionAndLoadedData() = runTest(dispatcher) {
        val session = TestSession()
        val model = viewModel(api(members = { _, _ ->
            throw HttpException(Response.error<Any>(401, """{"message":"token已过期"}""".toResponseBody()))
        }), session)
        runCurrent()

        assertNull(session.token)
        assertEquals(1, session.clearCount)
        assertEquals(EnterpriseUiState(), model.state.value)
    }

    @Test
    fun oldAccountResponsesCannotReplaceDataOrSignOutTheNewAccount() = runTest(dispatcher) {
        val session = TestSession()
        var oldInfo: Continuation<BaseResponse<EnterpriseInfo>>? = null
        var oldMembers: Continuation<BaseResponse<List<EnterpriseMember>>>? = null
        val model = viewModel(api(
            info = {
                if (session.token == "account-a") suspendCoroutine { oldInfo = it }
                else BaseResponse(200, "success", EnterpriseInfo(enterpriseId = 2, enterpriseName = "企业二"))
            },
            members = { _, _ ->
                if (session.token == "account-a") suspendCoroutine { oldMembers = it }
                else BaseResponse(200, "success", members(200), hasMore = false)
            }
        ), session)
        runCurrent()
        session.switchTo("account-b")
        runCurrent()
        assertEquals(2, model.state.value.enterpriseInfo?.enterpriseId)

        // These callbacks deliberately ignore cancellation to model responses arriving after logout.
        requireNotNull(oldInfo).resumeWithException(
            HttpException(Response.error<Any>(401, """{"message":"旧登录已失效"}""".toResponseBody()))
        )
        requireNotNull(oldMembers).resume(BaseResponse(200, "success", members(100), hasMore = false))
        runCurrent()

        assertEquals("account-b", session.token)
        assertEquals(0, session.clearCount)
        assertEquals(2, model.state.value.enterpriseInfo?.enterpriseId)
        assertEquals(listOf(200), model.state.value.members.map { it.userId })
        assertFalse(model.state.value.isLoadingInfo)
        assertFalse(model.state.value.isLoadingMembers)
    }
}
