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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cxsysys.model.PlantDetail
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import com.example.cxsysys.viewmodel.PlantingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: String, 
    onBackClick: () -> Unit,
    viewModel: PlantingViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("生长态势", "农事记录", "结香采收")

    val plantDetail by viewModel.plantDetail.collectAsState()
    val farmingList by viewModel.farmingList.collectAsState()
    val growthList by viewModel.growthList.collectAsState()
    val punchList by viewModel.punchList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(plantId) {
        viewModel.fetchPlantDetailAll(plantId)
    }

    // 修改弹窗状态
    var showEditDialog by remember { mutableStateOf(false) }
    var editItemTitle by remember { mutableStateOf("") }

    // 模拟修改弹窗 (接口待定)
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("修改 $editItemTitle") },
            text = {
                Column {
                    Text("接口待定，当前无法保存修改。", fontSize = 14.sp, color = Color.Gray)
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
                        Toast.makeText(context, "批量及修改接口暂未完成", Toast.LENGTH_SHORT).show()
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
                title = { Text("档案详情", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BgGray
    ) { padding ->
        if (isLoading && plantDetail == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgGreenPrimary)
            }
            return@Scaffold
        }

        Column(modifier = Modifier.padding(padding)) {
            // 1. 顶部基础信息
            plantDetail?.let { detail ->
                PlantHeaderCard(detail) {
                    editItemTitle = "定植基本信息"
                    showEditDialog = true
                }
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
                        if (growthList.isEmpty()) {
                            item { Text("暂无记录", color = Color.Gray, modifier = Modifier.padding(top = 16.dp)) }
                        } else {
                            items(growthList.size) { i ->
                                val record = growthList[i]
                                RecordItemCard(
                                    title = "生长监测记录 #${record.recordId}",
                                    date = record.recordDate,
                                    summary = "树高: ${record.treeHeight}m | 地径: ${record.groundDiameter}cm",
                                    details = mapOf(
                                        "记录日期" to record.recordDate,
                                        "树高" to "${record.treeHeight} 米",
                                        "地径" to "${record.groundDiameter} 厘米"
                                    ),
                                    icon = Icons.Default.Timeline,
                                    color = Color(0xFFE3F2FD),
                                    onEditClick = { editItemTitle = "生长记录"; showEditDialog = true }
                                )
                            }
                        }
                    }
                    1 -> { // 农事记录
                        item { SectionTitle("近期农事操作日志") }
                        
                        if (farmingList.isEmpty()) {
                            item { Text("暂无记录", color = Color.Gray, modifier = Modifier.padding(top = 16.dp)) }
                        } else {
                            // 暂时只展示返回的第一组对象的各农事记录
                            val fRecord = farmingList.first()
                            
                            // 1. 施肥
                            fRecord.fert?.let { fert ->
                                if (fert.hasRecord) {
                                    item {
                                        RecordItemCard(
                                            title = "施肥作业",
                                            date = fert.date ?: "未知",
                                            summary = "${fert.name ?: "未知肥料"} | ${fert.method ?: "未知方式"}",
                                            details = mapOf(
                                                "肥料名称" to (fert.name ?: ""),
                                                "单株用量" to (fert.dosage ?: ""),
                                                "施用方法" to (fert.method ?: "")
                                            ),
                                            icon = Icons.Default.Spa,
                                            color = Color(0xFFF1F8E9),
                                            onEditClick = { editItemTitle = "施肥记录"; showEditDialog = true }
                                        )
                                    }
                                }
                            }
                            
                            // 2. 病虫害
                            fRecord.disease?.let { disease ->
                                if (disease.hasRecord) {
                                    item {
                                        RecordItemCard(
                                            title = "病虫害记录",
                                            date = disease.date ?: "未知",
                                            summary = "${disease.type ?: "未知病害"} | 有记录",
                                            details = mapOf(
                                                "病虫害描述" to (disease.description ?: "")
                                            ),
                                            icon = Icons.Default.BugReport,
                                            color = Color(0xFFFFEBEE),
                                            onEditClick = { editItemTitle = "病虫害信息"; showEditDialog = true }
                                        )
                                    }
                                }
                            }
                            
                            // 3. 施药
                            fRecord.pest?.let { pest ->
                                if (pest.hasRecord) {
                                    item {
                                        RecordItemCard(
                                            title = "施药作业",
                                            date = pest.date ?: "未知",
                                            summary = "${pest.name ?: "未知农药"} | ${pest.method ?: "未知方式"}",
                                            details = mapOf(
                                                "农药名称" to (pest.name ?: ""),
                                                "施药方式" to (pest.method ?: "")
                                            ),
                                            icon = Icons.Default.Science,
                                            color = Color(0xFFFFF3E0),
                                            onEditClick = { editItemTitle = "施药记录"; showEditDialog = true }
                                        )
                                    }
                                }
                            }
                            
                            // 4. 灌溉
                            fRecord.irri?.let { irri ->
                                if (irri.hasRecord) {
                                    item {
                                        RecordItemCard(
                                            title = "灌溉记录",
                                            date = irri.date ?: "未知",
                                            summary = "${irri.method ?: "未知方式"} | ${irri.period ?: "未知时段"}",
                                            details = mapOf(
                                                "灌溉方式" to (irri.method ?: ""),
                                                "时段" to (irri.period ?: "")
                                            ),
                                            icon = Icons.Default.WaterDrop,
                                            color = Color(0xFFE0F7FA),
                                            onEditClick = { editItemTitle = "灌溉记录"; showEditDialog = true }
                                        )
                                    }
                                }
                            }
                            
                            // 5. 剪枝
                            fRecord.prun?.let { prun ->
                                if (prun.hasRecord) {
                                    item {
                                        RecordItemCard(
                                            title = "剪枝修整",
                                            date = prun.date ?: "未知",
                                            summary = "${prun.type ?: "未知类型"} | 有记录",
                                            details = mapOf(
                                                "剪枝类型" to (prun.type ?: "")
                                            ),
                                            icon = Icons.Default.ContentCut,
                                            color = Color(0xFFF3E5F5),
                                            onEditClick = { editItemTitle = "剪枝记录"; showEditDialog = true }
                                        )
                                    }
                                }
                            }
                            
                            // 6. 定植
                            fRecord.plant?.let { plant ->
                                if (plant.hasRecord) {
                                    item {
                                        RecordItemCard(
                                            title = "苗木定植",
                                            date = plant.date ?: "未知",
                                            summary = "${plant.fieldCode ?: "未知地块"} | 深度 ${plant.depth ?: "未知"}",
                                            details = mapOf(
                                                "深度" to (plant.depth ?: ""),
                                                "关联地块" to (plant.fieldCode ?: "")
                                            ),
                                            icon = Icons.Default.Forest,
                                            color = Color(0xFFE8F5E9),
                                            onEditClick = { editItemTitle = "定植信息"; showEditDialog = true }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    2 -> { // 结香采收
                        item { SectionTitle("结香与采收溯源") }
                        if (punchList.isEmpty()) {
                            item { Text("暂无记录", color = Color.Gray, modifier = Modifier.padding(top = 16.dp)) }
                        } else {
                            val record = punchList.first()
                            if (record.punch.hasRecord) {
                                item {
                                    RecordItemCard(
                                        title = "打孔结香",
                                        date = "有记录",
                                        summary = "已打孔",
                                        details = mapOf("打孔状态" to "已完成"),
                                        icon = Icons.Default.Hardware,
                                        color = Color(0xFFFFF8E1),
                                        onEditClick = { editItemTitle = "打孔记录"; showEditDialog = true }
                                    )
                                }
                            }
                            
                            if (record.harvest.hasRecord) {
                                item {
                                    RecordItemCard(
                                        title = "采收香木",
                                        date = record.harvest.date,
                                        summary = "重量: ${record.harvest.weight}",
                                        details = mapOf("采香重量" to "${record.harvest.weight}"),
                                        icon = Icons.Default.Inventory,
                                        color = Color(0xFFE0F7FA),
                                        onEditClick = { editItemTitle = "采收记录"; showEditDialog = true }
                                    )
                                }
                            } else {
                                item {
                                    RecordItemCard(
                                        title = "采收香木",
                                        date = "暂无记录",
                                        summary = "该苗木尚未进行采收作业",
                                        details = emptyMap(),
                                        icon = Icons.Default.Inventory,
                                        color = Color(0xFFF5F5F5),
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
    }
}

// === 组件 ===

@Composable
fun PlantHeaderCard(detail: PlantDetail, onEditClick: () -> Unit) {
    // 状态显示逻辑
    val (statusText, statusColor) = when(detail.status) {
        "0" -> "正常" to AgGreenPrimary
        "1" -> "冻结" to Color(0xFFFFA000)
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
                    Text(detail.subspeciesName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AgGreenPrimary)
                    Text("二维码: ${detail.plantQrcode}", color = Color.Gray, fontSize = 12.sp)
                }
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, "Edit Base Info", tint = AgGreenPrimary)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BgGray)
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("树龄", detail.treeAge, Modifier.weight(1f))
                InfoItem("地块", detail.fieldCode, Modifier.weight(1f)) 

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