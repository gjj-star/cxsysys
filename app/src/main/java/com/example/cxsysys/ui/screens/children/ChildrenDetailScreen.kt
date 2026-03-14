package com.example.cxsysys.ui.screens.children

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildrenDetailScreen(seedbedId: String, onBackClick: () -> Unit) {

    // --- 模拟：联表查询结果 (seedbed + sapling_seedbed) ---
    // 在真实开发中，这里会由 ViewModel 根据传入的 seedbedId 向后端请求数据
    val seedbedCode = "SB-001"
    val greenhouseCode = "GH-A01"
    val status = 1 // 0空闲 1有苗占用 2停用
    val length = "10.0"
    val width = "1.5"

    // 仅当 status == 1 时才有意义的当前批次数据
    val speciesName = "白木香"
    val generation = "1"
    val generationWay = "扦插"
    val initialQty = 5000
    val currentQty = 4850
    val motherTreeQr = "MT-778899"
    val saplingDate = "2026-01-15"
    val entryDate = "2026-02-01"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("苗床详情", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // 底部操作栏：只有在“有苗占用”时才能记录损耗或批量出棚
            if (status == 1) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* TODO: 弹窗记录死亡损耗 */ },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("登记损耗")
                        }
                        Button(
                            onClick = { /* TODO: 批量出棚转移/定植 */ },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary)
                        ) {
                            Text("出棚/定植")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgGray)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 区块 1：苗床硬件档案 ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("基本信息", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if(status==1) "有苗占用" else if(status==0) "空闲" else "停用",
                            color = if(status==1) Color(0xFF2196F3) else Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BgGray)

                    InfoRow("苗床编码", seedbedCode)
                    InfoRow("所属大棚", greenhouseCode)
                    InfoRow("规格尺寸", "长 ${length}m × 宽 ${width}m")
                }
            }

            // --- 区块 2：当前幼苗批次信息 (有苗时显示) ---
            if (status == 1) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("当前批次信息", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BgGray)

                        // 重点数据突出展示
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("初始入棚(株)", fontSize = 12.sp, color = Color.Gray)
                                Text("$initialQty", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("当前存量(株)", fontSize = 12.sp, color = Color.Gray)
                                Text("$currentQty", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AgGreenPrimary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("耗损率", fontSize = 12.sp, color = Color.Gray)
                                val lossRate = ((initialQty - currentQty).toFloat() / initialQty * 100)
                                Text(String.format("%.1f%%", lossRate), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                        }
                        HorizontalDivider(color = BgGray)

                        InfoRow("品种细分", speciesName)
                        InfoRow("育苗方式", generationWay)
                        InfoRow("代数", "$generation 代")
                        InfoRow("母树来源", motherTreeQr)
                        InfoRow("育苗日期", saplingDate)
                        InfoRow("入棚日期", entryDate)
                    }
                }

                // 提示区
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
                        text = "幼苗死亡损耗请及时点击下方“登记损耗”更新数量。待幼苗长成定植移出大棚时，系统会自动记录出棚日期。",
                        color = Color(0xFF0D47A1),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            } else if (status == 0) {
                // 空闲状态提示
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("当前苗床空闲，等待新批次入棚", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }
}

// 辅助组件：渲染详情行
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 15.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium)
    }
}