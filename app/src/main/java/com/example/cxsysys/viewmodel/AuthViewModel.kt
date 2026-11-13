package com.example.cxsysys.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.api.AuthApiService
import com.example.cxsysys.api.PlantingApiService
import com.example.cxsysys.model.*
import com.example.cxsysys.utils.*
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

enum class LoginMethod { SMS_CODE, PASSWORD }

sealed class AuthState {
    object NotLoggedIn : AuthState()
    object Loading : AuthState()
    data class LoggedIn(
        val userId: Int,
        val userName: String?,
        val realName: String?,
        val phoneNumber: String?,
        val avatarUrl: String?,
        val enterpriseId: Int,
        val deptId: Int?,
        val superAdmin: Boolean,
        val enterpriseName: String? = null,
        val roles: List<UserRole> = emptyList(),
        val status: Int? = null
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}

data class LoginFormState(
    val loginMethod: LoginMethod = LoginMethod.SMS_CODE,
    val phone: String = "",
    val smsCode: String = "",
    val account: String = "",
    val password: String = "",
    val smsCodeSent: Boolean = false,
    val smsCountdown: Int = 0,
    val isSendingSms: Boolean = false,
    val isLoggingIn: Boolean = false,
    val errorMessage: String? = null
)

data class ProfileState(
    val isLoading: Boolean = false,
    val isSavingAvatar: Boolean = false,
    val errorMessage: String? = null
)

data class PasswordChangeState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)

class AuthViewModel(
    private val authApi: AuthApiService = RetrofitClient.authApi
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotLoggedIn)
    val authState = _authState.asStateFlow()
    private val _formState = MutableStateFlow(LoginFormState())
    val formState = _formState.asStateFlow()
    private val _profileState = MutableStateFlow(ProfileState())
    val profileState = _profileState.asStateFlow()
    private val _passwordState = MutableStateFlow(PasswordChangeState())
    val passwordState = _passwordState.asStateFlow()
    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut = _isLoggingOut.asStateFlow()
    private val _notice = MutableStateFlow<String?>(null)
    val notice = _notice.asStateFlow()

    private var profileJob: Job? = null
    private var avatarJob: Job? = null
    private var passwordJob: Job? = null
    private var loginJob: Job? = null
    private var smsJob: Job? = null
    private var countdownJob: Job? = null
    private var observedSessionVersion = TokenManager.sessionVersion.value
    private val plantingApi by lazy { RetrofitClient.businessRetrofit.create(PlantingApiService::class.java) }

    init {
        restoreLoginState()
        viewModelScope.launch {
            TokenManager.sessionVersion.collect { synchronizeSession() }
        }
    }

    private fun UserInfo.toAuthState() = AuthState.LoggedIn(
        userId = userId ?: 0, userName = userName, realName = realName,
        phoneNumber = phoneNumber, avatarUrl = avatarUrl, enterpriseId = enterpriseId ?: 0,
        deptId = deptId, superAdmin = superAdmin, enterpriseName = enterpriseName,
        roles = roles.orEmpty(), status = status
    )

    private fun restoreLoginState() {
        _authState.value = if (TokenManager.isLoggedIn()) TokenManager.getUserInfo().toAuthState()
            else AuthState.NotLoggedIn
    }

    private fun synchronizeSession() {
        val version = TokenManager.sessionVersion.value
        if (version == observedSessionVersion) return
        observedSessionVersion = version
        profileJob?.cancel()
        avatarJob?.cancel()
        passwordJob?.cancel()
        loginJob?.cancel()
        smsJob?.cancel()
        countdownJob?.cancel()
        _profileState.value = ProfileState()
        _passwordState.value = PasswordChangeState()
        _formState.value = LoginFormState()
        _isLoggingOut.value = false
        restoreLoginState()
    }

    private fun currentSession(): BusinessSession? = TokenManager.getToken()
        ?.takeIf { it.isNotBlank() }
        ?.let { BusinessSession(it, TokenManager.sessionVersion.value) }

    private fun isCurrent(session: BusinessSession) =
        TokenManager.isCurrentSession(session.token, session.version)

    private fun expireSession(session: BusinessSession) {
        if (TokenManager.clearIfCurrent(session.token, session.version)) {
            synchronizeSession()
            _notice.value = "登录状态已失效，请重新登录"
        }
    }

    private fun successful(code: Int, message: String?, session: BusinessSession, fallback: String): Boolean {
        if (code == 401) {
            expireSession(session)
            return false
        }
        if (code != 0 && code != 200) throw IOException(message.orErrorMessage(fallback))
        return true
    }

    private fun applyUser(user: UserInfo, session: BusinessSession) {
        if (!isCurrent(session) || _isLoggingOut.value) return
        require((user.userId ?: 0) > 0) { "获取用户信息失败，缺少用户标识" }
        TokenManager.saveUserInfo(user)
        _authState.value = user.toAuthState()
    }

    private suspend fun fetchProfile(session: BusinessSession) {
        val response = authApi.getCurrentUser(session)
        if (!isCurrent(session) || _isLoggingOut.value) return
        if (successful(response.code, response.message, session, "获取个人信息失败")) {
            applyUser(requireNotNull(response.data) { "获取个人信息失败，未返回用户资料" }, session)
        }
    }

    fun refreshProfile() {
        val session = currentSession() ?: return
        if (_isLoggingOut.value || _profileState.value.isLoading || _profileState.value.isSavingAvatar) return
        profileJob?.cancel()
        _profileState.value = _profileState.value.copy(isLoading = true, errorMessage = null)
        profileJob = viewModelScope.launch {
            try {
                fetchProfile(session)
            } catch (error: Exception) {
                profileFailure(error, session, "获取个人信息失败")
            } finally {
                if (isCurrent(session)) _profileState.value = _profileState.value.copy(isLoading = false)
            }
        }
    }

    private fun profileFailure(error: Exception, session: BusinessSession, fallback: String) {
        if (error is CancellationException) throw error
        if (!isCurrent(session) || _isLoggingOut.value) return
        if (error is HttpException && error.code() == 401) expireSession(session)
        else _profileState.value = _profileState.value.copy(errorMessage = error.toUserMessage(fallback))
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        val session = currentSession() ?: return
        if (_isLoggingOut.value || _profileState.value.isSavingAvatar || _passwordState.value.isSubmitting) return
        profileJob?.cancel()
        _profileState.value = ProfileState(isSavingAvatar = true)
        val appContext = context.applicationContext
        avatarJob = viewModelScope.launch {
            var image: File? = null
            try {
                val mediaType = appContext.contentResolver.getType(uri)?.toMediaTypeOrNull()
                    ?: "application/octet-stream".toMediaType()
                image = withContext(Dispatchers.IO) {
                    val suffix = when (mediaType.subtype) {
                        "png" -> ".png"
                        "webp" -> ".webp"
                        "gif" -> ".gif"
                        else -> ".jpg"
                    }
                    val file = File.createTempFile("avatar_", suffix, appContext.cacheDir)
                    image = file
                    try {
                        val input = appContext.contentResolver.openInputStream(uri)
                            ?: throw IOException("无法读取所选图片")
                        input.use { source -> file.outputStream().use { source.copyTo(it) } }
                        file
                    } catch (error: Exception) {
                        file.delete()
                        throw error
                    }
                }
                if (!isCurrent(session) || _isLoggingOut.value) return@launch
                val response = plantingApi.uploadFile(
                    MultipartBody.Part.createFormData("file", image.name, image.asRequestBody(mediaType)),
                    "avatar".toRequestBody("text/plain".toMediaType()), session
                )
                if (!isCurrent(session) || _isLoggingOut.value) return@launch
                if (successful(response.code, response.message, session, "头像上传失败")) {
                    val url = requireNotNull(response.data?.fileUrl?.takeIf { it.isNotBlank() }) { "上传未返回头像地址" }
                    // 上传即已保存；刷新失败也不回退已确认的新头像。
                    applyUser(TokenManager.getUserInfo().copy(avatarUrl = url), session)
                    _notice.value = "头像已更新"
                    fetchProfile(session)
                }
            } catch (error: Exception) {
                profileFailure(error, session, "头像上传失败，请重试")
            } finally {
                image?.delete()
                if (isCurrent(session)) _profileState.value = _profileState.value.copy(isSavingAvatar = false)
            }
        }
    }

    fun clearAvatar() {
        val session = currentSession() ?: return
        if (_isLoggingOut.value || _profileState.value.isSavingAvatar || _passwordState.value.isSubmitting) return
        profileJob?.cancel()
        _profileState.value = ProfileState(isSavingAvatar = true)
        avatarJob = viewModelScope.launch {
            try {
                val response = authApi.updateAvatar(avatarUpdateBody(null), session)
                if (!isCurrent(session) || _isLoggingOut.value) return@launch
                if (successful(response.code, response.message, session, "清除头像失败")) {
                    applyUser(requireNotNull(response.data) { "清除头像后未返回用户资料，请刷新" }, session)
                    _notice.value = "头像已清除"
                }
            } catch (error: Exception) {
                profileFailure(error, session, "清除头像失败")
            } finally {
                if (isCurrent(session)) _profileState.value = _profileState.value.copy(isSavingAvatar = false)
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        val session = currentSession() ?: return
        if (_passwordState.value.isSubmitting || _isLoggingOut.value || _profileState.value.isSavingAvatar) return
        passwordChangeError(oldPassword, newPassword, confirmPassword)?.let {
            _passwordState.value = PasswordChangeState(errorMessage = it)
            return
        }
        _passwordState.value = PasswordChangeState(isSubmitting = true)
        passwordJob = viewModelScope.launch {
            try {
                val response = authApi.changePassword(ChangePasswordRequest(oldPassword, newPassword, confirmPassword), session)
                if (!isCurrent(session) || _isLoggingOut.value) return@launch
                if (successful(response.code, response.message, session, "修改密码失败")) {
                    val superAdmin = TokenManager.isSuperAdmin()
                    if (!superAdmin && TokenManager.clearIfCurrent(session.token, session.version)) synchronizeSession()
                    _passwordState.value = PasswordChangeState(success = true)
                    _notice.value = if (superAdmin) "密码修改成功" else "密码已修改，请重新登录"
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (!isCurrent(session) || _isLoggingOut.value) return@launch
                if (error is HttpException && error.code() == 401) expireSession(session)
                else _passwordState.value = PasswordChangeState(errorMessage = error.toUserMessage("修改密码失败"))
            } finally {
                if (isCurrent(session)) _passwordState.value = _passwordState.value.copy(isSubmitting = false)
            }
        }
    }

    fun resetPasswordState() {
        if (!_passwordState.value.isSubmitting) _passwordState.value = PasswordChangeState()
    }

    fun logout() {
        val session = currentSession() ?: return
        if (_isLoggingOut.value) return
        _isLoggingOut.value = true
        profileJob?.cancel()
        avatarJob?.cancel()
        passwordJob?.cancel()
        viewModelScope.launch {
            var warning: String? = null
            try {
                val response = withTimeout(10_000) { authApi.logout(session) }
                if (response.code !in listOf(0, 200, 401)) warning = response.message.orErrorMessage("注销失败")
            } catch (_: TimeoutCancellationException) {
                warning = "请求超时"
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (error !is HttpException || error.code() != 401) warning = error.toUserMessage("注销失败")
            } finally {
                if (TokenManager.clearIfCurrent(session.token, session.version)) {
                    synchronizeSession()
                    _notice.value = if (warning == null) "已退出登录" else "已退出本地登录，服务端注销未确认：$warning"
                }
                if (TokenManager.sessionVersion.value == session.version || !TokenManager.isLoggedIn()) {
                    _isLoggingOut.value = false
                }
            }
        }
    }

    fun clearNotice() { _notice.value = null }
    fun clearError() { _formState.value = _formState.value.copy(errorMessage = null) }
    fun switchLoginMethod(method: LoginMethod) { _formState.value = _formState.value.copy(loginMethod = method, errorMessage = null) }
    fun updatePhone(phone: String) { _formState.value = _formState.value.copy(phone = phone, errorMessage = null) }
    fun updateSmsCode(code: String) { _formState.value = _formState.value.copy(smsCode = code, errorMessage = null) }
    fun updateAccount(account: String) { _formState.value = _formState.value.copy(account = account, errorMessage = null) }
    fun updatePassword(password: String) { _formState.value = _formState.value.copy(password = password, errorMessage = null) }

    fun sendSmsCode() {
        val state = _formState.value
        if (state.isSendingSms || state.smsCountdown > 0) return
        if (!isValidPhone(state.phone)) {
            _formState.value = state.copy(errorMessage = "请输入正确的手机号")
            return
        }
        val token = TokenManager.getToken()
        val version = TokenManager.sessionVersion.value
        _formState.value = state.copy(isSendingSms = true, errorMessage = null)
        smsJob = viewModelScope.launch {
            try {
                val response = authApi.sendSmsCode(SendSmsCodeRequest(state.phone))
                if (!TokenManager.isCurrentSession(token, version)) return@launch
                if (response.code == 0 || response.code == 200) {
                    _formState.value = _formState.value.copy(smsCodeSent = true, isSendingSms = false, smsCountdown = 60)
                    countdownJob?.cancel()
                    countdownJob = viewModelScope.launch {
                        while (_formState.value.smsCountdown > 0) {
                            delay(1000)
                            _formState.value = _formState.value.copy(smsCountdown = _formState.value.smsCountdown - 1)
                        }
                    }
                } else _formState.value = _formState.value.copy(isSendingSms = false, errorMessage = response.message.orErrorMessage("发送验证码失败"))
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (TokenManager.isCurrentSession(token, version)) _formState.value = _formState.value.copy(isSendingSms = false, errorMessage = error.toUserMessage("发送验证码失败"))
            }
        }
    }

    fun loginBySms() {
        val state = _formState.value
        if (!isValidPhone(state.phone)) {
            _formState.value = state.copy(errorMessage = "请输入正确的手机号")
            return
        }
        if (state.smsCode.length < 4) {
            _formState.value = state.copy(errorMessage = "请输入验证码")
            return
        }
        login { authApi.loginBySms(SmsLoginRequest(state.phone, state.smsCode)) }
    }

    fun loginByPassword() {
        val state = _formState.value
        if (state.account.isBlank() || state.password.isBlank()) {
            _formState.value = state.copy(errorMessage = if (state.account.isBlank()) "请输入手机号或用户名" else "请输入密码")
            return
        }
        login { authApi.loginByPassword(PasswordLoginRequest(state.account, state.password)) }
    }

    private fun login(request: suspend () -> BaseResponse<LoginResponse>) {
        if (_formState.value.isLoggingIn) return
        val token = TokenManager.getToken()
        val version = TokenManager.sessionVersion.value
        _formState.value = _formState.value.copy(isLoggingIn = true, errorMessage = null)
        _notice.value = null
        loginJob = viewModelScope.launch {
            try {
                val response = request()
                if (!TokenManager.isCurrentSession(token, version)) return@launch
                if (response.code != 0 && response.code != 200) throw IOException(response.message.orErrorMessage("登录失败"))
                val data = requireNotNull(response.data) { "登录失败，未返回登录信息" }
                val nextToken = requireNotNull(data.token?.takeIf { it.isNotBlank() }) { "登录失败，未获取到登录凭证" }
                val user = requireNotNull(data.userInfo) { "登录失败，未获取到用户信息" }
                require((user.userId ?: 0) > 0) { "登录失败，未获取到用户标识" }
                TokenManager.saveToken(nextToken)
                TokenManager.setDebugSuperToken(false)
                TokenManager.saveUserInfo(user)
                synchronizeSession()
                _authState.value = user.toAuthState()
                _formState.value = LoginFormState()
                refreshProfile()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (TokenManager.isCurrentSession(token, version)) _formState.value = _formState.value.copy(isLoggingIn = false, errorMessage = error.toUserMessage("登录失败"))
            }
        }
    }

    private fun isValidPhone(phone: String) = phone.matches(Regex("^1[3-9]\\d{9}$"))
}
