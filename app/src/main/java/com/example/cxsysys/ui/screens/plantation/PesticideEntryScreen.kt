package com.example.cxsysys.ui.screens.plantation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- 数据模型 ---
data class Pesticide(
    val id: Int,
    val supplierId: Int,
    val name: String,
    val ingredient: String,
    val remark: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesticideEntryScreen(
    onBackClick: () -> Unit,
    onNavigateToPesticideAdd: () -> Unit = {} // 用于跳转至农药信息入库页面
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- 模拟数据 ---
    val pesticides = remember { mutableStateListOf(
        Pesticide(1, 1, "阿维菌素", "1.8% 乳油", "防治蚜虫、红蜘蛛"),
        Pesticide(2, 2, "吡虫啉", "10% 可湿性粉剂", "内吸性杀虫剂")
    ) }

    // --- 表单状态 ---
    // 录入模式：0-个别录入(苗木), 1-批量录入(地块)。默认为1 (大部分情境为批量)
    var inputMode by remember { mutableIntStateOf(1) }

    var plant_id by remember { mutableStateOf("") } // 苗木ID
    var field_id by remember { mutableStateOf("") } // 地块ID

    var selectedPesticide by remember { mutableStateOf<Pesticide?>(null) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var apply_date by remember { mutableStateOf(dateFormat.format(Date())) }

    // [新增] 施药时段
    var pesticide_time by remember { mutableStateOf("9-11时") }
    val timeSlotOptions = listOf("6-8时", "9-11时", "12-14时", "15-17时", "18-20时")

    // [修改] 剂量与浓度字段
    var dosage_ml_per_plant by remember { mutableStateOf("") } // 单株用量（ml）
    var method by remember { mutableStateOf("喷雾") } // 施药方式
    var concentration_ppm by remember { mutableStateOf("") } // 稀释浓度（ppm）

    var remark by remember { mutableStateOf("") }

    // UI 控制
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var isScanning by remember { mutableStateOf(false) }

    var showSelectPesticideDialog by remember { mutableStateOf(false) }
    val methodOptions = listOf("喷雾", "灌根", "涂抹", "喷粉", "其他")

    // 模拟扫码逻辑
    fun simulateScan() {
        scope.launch {
            isScanning = true
            val msg = if (inputMode == 0) "正在识别苗木二维码..." else "正在识别地块二维码..."
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            delay(1500)
            isScanning = false
            if (inputMode == 0) {
                plant_id = "TREE-PEST-2023-001"
            } else {
                field_id = "FIELD-PEST-B05"
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
                        apply_date = dateFormat.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("确定", color = AgGreenPrimary) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消", color = Color.Gray) } }
        ) { DatePicker(state = datePickerState, showModeToggle = false) }
    }

    // 选择农药弹窗
    if (showSelectPesticideDialog) {
        PesticideSelectDialog(
            pesticides = pesticides,
            onDismiss = { showSelectPesticideDialog = false },
            onConfirm = { pesticide ->
                selectedPesticide = pesticide
                showSelectPesticideDialog = false
            },
            onAddClick = {
                showSelectPesticideDialog = false
                onNavigateToPesticideAdd()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("施药作业录入", fontWeight = FontWeight.Bold) },
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
                        val targetValid = if (inputMode == 0) plant_id.isNotEmpty() else field_id.isNotEmpty()
                        if (!targetValid) {
                            val msg = if (inputMode == 0) "请扫码或输入苗木二维码" else "请扫码或输入地块自编码"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        } else if (selectedPesticide == null) {
                            Toast.makeText(context, "请选择农药", Toast.LENGTH_SHORT).show()
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
                        text = "个别录入 (苗木)",
                        color = if (inputMode == 0) Color.White else Color.Gray,
                        fontWeight = if (inputMode == 0) FontWeight.Bold else FontWeight.Normal
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
                        fontWeight = if (inputMode == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // 2. 顶部扫码区
            PesticideScanSection(
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
                    // 关联对象输入：根据模式动态切换标签与扫码逻辑
                    if (inputMode == 0) {
                        PesticideInputWithScanField(
                            label = "苗木二维码",
                            value = plant_id,
                            onValueChange = { plant_id = it },
                            onScanClick = { simulateScan() },
                            placeholder = "手动输入苗木二维码"
                        )
                    } else {
                        PesticideInputWithScanField(
                            label = "定植地块",
                            value = field_id,
                            onValueChange = { field_id = it },
                            onScanClick = { simulateScan() },
                            placeholder = "手动输入地块自编码"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 施药日期
                    OutlinedTextField(
                        value = apply_date,
                        onValueChange = { apply_date = it },
                        label = { Text("施药日期") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, "选择日期", tint = AgGreenPrimary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // [新增] 施药时段
                    PesticideDropdownField(
                        label = "施药时段",
                        selectedValue = pesticide_time,
                        options = timeSlotOptions,
                        onValueChange = { pesticide_time = it }
                    )
                }
            }

            Text("药剂详情", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 选择农药
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("所选农药", fontWeight = FontWeight.Bold)
                        TextButton(onClick = { showSelectPesticideDialog = true }) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Text("选择药剂", color = AgGreenPrimary)
                        }
                    }

                    if (selectedPesticide == null) {
                        Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(BgGray, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                            Text("暂未选择农药药剂", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth().border(1.dp, AgGreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selectedPesticide!!.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(selectedPesticide!!.ingredient, color = Color.Gray, fontSize = 11.sp)
                            }
                            IconButton(onClick = { selectedPesticide = null }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // [修改] 单株用量
                    OutlinedTextField(
                        value = dosage_ml_per_plant,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) dosage_ml_per_plant = it },
                        label = { Text("单株用量") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("ml", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 施药方式
                    PesticideDropdownField(
                        label = "施药方式",
                        selectedValue = method,
                        options = methodOptions,
                        onValueChange = { method = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // [修改] 稀释浓度
                    OutlinedTextField(
                        value = concentration_ppm,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) concentration_ppm = it },
                        label = { Text("稀释浓度") },
                        placeholder = { Text("如 1000", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("ppm", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        label = { Text("备注 (选填)") },
                        placeholder = { Text("病虫害程度、天气情况等", color = Color.Gray) },
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
// ⬇️ 内部组件
// =================================================================

// 顶部扫码模块
@Composable
private fun PesticideScanSection(isScanning: Boolean, inputMode: Int, onScanClick: () -> Unit) {
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
                    val title = if (inputMode == 0) "点击扫描苗木二维码" else "点击扫描地块二维码"
                    Text(title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    val subtitle = if (inputMode == 0) "直接录入关联苗木施药记录" else "批量录入关联地块施药记录"
                    Text(subtitle, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

// 带扫码功能的输入框组件
@Composable
private fun PesticideInputWithScanField(
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

// 选择药剂弹窗
@Composable
fun PesticideSelectDialog(
    pesticides: List<Pesticide>,
    onDismiss: () -> Unit,
    onConfirm: (Pesticide) -> Unit,
    onAddClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 300.dp, max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("选择要使用的农药", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AgGreenPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    pesticides.forEach { pesticide ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onConfirm(pesticide) }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(pesticide.name, fontWeight = FontWeight.Bold)
                                Text(pesticide.ingredient, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Icon(Icons.Default.AddCircleOutline, null, tint = AgGreenPrimary)
                        }
                        HorizontalDivider(color = BgGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 三个等宽按钮 (顺序：新增 -> 删除 -> 取消)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. 绿色的“新增”按钮
                    Button(
                        onClick = { onAddClick() },
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("新增", color = Color.White, fontSize = 14.sp)
                    }

                    // 2. 红色的“删除”按钮
                    Button(
                        onClick = {
                            // [预留接口代码] 暂时不写交互作用，方便后续修改或删除
                            // onDeleteClick()
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("删除", color = Color.White, fontSize = 14.sp)
                    }

                    // 3. 灰色的“取消”按钮
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("取消", color = Color(0xFF666666), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PesticideDropdownField(label: String, selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
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