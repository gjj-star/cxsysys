package com.example.cxsysys.model

import com.google.gson.annotations.SerializedName

// ========== 认证相关数据模型 ==========

/**
 * 发送短信验证码请求
 *
 * @param phoneNumber 十一位手机号
 * @param scene 验证码使用场景（如 "login"）
 */
data class SendSmsCodeRequest(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("scene") val scene: String = "login"
)

/**
 * 手机号 + 验证码登录请求
 */
data class SmsLoginRequest(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("sms_code") val smsCode: String
)

/**
 * 手机号/用户名 + 密码登录请求
 */
data class PasswordLoginRequest(
    @SerializedName("account") val account: String,
    @SerializedName("password") val password: String
)

/**
 * 登录响应数据（嵌套结构）
 *
 * 后端返回格式：
 * {
 *   "token": "jwt...",
 *   "expires_in": 7200,
 *   "user_info": { ... }
 * }
 */
data class LoginResponse(
    @SerializedName("token") val token: String? = null,
    @SerializedName("expires_in") val expiresIn: Int? = null,
    @SerializedName("user_info") val userInfo: UserInfo? = null
)

/**
 * 用户信息
 */
data class UserInfo(
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("user_name") val userName: String? = null,
    @SerializedName("real_name") val realName: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("enterprise_id") val enterpriseId: Int? = null,
    @SerializedName("dept_id") val deptId: Int? = null,
    @SerializedName("super_admin") val superAdmin: Boolean = false
)
