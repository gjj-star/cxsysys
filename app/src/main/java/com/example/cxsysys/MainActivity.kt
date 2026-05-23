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

        // TODO: 测试阶段临时写入超级管理员 token，正式上线前移除
        if (!TokenManager.isLoggedIn()) {
            TokenManager.saveToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMiIsInVzZXJJZCI6MTIsInVzZXJuYW1lIjoic3VwZXJwbGFudGluZyIsImVudGVycHpc2VJZCI6MSwiaWF0IjoxNzc4ODUwMDAwLCJleHAiOjI1MzQwMjI3MTk5OX0.TKSoJJNqlGrAKbFM3GCsmN29MQMz12iL55hTOkl7dMc")
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
