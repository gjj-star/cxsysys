package com.example.cxsysys.ui.screens.children

import android.widget.Toast
import androidx.compose.foundation.BorderStroke // [修复] 添加 BorderStroke 引用
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildrenDetailScreen(saplingId: String, onBackClick: () -> Unit) {
    val context = LocalContext.current

    // --- 模拟数据状态 ---
    var qrCode by remember { mutableStateOf(saplingId) } // 幼苗二维码
    var motherTreeName by remember { mutableStateOf("母树-2012-A001") } // [修改] 中文前缀
    var generation by remember { mutableStateOf("1") } // 代数
    var generationWay by remember { mutableStateOf("嫁接") } // 育苗方法
    var subspeciesName by remember { mutableStateOf("金丝油 (奇楠)") } // 品种
    var saplingDate by remember { mutableStateOf("2023-10-01") } // 育苗日期
    var greenhouseName by remember { mutableStateOf("1号智能温室") } // 种植大棚

    // 状态: 0正常, 1冻结/售出, 2注销/死亡
    var status by remember { mutableIntStateOf(0) }

    // 修改弹窗控制
    var showEditDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    // 状态描述映射
    val statusMap = mapOf(0 to "正常/存活", 1 to "冻结/售出", 2 to "注销/死亡")
    val statusColors = mapOf(0 to AgGreenPrimary, 1 to Color(0xFFFFA000), 2 to Color.Red)

    // 基本信息修改弹窗
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("修改幼苗信息") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = greenhouseName,
                        onValueChange = { greenhouseName = it },
                        label = { Text("所在大棚") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = generationWay,
                        onValueChange = { generationWay = it },
                        label = { Text("育苗方式") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showEditDialog = false; Toast.makeText(context, "已保存修改", Toast.LENGTH_SHORT).show() }, colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary)) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("取消") } }
        )
    }

    // 状态修改弹窗
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("变更幼苗状态") },
            text = {
                Column {
                    statusMap.forEach { (key, value) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { status = key }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = (status == key), onClick = { status = key })
                            Text(text = value, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showStatusDialog = false; Toast.makeText(context, "状态已更新", Toast.LENGTH_SHORT).show() }, colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary)) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showStatusDialog = false }) { Text("取消") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("幼苗详情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BgGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 顶部状态卡片
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(subspeciesName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AgGreenPrimary)
                            Text(qrCode, color = Color.Gray, fontSize = 14.sp)
                        }
                        // 状态标签 (可点击修改)
                        Surface(
                            color = statusColors[status]?.copy(alpha = 0.1f) ?: Color.Gray,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, statusColors[status] ?: Color.Gray), // [修复] 使用正确的 BorderStroke
                            modifier = Modifier.clickable { showStatusDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = statusMap[status] ?: "未知",
                                    color = statusColors[status] ?: Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Edit, null, tint = statusColors[status] ?: Color.Gray, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }

            // 2. 详细信息卡片
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("基本档案", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { showEditDialog = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, "Edit", tint = Color.Gray)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BgGray) // [修复] 使用 HorizontalDivider

                    InfoRow("母树来源", motherTreeName)
                    InfoRow("代数", "$generation 代")
                    InfoRow("育苗方式", generationWay)
                    InfoRow("育苗日期", saplingDate)
                    InfoRow("所属大棚", greenhouseName)
                }
            }

            // 3. 提示区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Info, null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "幼苗二维码一经生成不可更改。若幼苗死亡或售出，请点击右上角状态标签进行变更。",
                    color = Color(0xFF0D47A1),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}