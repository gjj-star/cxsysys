package com.example.cxsysys.ui.screens.children

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// [修改] 筛选类型：移除批次，改为二维码/大棚/日期
enum class SaplingFilterType(val label: String) {
    QRCode("二维码"),
    Greenhouse("大棚"),
    Date("日期")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildrenScreen(onNavigateToDetail: (String) -> Unit) {
    val context = LocalContext.current

    // 筛选状态
    var selectedFilterType by remember { mutableStateOf(SaplingFilterType.QRCode) }
    var searchText by remember { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }

    // 日期选择状态
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // 日期选择器
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        searchText = dateFormat.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("确定") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                CenterAlignedTopAppBar(
                    title = { Text("幼苗档案管理", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                    actions = {
                        IconButton(onClick = { Toast.makeText(context, "导出育苗清单", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Print, contentDescription = "Export")
                        }
                    }
                )

                // --- 搜索筛选栏 ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(56.dp)
                        .background(BgGray, RoundedCornerShape(28.dp))
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. 筛选类型选择器
                    Box {
                        Row(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { showFilterMenu = true }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedFilterType.label, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                        }

                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            SaplingFilterType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.label) },
                                    onClick = {
                                        selectedFilterType = type
                                        searchText = ""
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 2. 动态输入区域
                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedFilterType) {
                            SaplingFilterType.QRCode -> {
                                OutlinedTextField(
                                    value = searchText,
                                    onValueChange = { searchText = it },
                                    placeholder = { Text("扫描或输入幼苗码") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    singleLine = true
                                )
                            }
                            SaplingFilterType.Greenhouse -> {
                                Text(
                                    text = if(searchText.isEmpty()) "请选择大棚" else searchText,
                                    color = if(searchText.isEmpty()) Color.Gray else Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp)
                                        .clickable {
                                            searchText = "1号智能温室"
                                            Toast.makeText(context, "模拟选择：1号智能温室", Toast.LENGTH_SHORT).show()
                                        }
                                )
                            }
                            SaplingFilterType.Date -> {
                                Text(
                                    text = if(searchText.isEmpty()) "请选择育苗日期" else searchText,
                                    color = if(searchText.isEmpty()) Color.Gray else Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp)
                                        .clickable { showDatePicker = true }
                                )
                            }
                        }
                    }

                    // 3. 操作按钮
                    IconButton(
                        onClick = {
                            if (selectedFilterType == SaplingFilterType.QRCode) {
                                Toast.makeText(context, "启动扫码...", Toast.LENGTH_SHORT).show()
                                searchText = "幼苗-2023-001"
                            } else if (selectedFilterType == SaplingFilterType.Date) {
                                showDatePicker = true
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        val icon = when(selectedFilterType) {
                            SaplingFilterType.QRCode -> Icons.Default.QrCodeScanner
                            SaplingFilterType.Date -> Icons.Default.CalendarToday
                            else -> Icons.Default.Search
                        }
                        Icon(icon, null, tint = AgGreenPrimary)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgGray)
                .padding(padding)
        ) {
            SaplingList(onItemClick = onNavigateToDetail)
        }
    }
}

// === 幼苗列表 ===
@Composable
fun SaplingList(onItemClick: (String) -> Unit) {
    // [修改] 模拟单株幼苗数据 (无批次，无数量，有状态)
    val saplings = listOf(
        SaplingItem("幼苗-2023-001-001", "金丝油", "嫁接", "1号智能温室", "2023-10-01", 0),
        SaplingItem("幼苗-2023-001-002", "金丝油", "嫁接", "1号智能温室", "2023-10-01", 0),
        SaplingItem("幼苗-2023-005-088", "奇楠1号", "扦插", "2号大棚", "2023-11-15", 1), // 1=冻结/售出
        SaplingItem("幼苗-2024-002-012", "虎斑", "播种", "1号智能温室", "2024-01-20", 2) // 2=死亡
    )

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(saplings.size) { index ->
            SaplingCard(saplings[index], onClick = { onItemClick(saplings[index].qrCode) })
        }
    }
}

// [修改] 数据类：移除 count, 添加 status
data class SaplingItem(
    val qrCode: String,
    val species: String,
    val method: String,
    val location: String,
    val date: String,
    val status: Int // 0正常, 1冻结, 2注销
)

@Composable
fun SaplingCard(item: SaplingItem, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                // 显示单株二维码
                Text(item.qrCode, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                // [新增] 状态标签
                val (statusText, statusColor) = when(item.status) {
                    0 -> "正常" to AgGreenPrimary
                    1 -> "售出" to Color(0xFFFFA000)
                    else -> "死亡" to Color.Red
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BgGray)
            Spacer(modifier = Modifier.height(8.dp))

            // [修改] 移除数量展示，保留其他信息
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoColumn("品种", item.species, Modifier.weight(1f))
                InfoColumn("方式", item.method, Modifier.weight(1f))
                InfoColumn("大棚", item.location, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("育苗日期：${item.date}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}