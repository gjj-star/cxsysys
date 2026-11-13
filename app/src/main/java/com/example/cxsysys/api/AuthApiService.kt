package com.example.cxsysys.api

import com.example.cxsysys.model.*
import com.example.cxsysys.utils.AllowAnonymous
import com.example.cxsysys.utils.BusinessSession
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Tag

/**
 * 认证相关 API 接口
 *
 * 认证与个人中心接口，沿用业务客户端的 token 请求头。
 */
interface AuthApiService {

    /**
     * 获取短信验证码
     *
     * @param request 包含手机号和使用场景
     * @return 发送结果
     */
    @AllowAnonymous
    @POST("auth/smsCode")
    suspend fun sendSmsCode(@Body request: SendSmsCodeRequest): BaseResponse<Any>

    /**
     * 手机号 + 验证码登录
     *
     * @param request 手机号和验证码
     * @return 登录结果，包含 token、过期时间和用户信息
     */
    @AllowAnonymous
    @POST("auth/login/sms")
    suspend fun loginBySms(@Body request: SmsLoginRequest): BaseResponse<LoginResponse>

    /**
     * 手机号/用户名 + 密码登录
     *
     * @param request 账号和密码
     * @return 登录结果，包含 token、过期时间和用户信息
     */
    @AllowAnonymous
    @POST("auth/login/password")
    suspend fun loginByPassword(@Body request: PasswordLoginRequest): BaseResponse<LoginResponse>

    /**
     * 获取当前用户信息
     *
     * 需要 token 请求头
     * @return 用户信息
     */
    @GET("user")
    suspend fun getCurrentUser(@Tag session: BusinessSession? = null): BaseResponse<UserInfo>

    // 使用原始 JSON 请求体，确保清除头像时显式发送 avatar_url: null。
    @PUT("user")
    suspend fun updateAvatar(
        @Body request: RequestBody,
        @Tag session: BusinessSession? = null
    ): BaseResponse<UserInfo>

    @PUT("user/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest,
        @Tag session: BusinessSession? = null
    ): BaseResponse<Any>

    @GET("user/enterprise")
    suspend fun getEnterprise(): BaseResponse<EnterpriseInfo>

    @GET("user/enterprise/memberList")
    suspend fun getEnterpriseMembers(
        @Query("lastid") lastId: Int? = null,
        @Query("limit") limit: Int? = null
    ): BaseResponse<List<EnterpriseMember>>

    @POST("user/logout")
    suspend fun logout(@Tag session: BusinessSession? = null): BaseResponse<Any>
}
