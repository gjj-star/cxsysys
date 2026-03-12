package com.example.cxsysys

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cxsysys.ui.MainScreen
import com.example.cxsysys.ui.theme.CXSYSYSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 启用全面屏
        setContent {
            CXSYSYSTheme {
                // 调用写好的主界面
                MainScreen()
            }
        }
    }
}