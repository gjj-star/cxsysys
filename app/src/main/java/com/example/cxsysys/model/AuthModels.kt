package com.example.cxsysys.model

import com.google.gson.annotations.SerializedName

// ========== 认证相关数据模型 ==========

/**
 * 发送短信验证码请求
 */
data class SendSmsCodeRequest(
    @SerializedName("phone") val phone: String
)

/**
 * 手机号 + 验证码登录请求
 */
data class SmsLoginRequest(
    @SerializedName("phone") val phone: String,
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
 * 登录响应数据
 */
data class LoginResponse(
    @SerializedName("token") val token: String? = null,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("real_name") val realName: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("enterprise_id") val enterpriseId: Int? = null,
    @SerializedName("enterprise_name") val enterpriseName: String? = null
)
