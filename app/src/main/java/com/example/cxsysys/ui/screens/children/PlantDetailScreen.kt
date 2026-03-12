package com.example.cxsysys.ui.screens.children

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(plantId: String, initialStatus: Int = 0, onBackClick: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("生长态势", "农事记录", "结香采收")

    // --- 模拟数据对齐逻辑 ---
    // 根据传入的 ID 和 status 初始化，确保与列表页一致
    // 列表页数据参考：
    // 苗木-A-001 -> 0 (正常)
    // 苗木-A-002 -> 0 (正常)
    // 苗木-B-088 -> 1 (冻结)

    // 如果是通过路由传参，initialStatus 可能会丢失，这里做个简单的模拟映射兜底
    val currentStatus = remember {
        when {
            plantId.contains("B-088") -> 1 // 冻结
            else -> initialStatus
        }
    }

    // 修改弹窗状态
    var showEditDialog by remember { mutableStateOf(false) }
    var editItemTitle by remember { mutableStateOf("") }

    // 模拟修改弹窗
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("修改 $editItemTitle") },
            text = {
                Column {
                    Text("此处显示原数据并允许修改。", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = "模拟回填数据",
                        onValueChange = {},
                        label = { Text("数值/内容") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("注：超过1个月的记录修改需审核。", color = Color.Red, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "修改申请已提交", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary)
                ) { Text("保存修改") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("取消") }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("档案详情: $plantId", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BgGray
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // 1. 顶部基础信息 (传入状态)
            PlantHeaderCard(plantId, currentStatus) {
                editItemTitle = "定植基本信息"
                showEditDialog = true
            }

            // 2. Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = AgGreenPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AgGreenPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // 3. 内容列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> { // 生长记录
                        item { SectionTitle("全部生长记录") }
                        items(3) { i ->
                            RecordItemCard(
                                title = "生长监测记录 #${3-i}",
                                date = "2023-12-0${i+1}",
                                summary = "树高: ${3.0+i*0.2}m | 地径: ${8+i}cm",
                                details = mapOf(
                                    "记录日期" to "2023-12-0${i+1}",
                                    "树高" to "${3.0+i*0.2} 米",
                                    "地径" to "${8+i} 厘米",
                                    "分枝数" to "${5+i} 枝",
                                    "照片" to "已上传 (点击查看)"
                                ),
                                icon = Icons.Default.Timeline,
                                color = Color(0xFFE3F2FD),
                                onEditClick = { editItemTitle = "生长记录"; showEditDialog = true }
                            )
                        }
                    }
                    1 -> { // 农事记录
                        item { SectionTitle("近期农事操作日志") }

                        // 1. 施肥
                        item {
                            RecordItemCard(
                                title = "施肥作业",
                                date = "2023-12-15",
                                summary = "复合肥 50g/株 | 穴施",
                                details = mapOf(
                                    "施肥时段" to "9-11时",
                                    "肥料名称" to "通用型复合肥",
                                    "单株用量" to "50g",
                                    "施用方法" to "穴施",
                                    "水肥配比" to "无",
                                    "备注" to "雨后施肥"
                                ),
                                icon = Icons.Default.Spa,
                                color = Color(0xFFF1F8E9), // 绿
                                onEditClick = { editItemTitle = "施肥记录"; showEditDialog = true }
                            )
                        }

                        // 2. 病虫害信息
                        item {
                            RecordItemCard(
                                title = "病虫害记录",
                                date = "2023-12-10",
                                summary = "发现卷叶虫 | 轻度",
                                details = mapOf(
                                    "记录日期" to "2023-12-10",
                                    "病虫害描述" to "叶片出现卷曲，发现少量幼虫",
                                    "照片" to "2张 (点击查看)"
                                ),
                                icon = Icons.Default.BugReport,
                                color = Color(0xFFFFEBEE), // 红
                                onEditClick = { editItemTitle = "病虫害信息"; showEditDialog = true }
                            )
                        }

                        // 3. 施药记录
                        item {
                            RecordItemCard(
                                title = "施药作业",
                                date = "2023-12-11",
                                summary = "阿维菌素 | 喷雾",
                                details = mapOf(
                                    "施药日期" to "2023-12-11",
                                    "施药时段" to "15-17时",
                                    "农药名称" to "阿维菌素",
                                    "稀释浓度" to "1500 ppm",
                                    "单株用量" to "200 ml",
                                    "施药方式" to "喷雾",
                                    "防治对象" to "卷叶虫",
                                    "安全间隔" to "7天"
                                ),
                                icon = Icons.Default.Science,
                                color = Color(0xFFFFF3E0), // 橙
                                onEditClick = { editItemTitle = "施药记录"; showEditDialog = true }
                            )
                        }

                        // 4. 灌溉
                        item {
                            RecordItemCard(
                                title = "灌溉记录",
                                date = "2023-12-08",
                                summary = "滴灌 | 9-11时",
                                details = mapOf(
                                    "灌溉日期" to "2023-12-08",
                                    "灌溉时段" to "9-11时",
                                    "灌溉方式" to "滴灌"
                                ),
                                icon = Icons.Default.WaterDrop,
                                color = Color(0xFFE0F7FA), // 蓝
                                onEditClick = { editItemTitle = "灌溉记录"; showEditDialog = true }
                            )
                        }

                        // 5. 剪枝
                        item {
                            RecordItemCard(
                                title = "剪枝修整",
                                date = "2023-11-20",
                                summary = "疏剪 | 5枝",
                                details = mapOf(
                                    "剪枝日期" to "2023-11-20",
                                    "剪枝时段" to "15-17时",
                                    "剪枝类型" to "疏剪",
                                    "剪除分枝数" to "5枝",
                                    "最大剪口" to "1.5 cm",
                                    "工具类型" to "手剪",
                                    "消毒方式" to "酒精",
                                    "备注" to "清理内膛枝"
                                ),
                                icon = Icons.Default.ContentCut,
                                color = Color(0xFFF3E5F5), // 紫
                                onEditClick = { editItemTitle = "剪枝记录"; showEditDialog = true }
                            )
                        }

                        // 补充苗木定植卡片
                        item {
                            RecordItemCard(
                                title = "苗木定植",
                                date = "2020-05-01",
                                summary = "A区-03地块 | 穴深40cm",
                                details = mapOf(
                                    "定植日期" to "2020-05-01",
                                    "幼苗来源" to "幼苗-2023-001 (嫁接)",
                                    "沉香品种" to "金丝油",
                                    "代数" to "2代",
                                    "种植规格" to "穴深40cm x 穴宽40cm",
                                    "种植间距" to "2.5米",
                                    "定植地块" to "A区-03号地"
                                ),
                                icon = Icons.Default.Forest,
                                color = Color(0xFFE8F5E9), // 浅绿
                                onEditClick = { editItemTitle = "定植信息"; showEditDialog = true }
                            )
                        }
                    }
                    2 -> { // 结香采收
                        item { SectionTitle("结香与采收溯源") }
                        item {
                            RecordItemCard(
                                title = "打孔结香",
                                date = "2023-06-01",
                                summary = "4孔 | 孔径5mm",
                                details = mapOf(
                                    "打孔日期" to "2023-06-01",
                                    "打孔时段" to "9-11时",
                                    "平均孔深" to "3.0 cm",
                                    "孔径" to "5.0 mm",
                                    "平均孔距" to "10 cm",
                                    "打孔数量" to "4 个",
                                    "备注" to "使用电钻"
                                ),
                                icon = Icons.Default.Hardware,
                                color = Color(0xFFFFF8E1), // 黄
                                onEditClick = { editItemTitle = "打孔记录"; showEditDialog = true }
                            )
                        }
                        item {
                            RecordItemCard(
                                title = "采收香木",
                                date = "暂无记录",
                                summary = "该苗木尚未进行采收作业",
                                details = emptyMap(),
                                icon = Icons.Default.Inventory,
                                color = Color(0xFFF5F5F5), // 灰
                                showEdit = false,
                                onEditClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

// === 组件 ===

@Composable
fun PlantHeaderCard(plantId: String, status: Int, onEditClick: () -> Unit) {
    // 状态显示逻辑
    val (statusText, statusColor) = when(status) {
        0 -> "正常" to AgGreenPrimary
        1 -> "冻结" to Color(0xFFFFA000)
        else -> "死亡" to Color.Red
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(0.dp, 0.dp, 12.dp, 12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("金丝油 (奇楠)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AgGreenPrimary)
                    Text("二维码: $plantId", color = Color.Gray, fontSize = 12.sp)
                }
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, "Edit Base Info", tint = AgGreenPrimary)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BgGray)
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("树龄", "3.5年", Modifier.weight(1f))
                InfoItem("地块", if(plantId.contains("B")) "B区-01" else "A区-03", Modifier.weight(1f)) // 简单模拟地块

                // [修改] 使用传入的状态显示
                Column(modifier = Modifier.weight(1f)) {
                    Text("状态", fontSize = 12.sp, color = Color.Gray)
                    Text(statusText, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = statusColor)
                }
            }
        }
    }
}

// 可折叠的记录卡片
@Composable
fun RecordItemCard(
    title: String,
    date: String,
    summary: String, // 简略信息
    details: Map<String, String> = emptyMap(), // 详细键值对
    icon: ImageVector,
    color: Color,
    showEdit: Boolean = true,
    onEditClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.clickable { expanded = !expanded } // 点击卡片也能切换
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. 标题行 (始终显示)
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(40.dp).background(color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.Black.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(date, color = Color.Gray, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(summary, fontSize = 13.sp, color = Color.DarkGray)
                }

                // 展开/收起按钮
                if (details.isNotEmpty()) {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(24.dp).padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = Color.Gray
                        )
                    }
                }
            }

            // 2. 详细信息区域 (动画展开)
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(durationMillis = 300)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 300))
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = BgGray, modifier = Modifier.padding(bottom = 8.dp))

                    details.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(key, fontSize = 13.sp, color = Color.Gray)
                            Text(value, fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                        }
                    }

                    // 修改按钮放在详情底部
                    if (showEdit) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedButton(
                                onClick = onEditClick,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("修改记录", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}