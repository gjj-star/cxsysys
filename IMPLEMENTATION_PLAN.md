# 认证系统升级 + 个人中心优化 实施方案

## 📋 项目现状分析

### 当前架构
- **技术栈**: Android Native (Kotlin + Jetpack Compose + Material3)
- **架构模式**: MVVM (ViewModel + StateFlow)
- **网络层**: Retrofit + OkHttp
- **导航**: Navigation Compose

### 需要修改的内容
1. **API 接口对齐** - 当前接口路径与新文档不一致
2. **JWT 认证集成** - 移除 `user-enterprise-id` header，改用 JWT token
3. **数据模型更新** - 响应字段与新文档对齐
4. **Token 持久化** - 使用 DataStore 存储 token
5. **个人中心优化** - 保持现有风格，增强功能

---

## 🎯 实施步骤

### Phase 1: 数据层更新 (Models & API)

#### 1.1 更新 AuthModels.kt
```kotlin
// 新增请求模型
data class SendSmsCodeRequest(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("scene") val scene: String
)

data class SmsLoginRequest(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("sms_code") val smsCode: String
)

data class PasswordLoginRequest(
    @SerializedName("account") val account: String,
    @SerializedName("password") val password: String
)

// 更新响应模型
data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("user_info") val userInfo: UserInfo
)

data class UserInfo(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user_name") val userName: String,
    @SerializedName("real_name") val realName: String?,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("enterprise_id") val enterpriseId: Int,
    @SerializedName("dept_id") val deptId: Int?,
    @SerializedName("super_admin") val superAdmin: Boolean
)
```

#### 1.2 更新 AuthApiService.kt
```kotlin
interface AuthApiService {
    @POST("auth/smsCode")
    suspend fun sendSmsCode(@Body request: SendSmsCodeRequest): BaseResponse<Any>

    @POST("auth/login/sms")
    suspend fun loginBySms(@Body request: SmsLoginRequest): BaseResponse<LoginResponse>

    @POST("auth/login/password")
    suspend fun loginByPassword(@Body request: PasswordLoginRequest): BaseResponse<LoginResponse>

    @GET("user")
    suspend fun getCurrentUser(): BaseResponse<UserInfo>
}
```

### Phase 2: Token 管理

#### 2.1 创建 TokenManager.kt
```kotlin
// 路径: utils/TokenManager.kt
object TokenManager {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_ENTERPRISE_ID = "enterprise_id"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clearToken() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getToken() != null
}
```

#### 2.2 更新 RetrofitClient.kt
```kotlin
// 移除 businessInterceptor 中的 user-enterprise-id
// 添加 JWT token 拦截器
private val authInterceptor = Interceptor { chain ->
    val originalRequest = chain.request()
    val token = TokenManager.getToken()

    val newRequest = if (token != null) {
        originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    } else {
        originalRequest
    }

    chain.proceed(newRequest)
}
```

### Phase 3: ViewModel 更新

#### 3.1 更新 AuthViewModel.kt
- 集成 TokenManager
- 登录成功后保存 token
- 添加自动登录检查
- 添加获取用户信息功能

### Phase 4: UI 优化

#### 4.1 个人中心页面 (MineScreen.kt)
- 保持现有绿色主题风格
- 增强用户信息展示
- 添加更多功能入口

---

## 🎨 设计规范

### 颜色方案 (保持现有)
- **主色调**: AgGreenPrimary (#4CAF50)
- **深色**: AgGreenDark (#388E3C)
- **浅色**: AgGreenLight (#E8F5E9)
- **背景**: BgGray (#F5F5F5)

### 组件风格
- **卡片**: RoundedCornerShape(12.dp), 白色背景
- **按钮**: RoundedCornerShape(8.dp), 绿色填充
- **图标**: 圆形背景 + 绿色图标
- **文字**: 标题 16sp Medium, 副标题 12sp Gray

---

## 📁 文件变更清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `model/AuthModels.kt` | 修改 | 更新请求/响应模型 |
| `api/AuthApiService.kt` | 修改 | 更新 API 端点 |
| `utils/TokenManager.kt` | 新增 | Token 持久化管理 |
| `utils/RetrofitClient.kt` | 修改 | JWT 拦截器 |
| `viewmodel/AuthViewModel.kt` | 修改 | 集成 token 管理 |
| `ui/screens/mine/MineScreen.kt` | 修改 | UI 优化 |

---

## 🧪 测试用例

### 测试 Token
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMiIsInVzZXJJZCI6MTIsInVzZXJuYW1lIjoic3VwZXJwbGFudGluZyIsImVudGVycHJpc2VJZCI6MSwiaWF0IjoxNzc4ODUwMDAwLCJleHAiOjI1MzQwMjI3MTk5OX0.TKSoJJNqlGrAKbFM3GCsmN29MQMz12iL55hTOkl7dMc
```

### 测试场景
1. ✅ 验证码发送
2. ✅ 手机号+验证码登录
3. ✅ 手机号/用户名+密码登录
4. ✅ Token 持久化
5. ✅ 自动登录
6. ✅ 退出登录
7. ✅ 获取用户信息

---

## ⚠️ 注意事项

1. **向后兼容**: 保持现有 UI 风格不变
2. **错误处理**: 统一的错误提示机制
3. **安全性**: Token 使用 SharedPreferences 存储（生产环境建议 EncryptedSharedPreferences）
4. **测试**: 使用提供的超级管理员 token 进行测试

---

## 🚀 开始实施

准备就绪后，我将按以下顺序实施：
1. 更新数据模型 (AuthModels.kt)
2. 更新 API 接口 (AuthApiService.kt)
3. 创建 Token 管理器 (TokenManager.kt)
4. 更新网络客户端 (RetrofitClient.kt)
5. 更新 ViewModel (AuthViewModel.kt)
6. 优化 UI (MineScreen.kt)
