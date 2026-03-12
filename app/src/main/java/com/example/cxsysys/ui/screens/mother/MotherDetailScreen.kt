package com.example.cxsysys.ui.screens.mother

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.window.Dialog
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotherDetailScreen(motherTreeId: String, onBackClick: () -> Unit) {
    val context = LocalContext.current

    // --- 模拟数据状态 ---
    var qrCode by remember { mutableStateOf(motherTreeId) }
    var dnaBarcode by remember { mutableStateOf("ITS2-AGCT-0098-X7") }
    var subspeciesName by remember { mutableStateOf("金丝油 (奇楠)") }
    var treeAge by remember { mutableStateOf("12年") }
    var longitude by remember { mutableStateOf("110.98765") }
    var latitude by remember { mutableStateOf("21.54321") }
    var photoUrl by remember { mutableStateOf("") }

    // 状态: 0正常, 1冻结, 2注销/死亡
    var status by remember {
        mutableIntStateOf(if (motherTreeId.contains("C012")) 1 else 0)
    }

    // 修改弹窗控制
    var showEditDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    // 状态描述映射
    val statusMap = mapOf(0 to "正常", 1 to "冻结", 2 to "注销/死亡")
    val statusColors = mapOf(0 to AgGreenPrimary, 1 to Color(0xFFFFA000), 2 to Color.Red)

    // 基本信息修改弹窗
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("修改母树信息") },
            text = {
                // [修改] 增加 verticalScroll 防止字段过多超出屏幕，并补全所有需要修改的字段
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = qrCode,
                        onValueChange = { qrCode = it },
                        label = { Text("母树二维码") }, // [新增]
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.QrCodeScanner, null) } // [保留扫描功能]
                    )
                    OutlinedTextField(
                        value = dnaBarcode,
                        onValueChange = { dnaBarcode = it },
                        label = { Text("母树DNA条形码") }, // [新增]
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subspeciesName,
                        onValueChange = { subspeciesName = it },
                        label = { Text("母树品种细分") }, // [修改] (原“品种细分”改成“母树品种细分”)
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = treeAge,
                        onValueChange = { treeAge = it },
                        label = { Text("母树树龄") }, // [修改] (原“树龄”改成“母树树龄”)
                        modifier = Modifier.fillMaxWidth()
                    )
                    // [修改] 经纬度彻底分开
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = longitude,
                            onValueChange = { longitude = it },
                            label = { Text("经度") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = latitude,
                            onValueChange = { latitude = it },
                            label = { Text("纬度") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = photoUrl,
                        onValueChange = { photoUrl = it },
                        label = { Text("母树照片地址") }, // [新增]
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditDialog = false
                        Toast.makeText(context, "修改申请已提交，等待审核", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary)
                ) { Text("提交修改") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("取消") } }
        )
    }

    // 状态修改弹窗
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("变更母树状态") },
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
                Button(
                    onClick = {
                        showStatusDialog = false
                        Toast.makeText(context, "状态已更新", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary)
                ) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showStatusDialog = false }) { Text("取消") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("母树详情", fontWeight = FontWeight.Bold) },
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
            // 1. 顶部核心信息卡片
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(subspeciesName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AgGreenPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(qrCode, color = Color.Gray, fontSize = 14.sp)
                        }

                        // 状态标签
                        Surface(
                            color = statusColors[status]?.copy(alpha = 0.1f) ?: Color.Gray,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, statusColors[status] ?: Color.Gray),
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

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BgGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    // DNA 条形码
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // [修改] 替换为 Science 图标
                        Icon(Icons.Default.Science, null, tint = Color(0xFF9C27B0), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("DNA 条形码", fontSize = 12.sp, color = Color.Gray)
                            Text(dnaBarcode, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // 2. 详细参数卡片
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
                        Text("详细档案", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { showEditDialog = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, "Edit", tint = Color.Gray)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BgGray)

                    InfoRow("树龄", treeAge)
                    InfoRow("地理位置", "经度: $longitude  纬度: $latitude")

                    Spacer(modifier = Modifier.height(8.dp))

                    // 照片区域
                    Text("母树照片", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(BgGray, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUrl.isEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ImageNotSupported, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("暂无照片", color = Color.Gray)
                            }
                        } else {
                            // 实际加载图片逻辑
                            Text("图片加载中...", color = Color.Gray)
                        }
                    }
                }
            }

            // 3. 提示
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
                    text = "母树基本信息修改需要提交审核。DNA条形码由检测中心接口自动更新，不可手动修改。",
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