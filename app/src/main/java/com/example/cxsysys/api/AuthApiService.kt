package com.example.cxsysys.api

import com.example.cxsysys.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 认证相关 API 接口
 *
 * 接口文档：auth用户登录.md
 * Base URL: https://dbcx.org.cn/plantingApi/
 */
interface AuthApiService {

    /**
     * 获取短信验证码
     *
     * @param request 包含手机号和使用场景
     * @return 发送结果
     */
    @POST("auth/smsCode")
    suspend fun sendSmsCode(@Body request: SendSmsCodeRequest): BaseResponse<Any>

    /**
     * 手机号 + 验证码登录
     *
     * @param request 手机号和验证码
     * @return 登录结果，包含 token、过期时间和用户信息
     */
    @POST("auth/login/sms")
    suspend fun loginBySms(@Body request: SmsLoginRequest): BaseResponse<LoginResponse>

    /**
     * 手机号/用户名 + 密码登录
     *
     * @param request 账号和密码
     * @return 登录结果，包含 token、过期时间和用户信息
     */
    @POST("auth/login/password")
    suspend fun loginByPassword(@Body request: PasswordLoginRequest): BaseResponse<LoginResponse>

    /**
     * 获取当前用户信息
     *
     * 需要 Authorization header（JWT token）
     * @return 用户信息
     */
    @GET("user")
    suspend fun getCurrentUser(): BaseResponse<UserInfo>
}
