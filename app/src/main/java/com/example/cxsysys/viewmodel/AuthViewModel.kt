package com.example.cxsysys.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.model.*
import com.example.cxsysys.utils.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 登录方式枚举
 */
enum class LoginMethod {
    SMS_CODE,      // 手机号 + 验证码
    PASSWORD       // 手机号/用户名 + 密码
}

/**
 * 认证状态
 */
sealed class AuthState {
    /** 未登录 */
    object NotLoggedIn : AuthState()

    /** 登录中 */
    object Loading : AuthState()

    /** 已登录 */
    data class LoggedIn(
        val userId: Int,
        val username: String?,
        val phone: String?,
        val realName: String?,
        val avatarUrl: String?,
        val enterpriseName: String?
    ) : AuthState()

    /** 登录失败 */
    data class Error(val message: String) : AuthState()
}

/**
 * 登录表单状态
 */
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

/**
 * 认证 ViewModel
 *
 * 处理登录相关的业务逻辑，包括：
 * - 手机号 + 验证码登录
 * - 手机号/用户名 + 密码登录
 * - 验证码发送倒计时
 * - 登录状态管理
 *
 * 注意：后端正在同步开发此功能，此处先实现前端框架。
 * 实际 API 调用可能会根据后端接口调整。
 */
class AuthViewModel : ViewModel() {

    private val authApi = RetrofitClient.authApi

    // 认证状态
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotLoggedIn)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // 登录表单状态
    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    companion object {
        private const val SMS_COUNTDOWN_SECONDS = 60
    }

    /**
     * 切换登录方式
     */
    fun switchLoginMethod(method: LoginMethod) {
        _formState.value = _formState.value.copy(
            loginMethod = method,
            errorMessage = null
        )
    }

    /**
     * 更新手机号
     */
    fun updatePhone(phone: String) {
        _formState.value = _formState.value.copy(phone = phone, errorMessage = null)
    }

    /**
     * 更新验证码
     */
    fun updateSmsCode(code: String) {
        _formState.value = _formState.value.copy(smsCode = code, errorMessage = null)
    }

    /**
     * 更新账号（手机号或用户名）
     */
    fun updateAccount(account: String) {
        _formState.value = _formState.value.copy(account = account, errorMessage = null)
    }

    /**
     * 更新密码
     */
    fun updatePassword(password: String) {
        _formState.value = _formState.value.copy(password = password, errorMessage = null)
    }

    /**
     * 发送短信验证码
     *
     * 注意：此功能需要后端支持，当前为框架代码。
     * 实际使用时需要后端提供发送验证码的 API。
     */
    fun sendSmsCode() {
        val phone = _formState.value.phone

        // 验证手机号格式
        if (!isValidPhone(phone)) {
            _formState.value = _formState.value.copy(errorMessage = "请输入正确的手机号")
            return
        }

        viewModelScope.launch {
            _formState.value = _formState.value.copy(isSendingSms = true, errorMessage = null)

            try {
                val response = authApi.sendSmsCode(SendSmsCodeRequest(phone = phone))

                if (response.code == 0 || response.code == 200) {
                    _formState.value = _formState.value.copy(
                        smsCodeSent = true,
                        isSendingSms = false,
                        smsCountdown = SMS_COUNTDOWN_SECONDS
                    )

                    // 启动倒计时
                    startSmsCountdown()
                } else {
                    _formState.value = _formState.value.copy(
                        isSendingSms = false,
                        errorMessage = response.message.ifEmpty { "发送验证码失败" }
                    )
                }
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isSendingSms = false,
                    errorMessage = "发送验证码失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 验证码倒计时
     */
    private fun startSmsCountdown() {
        viewModelScope.launch {
            while (_formState.value.smsCountdown > 0) {
                delay(1000)
                _formState.value = _formState.value.copy(
                    smsCountdown = _formState.value.smsCountdown - 1
                )
            }
        }
    }

    /**
     * 手机号 + 验证码登录
     */
    fun loginBySms() {
        val state = _formState.value
        val phone = state.phone
        val smsCode = state.smsCode

        // 验证
        if (!isValidPhone(phone)) {
            _formState.value = state.copy(errorMessage = "请输入正确的手机号")
            return
        }
        if (smsCode.length < 4) {
            _formState.value = state.copy(errorMessage = "请输入验证码")
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isLoggingIn = true, errorMessage = null)

            try {
                val response = authApi.loginBySms(
                    SmsLoginRequest(phone = phone, smsCode = smsCode)
                )

                if (response.code == 0 || response.code == 200) {
                    response.data?.let { loginResponse ->
                        _authState.value = AuthState.LoggedIn(
                            userId = loginResponse.userId ?: 0,
                            username = loginResponse.username,
                            phone = loginResponse.phone ?: phone,
                            realName = loginResponse.realName,
                            avatarUrl = loginResponse.avatarUrl,
                            enterpriseName = loginResponse.enterpriseName
                        )
                        _formState.value = LoginFormState() // 重置表单
                    }
                } else {
                    _formState.value = state.copy(
                        isLoggingIn = false,
                        errorMessage = response.message.ifEmpty { "登录失败" }
                    )
                }
            } catch (e: Exception) {
                _formState.value = state.copy(
                    isLoggingIn = false,
                    errorMessage = "登录失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 手机号/用户名 + 密码登录
     */
    fun loginByPassword() {
        val state = _formState.value
        val account = state.account
        val password = state.password

        // 验证
        if (account.isBlank()) {
            _formState.value = state.copy(errorMessage = "请输入手机号或用户名")
            return
        }
        if (password.isBlank()) {
            _formState.value = state.copy(errorMessage = "请输入密码")
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isLoggingIn = true, errorMessage = null)

            try {
                val response = authApi.loginByPassword(
                    PasswordLoginRequest(account = account, password = password)
                )

                if (response.code == 0 || response.code == 200) {
                    response.data?.let { loginResponse ->
                        _authState.value = AuthState.LoggedIn(
                            userId = loginResponse.userId ?: 0,
                            username = loginResponse.username ?: account,
                            phone = loginResponse.phone,
                            realName = loginResponse.realName,
                            avatarUrl = loginResponse.avatarUrl,
                            enterpriseName = loginResponse.enterpriseName
                        )
                        _formState.value = LoginFormState() // 重置表单
                    }
                } else {
                    _formState.value = state.copy(
                        isLoggingIn = false,
                        errorMessage = response.message.ifEmpty { "登录失败" }
                    )
                }
            } catch (e: Exception) {
                _formState.value = state.copy(
                    isLoggingIn = false,
                    errorMessage = "登录失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 退出登录
     */
    fun logout() {
        viewModelScope.launch {
            try {
                authApi.logout()
            } catch (_: Exception) {
                // 忽略退出登录的网络错误
            }
            _authState.value = AuthState.NotLoggedIn
            _formState.value = LoginFormState()
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _formState.value = _formState.value.copy(errorMessage = null)
    }

    /**
     * 验证手机号格式
     */
    private fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^1[3-9]\\d{9}$"))
    }
}
