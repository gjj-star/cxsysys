package com.example.cxsysys.api

import com.example.cxsysys.model.*
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 认证相关 API 接口
 *
 * 当前后端正在同步开发此功能，此处先定义接口框架。
 * 实际 URL 路径待后端确认后可能需要调整。
 */
interface AuthApiService {

    /**
     * 发送手机验证码
     *
     * @param phone 手机号
     * @return 发送结果
     */
    @POST("auth/sendSmsCode")
    suspend fun sendSmsCode(@Body request: SendSmsCodeRequest): BaseResponse<Any>

    /**
     * 手机号 + 验证码登录
     *
     * @param request 手机号和验证码
     * @return 登录结果，包含用户信息和 token
     */
    @POST("auth/loginBySms")
    suspend fun loginBySms(@Body request: SmsLoginRequest): BaseResponse<LoginResponse>

    /**
     * 手机号/用户名 + 密码登录
     *
     * @param request 账号和密码
     * @return 登录结果，包含用户信息和 token
     */
    @POST("auth/loginByPassword")
    suspend fun loginByPassword(@Body request: PasswordLoginRequest): BaseResponse<LoginResponse>

    /**
     * 退出登录
     */
    @POST("auth/logout")
    suspend fun logout(): BaseResponse<Any>
}
