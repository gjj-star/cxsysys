package com.example.cxsysys

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cxsysys.ui.MainScreen
import com.example.cxsysys.ui.theme.CXSYSYSTheme
import com.example.cxsysys.utils.TokenManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 TokenManager（JWT token 持久化管理）
        TokenManager.init(applicationContext)

        // 超级管理员 token 调试模式（由 gradle.properties 的 enableSuperToken 控制）
        // 开启后启动时注入永不过期的超级管理员 token，免登录测试 API
        if (BuildConfig.ENABLE_SUPER_TOKEN && !TokenManager.isLoggedIn()) {
            TokenManager.saveToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMiIsInVzZXJJZCI6MTIsInVzZXJuYW1lIjoic3VwZXJwbGFudGluZyIsImVudGVycHJpc2VJZCI6MSwiaWF0IjoxNzc4ODUwMDAwLCJleHAiOjI1MzQwMjI3MTk5OX0.TKSoJJNqlGrAKbFM3GCsmN29MQMz12iL55hTOkl7dMc")
            TokenManager.saveUserInfo(
                userId = 12,
                userName = "superplanting",
                realName = "超级管理员",
                phoneNumber = null,
                avatarUrl = null,
                enterpriseId = 1,
                deptId = null,
                superAdmin = true
            )
        }

        enableEdgeToEdge() // 启用全面屏
        setContent {
            CXSYSYSTheme {
                // 调用写好的主界面
                MainScreen()
            }
        }
    }
}
