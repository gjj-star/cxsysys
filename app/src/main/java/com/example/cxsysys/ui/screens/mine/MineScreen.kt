package com.example.cxsysys.ui.screens.mine

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MineScreen() {
    val context = LocalContext.current

    // 读取 App 版本信息（由 BuildConfig 自动生成）
    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (e: Exception) {
        null
    }
    @Suppress("DEPRECATION")
    val versionName = packageInfo?.versionName ?: "未知"
    @Suppress("DEPRECATION")
    val versionCode = packageInfo?.versionCode?.toString() ?: "-"

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "个人中心",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "界面开发中...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 版本号显示
        Text(
            text = "版本 $versionName (Build $versionCode)",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
