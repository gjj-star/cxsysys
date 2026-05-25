package com.example.cxsysys.utils

import android.content.Context
import android.content.SharedPreferences

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

    private lateinit var prefs: SharedPreferences

    /**
     * 初始化（在 Application 或 MainActivity 中调用一次）
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // ========== Token 操作 ==========

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun isLoggedIn(): Boolean = getToken() != null

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
        superAdmin: Boolean
    ) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_REAL_NAME, realName)
            putString(KEY_PHONE, phoneNumber)
            putString(KEY_AVATAR_URL, avatarUrl)
            enterpriseId?.let { putInt(KEY_ENTERPRISE_ID, it) }
            deptId?.let { putInt(KEY_DEPT_ID, it) }
            putBoolean(KEY_SUPER_ADMIN, superAdmin)
            apply()
        }
    }

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, 0)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getRealName(): String? = prefs.getString(KEY_REAL_NAME, null)
    fun getPhoneNumber(): String? = prefs.getString(KEY_PHONE, null)
    fun getAvatarUrl(): String? = prefs.getString(KEY_AVATAR_URL, null)
    fun getEnterpriseId(): Int = prefs.getInt(KEY_ENTERPRISE_ID, 0)
    fun getDeptId(): Int = prefs.getInt(KEY_DEPT_ID, 0)
    fun isSuperAdmin(): Boolean = prefs.getBoolean(KEY_SUPER_ADMIN, false)

    /**
     * 清除所有数据（退出登录时调用）
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
