package com.example.cxsysys.ui.screens.plantation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 引入提取的公共组件
import com.example.cxsysys.ui.components.TopScanCard
import com.example.cxsysys.ui.components.DualModeIdentifierField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaplingEntryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- 模拟数据 (V10) ---
    val greenhouseOptions = listOf("A区-1号大棚", "A区-2号大棚", "B区-1号大棚 (连栋)")
    val seedbedData = mapOf(
        "A区-1号大棚" to listOf("SB-A1-001 (空闲)", "SB-A1-002 (空闲)", "SB-A1-005 (空闲)"),
        "A区-2号大棚" to listOf("SB-A2-001 (空闲)"),
        "B区-1号大棚 (连栋)" to listOf("SB-B1-010 (空闲)", "SB-B1-011 (空闲)")
    )
    val subspeciesOptions = listOf("0-野生沉香", "1-人工白木香", "2-人工奇楠沉香")
    val generationWayOptions = listOf("嫁接", "扦插", "圈枝", "组培", "其他")

    // --- 表单状态 ---
    var greenhouse_name by remember { mutableStateOf("") }
    var seedbed_code by remember { mutableStateOf("") }

    // 【恢复】：母树只有二维码字段
    var mother_tree_qr by remember { mutableStateOf("") }

    var generation by remember { mutableStateOf("") }
    var subspecies by remember { mutableStateOf("") }
    var generation_way by remember { mutableStateOf("") }

    // 日期处理
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var sapling_date by remember { mutableStateOf(dateFormat.format(Date())) }
    var entry_date by remember { mutableStateOf(dateFormat.format(Date())) }

    var initial_quantity by remember { mutableStateOf("") }

    // UI 状态
    var showSaplingDatePicker by remember { mutableStateOf(false) }
    var showEntryDatePicker by remember { mutableStateOf(false) }
    val saplingDatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val entryDatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    // 【恢复】：扫描状态
    var isScanning by remember { mutableStateOf(false) }

    val currentSeedbedOptions = seedbedData[greenhouse_name] ?: emptyList()

    // 【恢复】：模拟母树扫码功能
    fun simulateMotherTreeScan() {
        scope.launch {
            isScanning = true
            Toast.makeText(context, "正在识别母树二维码...", Toast.LENGTH_SHORT).show()
            delay(1500)
            isScanning = false
            mother_tree_qr = "MT-GEN-2023001"
            Toast.makeText(context, "扫码成功：已关联母树", Toast.LENGTH_SHORT).show()
        }
    }

    // --- 日期选择器逻辑 ---
    if (showSaplingDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showSaplingDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    saplingDatePickerState.selectedDateMillis?.let { millis ->
                        sapling_date = dateFormat.format(Date(millis))
                    }
                    showSaplingDatePicker = false
                }) { Text("确定", color = AgGreenPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showSaplingDatePicker = false }) { Text("取消", color = Color.Gray) }
            }
        ) { DatePicker(state = saplingDatePickerState, showModeToggle = false) }
    }

    if (showEntryDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEntryDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    entryDatePickerState.selectedDateMillis?.let { millis ->
                        entry_date = dateFormat.format(Date(millis))
                    }
                    showEntryDatePicker = false
                }) { Text("确定", color = AgGreenPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showEntryDatePicker = false }) { Text("取消", color = Color.Gray) }
            }
        ) { DatePicker(state = entryDatePickerState, showModeToggle = false) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("幼苗培育录入", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        if (seedbed_code.isNotEmpty() && initial_quantity.isNotEmpty()) {
                            Toast.makeText(context, "幼苗信息已保存！\n关联苗床: $seedbed_code", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        } else {
                            Toast.makeText(context, "请补全苗床和数量信息", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存信息", fontSize = 16.sp)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgGray)
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 【恢复】：顶部扫码卡片
            TopScanCard(
                isScanning = isScanning,
                title = "点击扫描母树二维码(选填)",
                subtitle = "快速关联繁育母树档案 ",
                onScanClick = { simulateMotherTreeScan() }
            )

            // 1. 位置信息卡片 (苗床关联)
            Text("位置信息", fontWeight = FontWeight.Bold, color = Color.Gray)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SaplingDropdownField(
                        label = "选择种植大棚",
                        value = greenhouse_name,
                        placeholder = "请选择大棚",
                        options = greenhouseOptions,
                        onValueChange = {
                            greenhouse_name = it
                            seedbed_code = ""
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SaplingDropdownField(
                        label = "选择空闲苗床",
                        value = seedbed_code,
                        placeholder = if (greenhouse_name.isEmpty()) "请先选择大棚" else "请选择空闲苗床",
                        options = currentSeedbedOptions,
                        onValueChange = { seedbed_code = it },
                        enabled = greenhouse_name.isNotEmpty()
                    )
                }
            }

            // 2. 幼苗基本属性
            Text("幼苗属性", fontWeight = FontWeight.Bold, color = Color.Gray)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // 【修改】：使用复用组件，但锁死为二维码模式，禁止切换
                    DualModeIdentifierField(
                        targetName = "母树",
                        qrCodeValue = mother_tree_qr,
                        onQrCodeChange = { mother_tree_qr = it },
                        selfCodeValue = "",
                        onSelfCodeChange = { },
                        isSelfCodeMode = false, // 永远为 false，保持扫码模式
                        onModeChange = { },     // 不响应切换
                        onScanClick = { simulateMotherTreeScan() },
                        showModeToggle = false  // 【关键】：隐藏右上角的切换按钮
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SaplingDropdownField(
                        label = "沉香品种细分",
                        value = subspecies,
                        placeholder = "请选择品种",
                        options = subspeciesOptions,
                        onValueChange = { subspecies = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = generation,
                            onValueChange = { if (it.length <= 2) generation = it },
                            label = { Text("代数", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) },
                            placeholder = { Text("如: 1", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            SaplingDropdownField(
                                label = "育苗方法",
                                value = generation_way,
                                placeholder = "选择方法",
                                options = generationWayOptions,
                                onValueChange = { generation_way = it }
                            )
                        }
                    }
                }
            }

            // 3. 时间与数量
            Text("时间与数量", fontWeight = FontWeight.Bold, color = Color.Gray)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = sapling_date,
                            onValueChange = {},
                            label = { Text("嫁接/扦插/播种日期") },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = AgGreenPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showSaplingDatePicker = true })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = entry_date,
                            onValueChange = {},
                            label = { Text("入棚日期") },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = AgGreenPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showEntryDatePicker = true })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = initial_quantity,
                        onValueChange = { if (it.all { char -> char.isDigit() }) initial_quantity = it },
                        label = { Text("本苗床幼苗初始数量") },
                        placeholder = { Text("请输入数量") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// === 内部组件 ===

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaplingDropdownField(
    label: String,
    value: String,
    placeholder: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) },
            placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) },
            readOnly = true,
            singleLine = true,
            enabled = enabled,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgGreenPrimary,
                focusedLabelColor = AgGreenPrimary,
                disabledContainerColor = BgGray.copy(alpha = 0.5f),
                disabledBorderColor = Color.LightGray
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}