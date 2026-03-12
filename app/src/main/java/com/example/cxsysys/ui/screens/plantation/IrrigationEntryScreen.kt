package com.example.cxsysys.ui.screens.plantation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IrrigationEntryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- 表单状态 ---
    // 录入模式：0-个别录入(苗木), 1-批量记录。默认为1
    var inputMode by remember { mutableIntStateOf(1) }

    // 个别录入字段
    var plant_id by remember { mutableStateOf("") }

    // 批量录入层级字段 (种植园 -> 大棚/地块 -> 苗床)
    var plantation_name by remember { mutableStateOf("") }
    val plantationOptions = listOf("茂名核心种植园", "电白试验园", "化州生态园")

    var region_type by remember { mutableStateOf("") }
    val regionTypeOptions = listOf("地块", "大棚")

    var region_id by remember { mutableStateOf("") }  // 地块或大棚的编号
    var seedbed_id by remember { mutableStateOf("") } // 苗床编号 (仅大棚有)

    // 灌溉基本信息
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var irrigation_date by remember { mutableStateOf(dateFormat.format(Date())) }

    // [新增] 灌溉时段 (time_slot)
    var time_slot by remember { mutableStateOf("9-11时") }
    val timeSlotOptions = listOf("6-8时", "9-11时", "12-14时", "15-17时", "18-20时")

    var irrigation_method by remember { mutableStateOf("滴灌") }
    val methodOptions = listOf("滴灌", "喷灌", "浇灌", "漫灌", "水肥一体化", "其他")

    // [删除] var water_amount by remember { mutableStateOf("") } (已删去灌溉量)
    var remark by remember { mutableStateOf("") }

    // UI 控制状态
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var isScanning by remember { mutableStateOf(false) }

    // 模拟扫码逻辑
    fun simulateScan(target: String = "general") {
        scope.launch {
            isScanning = true
            val msg = when (target) {
                "plant" -> "正在识别苗木二维码..."
                "region" -> "正在识别区域二维码..."
                "seedbed" -> "正在识别苗床二维码..."
                else -> if (inputMode == 0) "正在识别苗木二维码..." else "正在识别区域二维码..."
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            delay(1500)
            isScanning = false

            // 回填逻辑
            if (inputMode == 0) {
                plant_id = "TREE-IRR-001"
            } else {
                if (target == "seedbed") {
                    seedbed_id = "BED-012"
                } else {
                    // 批量模式扫主码，自动填满所有层级演示级联效果
                    plantation_name = "茂名核心种植园"
                    region_type = "大棚"
                    region_id = "GH-A-01"
                    seedbed_id = "BED-012"
                }
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
                        irrigation_date = dateFormat.format(Date(millis))
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
                title = { Text("灌溉记录录入", fontWeight = FontWeight.Bold) },
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
                        // 简单校验
                        if (inputMode == 0 && plant_id.isEmpty()) {
                            Toast.makeText(context, "请扫码或输入苗木二维码", Toast.LENGTH_SHORT).show()
                        } else if (inputMode == 1 && (plantation_name.isEmpty() || region_type.isEmpty() || region_id.isEmpty())) {
                            Toast.makeText(context, "请完整选择灌溉区域信息", Toast.LENGTH_SHORT).show()
                        } else {
                            // [删除] 对 water_amount 的非空校验
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
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().background(if (inputMode == 0) AgGreenPrimary else Color.Transparent).clickable { inputMode = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Text("个别录入 (苗木)", color = if (inputMode == 0) Color.White else Color.Gray, fontWeight = if (inputMode == 0) FontWeight.Bold else FontWeight.Normal)
                }
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().background(if (inputMode == 1) AgGreenPrimary else Color.Transparent).clickable { inputMode = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Text("批量记录", color = if (inputMode == 1) Color.White else Color.Gray, fontWeight = if (inputMode == 1) FontWeight.Bold else FontWeight.Normal)
                }
            }

            // 2. 顶部扫码区
            IrrigationScanSection(
                isScanning = isScanning,
                inputMode = inputMode,
                onScanClick = { simulateScan() }
            )

            Text("作业基本信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 层级联动的关联对象输入
                    if (inputMode == 0) {
                        IrrigationInputWithScanField(
                            label = "苗木二维码",
                            value = plant_id,
                            onValueChange = { plant_id = it },
                            onScanClick = { simulateScan("plant") },
                            placeholder = "手动输入苗木二维码"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        // 批量记录模式：级联选择
                        // 第一级：种植园
                        IrrigationSelectDropdown(
                            label = "种植园",
                            selectedValue = plantation_name,
                            options = plantationOptions,
                            onValueChange = {
                                plantation_name = it
                                // 切换园子时重置下级
                                region_type = ""
                                region_id = ""
                                seedbed_id = ""
                            }
                        )

                        // 第二级：选择地块或大棚 (前提是已选种植园)
                        AnimatedVisibility(visible = plantation_name.isNotEmpty()) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                IrrigationSelectDropdown(
                                    label = "区域类型",
                                    selectedValue = region_type,
                                    options = regionTypeOptions,
                                    onValueChange = {
                                        region_type = it
                                        region_id = ""
                                        seedbed_id = ""
                                    }
                                )
                            }
                        }

                        // 第三级：具体地块或大棚编号
                        AnimatedVisibility(visible = region_type.isNotEmpty()) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                IrrigationInputWithScanField(
                                    label = "${region_type}编号",
                                    value = region_id,
                                    onValueChange = { region_id = it },
                                    onScanClick = { simulateScan("region") },
                                    placeholder = "手动输入或扫码"
                                )
                            }
                        }

                        // 第四级：大棚下的苗床 (仅当类型为大棚时显示)
                        AnimatedVisibility(visible = region_type == "大棚") {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                // 增加 enabled 限制，大棚编号未填时，苗床编号不可输入且扫码按钮不可用
                                IrrigationInputWithScanField(
                                    label = "苗床编号",
                                    value = seedbed_id,
                                    onValueChange = { seedbed_id = it },
                                    onScanClick = { simulateScan("seedbed") },
                                    placeholder = if (region_id.isEmpty()) "请先输入大棚编号" else "手动输入或扫码",
                                    enabled = region_id.isNotEmpty()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 灌溉日期
                    OutlinedTextField(
                        value = irrigation_date,
                        onValueChange = { irrigation_date = it },
                        label = { Text("灌溉日期") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, "选择日期", tint = AgGreenPrimary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // [新增] 灌溉时段
                    IrrigationSelectDropdown(
                        label = "灌溉时段",
                        selectedValue = time_slot,
                        options = timeSlotOptions,
                        onValueChange = { time_slot = it }
                    )
                }
            }

            Text("灌溉详情", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 灌溉方式
                    IrrigationSelectDropdown(
                        label = "灌溉方式",
                        selectedValue = irrigation_method,
                        options = methodOptions,
                        onValueChange = { irrigation_method = it }
                    )

                    // [删除] 灌溉量 (water_amount) 输入框相关代码
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
                        placeholder = { Text("天气情况、水源状态等", color = Color.Gray) },
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
// ⬇️ 内部组件 (前缀 Irrigation)
// =================================================================

@Composable
private fun IrrigationScanSection(isScanning: Boolean, inputMode: Int, onScanClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(180.dp).clickable { if (!isScanning) onScanClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF263238))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isScanning) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AgGreenPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("识别中...", color = Color.White)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.QrCodeScanner, null, tint = Color.White, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    val title = if (inputMode == 0) "点击扫描苗木二维码" else "点击扫描区域二维码"
                    Text(title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    val subtitle = if (inputMode == 0) "直接录入苗木灌溉记录" else "批量录入区域(地块/大棚)灌溉记录"
                    Text(subtitle, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

// 增加 enabled 参数，控制组件是否可点击和输入
@Composable
private fun IrrigationInputWithScanField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onScanClick: () -> Unit,
    placeholder: String = "",
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        trailingIcon = {
            IconButton(onClick = onScanClick, enabled = enabled) {
                Icon(
                    Icons.Default.DocumentScanner,
                    contentDescription = "Scan",
                    tint = if (enabled) AgGreenPrimary else Color.Gray
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AgGreenPrimary,
            focusedLabelColor = AgGreenPrimary,
            disabledBorderColor = Color.LightGray,
            disabledLabelColor = Color.Gray,
            disabledTextColor = Color.Gray
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IrrigationSelectDropdown(label: String, selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
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