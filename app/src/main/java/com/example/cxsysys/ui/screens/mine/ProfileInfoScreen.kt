package com.example.cxsysys.ui.screens.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInfoScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人信息") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 头像
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("头像", fontSize = 16.sp, color = Color(0xFF333333), modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFCCCCCC))
                }
            }

            // 基本信息卡片
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileInfoRow(icon = Icons.Default.Person, label = "姓名", value = "—")
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    ProfileInfoRow(icon = Icons.Default.Phone, label = "手机号", value = "—")
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    ProfileInfoRow(icon = Icons.Default.Badge, label = "用户名", value = "—")
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    ProfileInfoRow(icon = Icons.Default.Business, label = "所属企业", value = "—")
                }
            }

            // 身份信息卡片
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileInfoRow(icon = Icons.Default.AdminPanelSettings, label = "角色", value = "—")
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    ProfileInfoRow(icon = Icons.Default.VerifiedUser, label = "认证状态", value = "—")
                }
            }

            Text(
                "接口开发中，数据将在后端就绪后自动加载",
                fontSize = 12.sp,
                color = Color(0xFFAAAAAA),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AgGreenPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, color = Color(0xFF666666), modifier = Modifier.width(72.dp))
        Text(value, fontSize = 15.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}
