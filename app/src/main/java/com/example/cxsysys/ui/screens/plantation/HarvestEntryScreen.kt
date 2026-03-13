package com.example.cxsysys.ui.screens.plantation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// 引入刚刚提取的顶部大卡片公共组件
import com.example.cxsysys.ui.components.TopScanCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestEntryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- 表单状态 ---
    // 录入模式：0-个别录入(枝干/苗木), 1-批量录入(地块)。默认为1
    var inputMode by remember { mutableIntStateOf(1) }

    // 个别录入类型：苗木 或 打孔枝干 (明确区分，防混淆)
    var individualTargetType by remember { mutableStateOf("苗木") }
    val individualTargetOptions = listOf("苗木", "打孔枝干")

    // 将原 target_id 拆分为明确的两个字段
    var plant_id by remember { mutableStateOf("") }  // 苗木ID
    var branch_id by remember { mutableStateOf("") } // 枝干ID
    var field_id by remember { mutableStateOf("") }  // 地块ID (批量模式)

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var harvest_date by remember { mutableStateOf(dateFormat.format(Date())) }

    // 采收信息
    var weight by remember { mutableStateOf("") } // 采收重量

    var remark by remember { mutableStateOf("") }

    // UI 控制状态
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var isScanning by remember { mutableStateOf(false) }

    // 模拟扫码逻辑
    fun simulateScan() {
        scope.launch {
            isScanning = true
            // 根据不同模式和类型提示
            val msg = when {
                inputMode == 1 -> "正在识别地块二维码..."
                individualTargetType == "苗木" -> "正在识别苗木二维码..."
                else -> "正在识别打孔枝干二维码..."
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            delay(1500)
            isScanning = false

            // 数据回填
            when {
                inputMode == 1 -> field_id = "HARVEST-FIELD-A01"
                individualTargetType == "苗木" -> plant_id = "HARVEST-TREE-001"
                else -> branch_id = "HARVEST-BRANCH-B02"
            }
            Toast.makeText(context, "扫码成功", Toast.LENGTH_SHORT).show()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        harvest_date = dateFormat.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("确定", color = AgGreenPrimary) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消", color = Color.Gray) } }
        ) { DatePicker(state = datePickerState, showModeToggle = false) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("采收香木录入", fontWeight = FontWeight.Bold) },
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
                        // 按照具体的字段进行校验，避免混为一谈
                        val isTargetValid = when {
                            inputMode == 1 -> field_id.isNotEmpty()
                            individualTargetType == "苗木" -> plant_id.isNotEmpty()
                            else -> branch_id.isNotEmpty()
                        }

                        if (!isTargetValid) {
                            val msg = when {
                                inputMode == 1 -> "请扫码或输入地块自编码"
                                individualTargetType == "苗木" -> "请扫码或输入苗木二维码"
                                else -> "请扫码或输入打孔枝干二维码"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        } else if (weight.isEmpty()) {
                            Toast.makeText(context, "请输入采收重量", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "保存成功！", Toast.LENGTH_SHORT).show()
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
            // 1. 模式切换器 (个别/批量)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 个别录入按钮
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (inputMode == 0) AgGreenPrimary else Color.Transparent)
                        .clickable { inputMode = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "个别录入 (枝干/苗木)",
                        color = if (inputMode == 0) Color.White else Color.Gray,
                        fontWeight = if (inputMode == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
                // 批量录入按钮
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (inputMode == 1) AgGreenPrimary else Color.Transparent)
                        .clickable { inputMode = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "批量录入 (地块)",
                        color = if (inputMode == 1) Color.White else Color.Gray,
                        fontWeight = if (inputMode == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }

            // 2. 顶部扫码区 (复用公共组件 TopScanCard)
            val scanTitle = when {
                inputMode == 1 -> "点击扫描地块二维码"
                individualTargetType == "苗木" -> "点击扫描苗木二维码"
                else -> "点击扫描打孔枝干二维码"
            }
            val scanSubtitle = when {
                inputMode == 1 -> "批量录入关联地块采收记录"
                individualTargetType == "苗木" -> "录入整株苗木采收记录"
                else -> "录入单个打孔枝干采收记录"
            }

            TopScanCard(
                isScanning = isScanning,
                title = scanTitle,
                subtitle = scanSubtitle,
                onScanClick = { simulateScan() }
            )

            Text("作业基本信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // 根据模式动态显示不同输入结构
                    if (inputMode == 0) {
                        // 个别模式下：先选类型，再输入对应二维码
                        HarvestSelectDropdown(
                            label = "采收对象类型",
                            selectedValue = individualTargetType,
                            options = individualTargetOptions,
                            onValueChange = { individualTargetType = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (individualTargetType == "苗木") {
                            HarvestInputWithScanField(
                                label = "苗木二维码",
                                value = plant_id,
                                onValueChange = { plant_id = it },
                                onScanClick = { simulateScan() },
                                placeholder = "手动输入苗木二维码"
                            )
                        } else {
                            HarvestInputWithScanField(
                                label = "打孔枝干二维码",
                                value = branch_id,
                                onValueChange = { branch_id = it },
                                onScanClick = { simulateScan() },
                                placeholder = "手动输入打孔枝干二维码"
                            )
                        }
                    } else {
                        // 批量模式
                        HarvestInputWithScanField(
                            label = "定植地块",
                            value = field_id,
                            onValueChange = { field_id = it },
                            onScanClick = { simulateScan() },
                            placeholder = "手动输入地块自编码"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 采收日期
                    OutlinedTextField(
                        value = harvest_date,
                        onValueChange = { harvest_date = it },
                        label = { Text("采收日期") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, "选择日期", tint = AgGreenPrimary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )
                }
            }

            Text("香木信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 采收重量 (去除了等级选择)
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) weight = it },
                        label = { Text("采收重量") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("g/克", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )
                }
            }

            Text("补充说明", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        label = { Text("备注 (选填)") },
                        placeholder = { Text("采收方法、香木形态等描述", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )
                }
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

// =================================================================
// ⬇️ 内部组件 (前缀 Harvest)
// =================================================================

// 【删除处】原有的 HarvestScanSection 已经被删除，转为调用引入的公共组件 TopScanCard

@Composable
private fun HarvestInputWithScanField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onScanClick: () -> Unit,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = onScanClick) {
                Icon(Icons.Default.DocumentScanner, contentDescription = "Scan", tint = AgGreenPrimary)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HarvestSelectDropdown(label: String, selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedValue, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary),
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onValueChange(option); expanded = false }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding)
            }
        }
    }
}