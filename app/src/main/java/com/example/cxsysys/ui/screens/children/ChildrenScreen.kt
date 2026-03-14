package com.example.cxsysys.ui.screens.children

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
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

// --- 苗床数据实体模型 ---
data class SeedbedListItem(
    val id: String,
    val seedbedCode: String,      // (1) 苗床自编码
    val greenhouseCode: String,   // (2) 大棚自编码
    val currentQuantity: Int,     // (3) 当前数量
    val species: String,          // (4) 品种
    val generation: String,       // (5) 代数
    val method: String,           // (6) 育苗方式
    val saplingDate: String,      // (6) 育苗日期
    val status: Int               // (7) 状态：0空闲 1有苗占用 2停用
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildrenScreen(
    onNavigateToDetail: (String) -> Unit
) {
    val context = LocalContext.current

    // --- 状态管理 ---
    var searchText by remember { mutableStateOf("") } // 搜索框输入

    // 模拟：种植园与大棚的级联筛选数据
    val plantations = listOf("全部种植园", "PL-001 (一号园)", "PL-002 (二号园)")
    val greenhousesMap = mapOf(
        "全部种植园" to listOf("全部大棚"),
        "PL-001 (一号园)" to listOf("全部大棚", "GH-A01", "GH-A02"),
        "PL-002 (二号园)" to listOf("全部大棚", "GH-B01")
    )

    var selectedPlantation by remember { mutableStateOf(plantations[0]) }
    var selectedGreenhouse by remember { mutableStateOf("全部大棚") }

    var plantationExpanded by remember { mutableStateOf(false) }
    var greenhouseExpanded by remember { mutableStateOf(false) }

    // --- 模拟：苗床列表数据 ---
    val allSeedbeds = remember {
        listOf(
            SeedbedListItem("1", "SB-001", "GH-A01", 4850, "白木香", "1代", "扦插", "2026-01-15", 1),
            SeedbedListItem("2", "SB-002", "GH-A01", 5000, "奇楠", "2代", "嫁接", "2026-02-10", 1),
            SeedbedListItem("3", "SB-003", "GH-A02", 0, "-", "-", "-", "-", 0),
            SeedbedListItem("4", "SB-004", "GH-B01", 2100, "白木香", "1代", "播种", "2025-11-20", 1),
            SeedbedListItem("5", "SB-005", "GH-B01", 0, "-", "-", "-", "-", 2)
        )
    }

    // --- 过滤逻辑 (整合了搜索框和大棚筛选) ---
    val filteredSeedbeds = allSeedbeds.filter { seedbed ->
        val matchesGreenhouse = (selectedGreenhouse == "全部大棚" || seedbed.greenhouseCode == selectedGreenhouse)
        val matchesSearch = (searchText.isEmpty() || seedbed.seedbedCode.contains(searchText, ignoreCase = true))
        matchesGreenhouse && matchesSearch
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                // 1. 标题栏 (对齐 PlantingScreen)
                CenterAlignedTopAppBar(
                    title = { Text("幼苗档案管理", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )

                // 2. 顶部检索框 (对齐 PlantingScreen)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(50.dp)
                        .background(BgGray, RoundedCornerShape(25.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchText.isEmpty()) {
                            Text("扫描或输入苗床自编码...", color = Color.Gray)
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "启动扫码...", Toast.LENGTH_SHORT).show()
                        searchText = "SB-001" // 模拟扫码结果
                    }) {
                        Icon(Icons.Default.QrCodeScanner, null, tint = AgGreenPrimary)
                    }
                }

                // 3. 种植园/大棚级联筛选
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 种植园选择
                    ExposedDropdownMenuBox(
                        expanded = plantationExpanded,
                        onExpandedChange = { plantationExpanded = !plantationExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedPlantation,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("所属种植园", fontSize = 12.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = plantationExpanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AgGreenPrimary,
                                focusedLabelColor = AgGreenPrimary
                            ),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = plantationExpanded,
                            onDismissRequest = { plantationExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            plantations.forEach { pt ->
                                DropdownMenuItem(
                                    text = { Text(pt) },
                                    onClick = {
                                        selectedPlantation = pt
                                        selectedGreenhouse = "全部大棚" // 切换种植园时重置大棚
                                        plantationExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // 大棚选择
                    ExposedDropdownMenuBox(
                        expanded = greenhouseExpanded,
                        onExpandedChange = { greenhouseExpanded = !greenhouseExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedGreenhouse,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("所属大棚", fontSize = 12.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = greenhouseExpanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AgGreenPrimary,
                                focusedLabelColor = AgGreenPrimary
                            ),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = greenhouseExpanded,
                            onDismissRequest = { greenhouseExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            greenhousesMap[selectedPlantation]?.forEach { gh ->
                                DropdownMenuItem(
                                    text = { Text(gh) },
                                    onClick = {
                                        selectedGreenhouse = gh
                                        greenhouseExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = BgGray, thickness = 1.dp)
            }
        }
    ) { paddingValues ->
        // --- 苗床列表区 ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)) // 背景灰，凸显白色卡片
                .padding(paddingValues)
        ) {
            if (filteredSeedbeds.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("未找到符合条件的苗床", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSeedbeds) { seedbed ->
                        SeedbedCard(item = seedbed, onClick = { onNavigateToDetail(seedbed.id) })
                    }
                }
            }
        }
    }
}

// === 苗床卡片组件 (保留原设计，修改了停用颜色) ===

@Composable
fun SeedbedCard(item: SeedbedListItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 第一行：苗床编码 (左上) + 状态 (右上)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.seedbedCode, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    Spacer(modifier = Modifier.width(8.dp))
                    // 大棚标签
                    Surface(color = BgGray, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = item.greenhouseCode,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // [修改] 状态标签设定：2停用/废弃 改为了 Color.Red
                val (statusText, statusColor) = when (item.status) {
                    0 -> "空闲" to Color(0xFF4CAF50)      // 绿色
                    1 -> "有苗占用" to Color(0xFF2196F3) // 蓝色
                    2 -> "停用/废弃" to Color.Red         // 红色
                    else -> "未知" to Color.Gray
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BgGray)
            Spacer(modifier = Modifier.height(12.dp))

            // 第二行：品种、代数、方式
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoColumn("品种", item.species, Modifier.weight(1f))
                InfoColumn("代数", item.generation, Modifier.weight(1f))
                InfoColumn("育苗", item.method, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 第三行：育苗日期 (左下) + 当前数量 (右下)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = if(item.status == 1) "育苗日期：${item.saplingDate}" else "暂无育苗批次",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                if (item.status == 1) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("当前存量: ", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = "${item.currentQuantity}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgGreenPrimary
                        )
                        Text(" 株", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 15.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium)
    }
}