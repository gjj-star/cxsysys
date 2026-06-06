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
fun EnterpriseScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("企业管理") },
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
            // 企业基本信息
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = AgGreenPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("企业信息", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    EnterpriseInfoRow(label = "企业名称", value = "—")
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    EnterpriseInfoRow(label = "企业ID", value = "—")
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    EnterpriseInfoRow(label = "联系电话", value = "—")
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    EnterpriseInfoRow(label = "企业地址", value = "—")
                }
            }

            // 成员管理（占位）
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = AgGreenPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("成员管理", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("成员列表将在接口就绪后展示", fontSize = 14.sp, color = Color(0xFF999999))
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
private fun EnterpriseInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, color = Color(0xFF666666), modifier = Modifier.width(80.dp))
        Text(value, fontSize = 15.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium)
    }
}
