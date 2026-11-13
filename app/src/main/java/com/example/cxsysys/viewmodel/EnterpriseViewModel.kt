package com.example.cxsysys.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.api.AuthApiService
import com.example.cxsysys.model.EnterpriseInfo
import com.example.cxsysys.model.EnterpriseMember
import com.example.cxsysys.utils.RetrofitClient
import com.example.cxsysys.utils.TokenManager
import com.example.cxsysys.utils.orErrorMessage
import com.example.cxsysys.utils.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class EnterpriseUiState(
    val enterpriseInfo: EnterpriseInfo? = null,
    val members: List<EnterpriseMember> = emptyList(),
    val isLoadingInfo: Boolean = false,
    val isLoadingMembers: Boolean = false,
    val infoError: String? = null,
    val membersError: String? = null,
    val membersLoaded: Boolean = false,
    val hasMore: Boolean = false
)

class EnterpriseViewModel(
    private val api: AuthApiService = RetrofitClient.authApi,
    private val tokenProvider: () -> String? = TokenManager::getToken,
    private val sessionVersions: StateFlow<Long> = TokenManager.sessionVersion,
    private val isCurrentSession: (String?, Long) -> Boolean = TokenManager::isCurrentSession,
    private val clearIfCurrent: (String, Long) -> Boolean = TokenManager::clearIfCurrent
) : ViewModel() {
    private data class Session(val token: String, val version: Long)

    private val _state = MutableStateFlow(EnterpriseUiState())
    val state: StateFlow<EnterpriseUiState> = _state.asStateFlow()

    private var session: Session? = null
    private var infoJob: Job? = null
    private var membersJob: Job? = null

    init {
        viewModelScope.launch {
            sessionVersions.collect { version ->
                val nextSession = tokenProvider()?.takeIf { it.isNotBlank() }
                    ?.let { Session(it, version) }
                if (session != nextSession) {
                    infoJob?.cancel()
                    membersJob?.cancel()
                    session = nextSession
                    _state.value = EnterpriseUiState()
                    if (nextSession != null) {
                        retryInfo()
                        retryMembers()
                    }
                }
            }
        }
    }

    fun retryInfo() {
        val requestSession = session ?: return
        if (_state.value.isLoadingInfo || !isCurrent(requestSession)) return
        _state.update { it.copy(isLoadingInfo = true, infoError = null) }
        infoJob = viewModelScope.launch {
            try {
                val response = api.getEnterprise()
                if (!isCurrent(requestSession)) return@launch
                when (response.code) {
                    0, 200 -> {
                        val info = requireNotNull(response.data) { "未获取到企业信息" }
                        _state.update { it.copy(enterpriseInfo = info) }
                    }
                    401 -> expireSession(requestSession)
                    else -> _state.update {
                        it.copy(infoError = response.message.orErrorMessage("获取企业信息失败"))
                    }
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (isCurrent(requestSession)) {
                    if (error is HttpException && error.code() == 401) {
                        expireSession(requestSession)
                    } else {
                        _state.update { it.copy(infoError = error.toUserMessage("获取企业信息失败")) }
                    }
                }
            } finally {
                if (isCurrent(requestSession)) {
                    _state.update { it.copy(isLoadingInfo = false) }
                }
            }
        }
    }

    fun retryMembers() {
        fetchMembers(append = _state.value.membersLoaded)
    }

    fun loadMoreMembers() {
        if (!_state.value.membersLoaded || !_state.value.hasMore) return
        fetchMembers(append = true)
    }

    private fun fetchMembers(append: Boolean) {
        val requestSession = session ?: return
        val currentState = _state.value
        if (currentState.isLoadingMembers || !isCurrent(requestSession)) return
        val lastId = if (append) currentState.members.lastOrNull()?.userId else null
        if (append && (lastId == null || lastId <= 0 || !currentState.hasMore)) return
        _state.update { it.copy(isLoadingMembers = true, membersError = null) }
        membersJob = viewModelScope.launch {
            try {
                val response = api.getEnterpriseMembers(lastId = lastId, limit = 10)
                if (!isCurrent(requestSession)) return@launch
                when (response.code) {
                    0, 200 -> {
                        val page = requireNotNull(response.data) { "未获取到企业成员" }
                        val nextId = page.lastOrNull()?.userId
                        _state.update {
                            it.copy(
                                members = (if (append) it.members + page else page).distinctBy { member -> member.userId },
                                membersLoaded = true,
                                hasMore = response.hasMore == true && nextId != null && nextId > (lastId ?: 0)
                            )
                        }
                    }
                    401 -> expireSession(requestSession)
                    else -> _state.update {
                        it.copy(membersError = response.message.orErrorMessage("获取企业成员失败"))
                    }
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (isCurrent(requestSession)) {
                    if (error is HttpException && error.code() == 401) {
                        expireSession(requestSession)
                    } else {
                        _state.update { it.copy(membersError = error.toUserMessage("获取企业成员失败")) }
                    }
                }
            } finally {
                if (isCurrent(requestSession)) {
                    _state.update { it.copy(isLoadingMembers = false) }
                }
            }
        }
    }

    private fun isCurrent(requestSession: Session): Boolean =
        session == requestSession && isCurrentSession(requestSession.token, requestSession.version)

    private fun expireSession(requestSession: Session) {
        if (clearIfCurrent(requestSession.token, requestSession.version)) {
            session = null
            infoJob?.cancel()
            membersJob?.cancel()
            _state.value = EnterpriseUiState()
        }
    }
}
