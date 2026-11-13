package com.example.cxsysys.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.cxsysys.model.UserInfo
import com.example.cxsysys.model.UserRole
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * JWT Token 管理器
 *
 * 负责 token 的持久化存储和读取。
 * 使用 SharedPreferences 存储（简单可靠）。
 * 生产环境可升级为 EncryptedSharedPreferences。
 */
object TokenManager {

    private const val PREF_NAME = "auth_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_REAL_NAME = "real_name"
    private const val KEY_PHONE = "phone_number"
    private const val KEY_AVATAR_URL = "avatar_url"
    private const val KEY_ENTERPRISE_ID = "enterprise_id"
    private const val KEY_DEPT_ID = "dept_id"
    private const val KEY_SUPER_ADMIN = "super_admin"
    private const val KEY_DEBUG_SUPER_TOKEN = "debug_super_token"
    private const val KEY_ENTERPRISE_NAME = "enterprise_name"
    private const val KEY_ROLES = "roles"
    private const val KEY_STATUS = "status"

    private lateinit var prefs: SharedPreferences
    private val _loginState = MutableStateFlow(false)
    val loginState = _loginState.asStateFlow()
    private val _sessionVersion = MutableStateFlow(0L)
    val sessionVersion = _sessionVersion.asStateFlow()

    /**
     * 初始化（在 Application 或 MainActivity 中调用一次）
     */
    fun init(context: Context) {
        init(context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE))
    }

    @Synchronized
    internal fun init(preferences: SharedPreferences) {
        prefs = preferences
        _sessionVersion.value++
        _loginState.value = isLoggedIn()
    }

    // ========== Token 操作 ==========

    @Synchronized
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
        _sessionVersion.value++
        _loginState.value = isLoggedIn()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clearToken() = clearAll()

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    @Synchronized
    fun isCurrentSession(token: String?, version: Long): Boolean =
        version == _sessionVersion.value && token == getToken()

    @Synchronized
    fun clearIfCurrent(token: String, version: Long): Boolean {
        if (!isCurrentSession(token, version)) return false
        clearAll()
        return true
    }

    fun applyDebugSuperToken(enabled: Boolean, token: String) {
        if (enabled) {
            saveToken(token)
            saveUserInfo(
                userId = 12,
                userName = "superplanting",
                realName = "超级管理员",
                phoneNumber = null,
                avatarUrl = null,
                enterpriseId = 1,
                deptId = null,
                superAdmin = true
            )
            setDebugSuperToken(true)
        } else if (isDebugSuperToken()) {
            clearAll()
        }
    }

    fun setDebugSuperToken(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEBUG_SUPER_TOKEN, enabled).apply()
    }

    private fun isDebugSuperToken(): Boolean = prefs.getBoolean(KEY_DEBUG_SUPER_TOKEN, false)

    // ========== 用户信息操作 ==========

    fun saveUserInfo(
        userId: Int,
        userName: String?,
        realName: String?,
        phoneNumber: String?,
        avatarUrl: String?,
        enterpriseId: Int?,
        deptId: Int?,
        superAdmin: Boolean,
        enterpriseName: String? = null,
        roles: List<UserRole> = emptyList(),
        status: Int? = null
    ) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_REAL_NAME, realName)
            putString(KEY_PHONE, phoneNumber)
            putString(KEY_AVATAR_URL, avatarUrl)
            if (enterpriseId == null) remove(KEY_ENTERPRISE_ID) else putInt(KEY_ENTERPRISE_ID, enterpriseId)
            if (deptId == null) remove(KEY_DEPT_ID) else putInt(KEY_DEPT_ID, deptId)
            putBoolean(KEY_SUPER_ADMIN, superAdmin)
            putString(KEY_ENTERPRISE_NAME, enterpriseName)
            putString(KEY_ROLES, Gson().toJson(roles))
            if (status == null) remove(KEY_STATUS) else putInt(KEY_STATUS, status)
            apply()
        }
    }

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, 0)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getRealName(): String? = prefs.getString(KEY_REAL_NAME, null)
    fun getPhoneNumber(): String? = prefs.getString(KEY_PHONE, null)
    fun getAvatarUrl(): String? = prefs.getString(KEY_AVATAR_URL, null)
    fun getEnterpriseId(): Int = prefs.getInt(KEY_ENTERPRISE_ID, 0)
    fun getDeptId(): Int? = if (prefs.contains(KEY_DEPT_ID)) prefs.getInt(KEY_DEPT_ID, 0) else null
    fun isSuperAdmin(): Boolean = prefs.getBoolean(KEY_SUPER_ADMIN, false)

    fun saveUserInfo(user: UserInfo) = saveUserInfo(
        userId = user.userId ?: 0,
        userName = user.userName,
        realName = user.realName,
        phoneNumber = user.phoneNumber,
        avatarUrl = user.avatarUrl,
        enterpriseId = user.enterpriseId,
        deptId = user.deptId,
        superAdmin = user.superAdmin,
        enterpriseName = user.enterpriseName,
        roles = user.roles.orEmpty(),
        status = user.status
    )

    fun getUserInfo(): UserInfo = UserInfo(
        userId = getUserId(),
        userName = getUserName(),
        realName = getRealName(),
        phoneNumber = getPhoneNumber(),
        avatarUrl = getAvatarUrl(),
        enterpriseId = getEnterpriseId(),
        deptId = getDeptId(),
        superAdmin = isSuperAdmin(),
        enterpriseName = prefs.getString(KEY_ENTERPRISE_NAME, null),
        roles = runCatching {
            Gson().fromJson(prefs.getString(KEY_ROLES, "[]"), Array<UserRole>::class.java)?.toList().orEmpty()
        }.getOrDefault(emptyList()),
        status = if (prefs.contains(KEY_STATUS)) prefs.getInt(KEY_STATUS, 0) else null
    )

    /**
     * 清除所有数据（退出登录时调用）
     */
    @Synchronized
    fun clearAll() {
        prefs.edit().clear().apply()
        _sessionVersion.value++
        _loginState.value = false
    }
}
