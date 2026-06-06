package com.example.cxsysys.ui.screens.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.AgGreenLight
import com.example.cxsysys.ui.theme.BgGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (e: Exception) {
        null
    }
    @Suppress("DEPRECATION")
    val versionName = packageInfo?.versionName ?: "未知"
    @Suppress("DEPRECATION")
    val versionCode = packageInfo?.versionCode?.toString() ?: "-"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgGreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgGray)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 应用图标
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(AgGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Forest,
                    contentDescription = null,
                    tint = AgGreenPrimary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "沉香溯源系统",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Text(
                "版本 $versionName (Build $versionCode)",
                fontSize = 14.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 应用信息
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AboutInfoRow(label = "应用名称", value = "沉香溯源系统")
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    AboutInfoRow(label = "应用版本", value = versionName)
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    AboutInfoRow(label = "构建号", value = versionCode)
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    AboutInfoRow(label = "技术框架", value = "Jetpack Compose")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 法律信息
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AboutInfoRow(label = "开发者", value = "沉香溯源团队")
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    AboutInfoRow(label = "版权声明", value = "© 2026 All Rights Reserved")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "本系统用于沉香种植全流程溯源管理\n涵盖母树、幼苗、苗木、农事作业等环节",
                fontSize = 13.sp,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, color = Color(0xFF666666), modifier = Modifier.width(80.dp))
        Text(value, fontSize = 15.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium)
    }
}
