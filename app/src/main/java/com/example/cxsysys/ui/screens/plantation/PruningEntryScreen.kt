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
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// 引入提取的顶部大卡片公共组件
import com.example.cxsysys.ui.components.TopScanCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PruningEntryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- 表单状态 ---
    // 录入模式：0-个别录入(苗木), 1-批量录入(地块)。默认为1 (大部分情境为批量)
    var inputMode by remember { mutableIntStateOf(1) }

    var plant_id by remember { mutableStateOf("") }
    var field_id by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var pruning_date by remember { mutableStateOf(dateFormat.format(Date())) }

    // [新增/找回] 选项数据状态
    var time_slot by remember { mutableStateOf("9-11时") }
    val timeSlotOptions = listOf("6-8时", "9-11时", "12-14时", "15-17时", "18-20时")

    var pruning_type by remember { mutableStateOf("疏剪") }
    val typeOptions = listOf("短截", "疏剪", "抹芽", "造型")

    var tool_type by remember { mutableStateOf("手剪") }
    val toolOptions = listOf("手剪", "高枝剪", "电锯", "其他")

    var disinfect_method by remember { mutableStateOf("酒精") }
    val disinfectOptions = listOf("酒精", "火焰", "次氯酸", "其他")

    var remark by remember { mutableStateOf("") }

    // UI 状态
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var isScanning by remember { mutableStateOf(false) }

    // 模拟扫码逻辑 (风格同施药/定植页)
    fun simulateScan() {
        scope.launch {
            isScanning = true
            val msg = if (inputMode == 0) "正在识别苗木二维码..." else "正在识别地块二维码..."
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            delay(1500)
            isScanning = false
            if (inputMode == 0) {
                plant_id = "TREE-PRUNE-V10-088"
            } else {
                field_id = "FIELD-PRUNE-V10-A01"
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
                        pruning_date = dateFormat.format(Date(millis))
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
                title = { Text("剪枝作业录入", fontWeight = FontWeight.Bold) },
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

            // 2. 顶部扫码区 (复用公共组件 TopScanCard)
            TopScanCard(
                isScanning = isScanning,
                title = if (inputMode == 0) "点击扫描苗木二维码" else "点击扫描地块二维码",
                subtitle = if (inputMode == 0) "直接录入苗木剪枝信息" else "批量录入地块剪枝信息",
                onScanClick = { simulateScan() }
            )

            Text("作业基本信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 关联对象输入：根据模式动态切换
                    if (inputMode == 0) {
                        PruningInputWithScanField(
                            label = "苗木二维码",
                            value = plant_id,
                            onValueChange = { plant_id = it },
                            onScanClick = { simulateScan() },
                            placeholder = "手动输入苗木二维码"
                        )
                    } else {
                        PruningInputWithScanField(
                            label = "定植地块",
                            value = field_id,
                            onValueChange = { field_id = it },
                            onScanClick = { simulateScan() },
                            placeholder = "手动输入地块自编码"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 剪枝日期
                    OutlinedTextField(
                        value = pruning_date,
                        onValueChange = { pruning_date = it },
                        label = { Text("剪枝日期") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, "选择日期", tint = AgGreenPrimary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // [找回] 剪枝时段 (time_slot)
                    PruningSelectDropdown(
                        label = "剪枝时段",
                        selectedValue = time_slot,
                        options = timeSlotOptions,
                        onValueChange = { time_slot = it }
                    )
                }
            }

            Text("作业细节", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // [找回] 剪枝类型 (prune_type)
                    PruningSelectDropdown(
                        label = "剪枝类型",
                        selectedValue = pruning_type,
                        options = typeOptions,
                        onValueChange = { pruning_type = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // [找回] 工具类型 (tool_type)
                    PruningSelectDropdown(
                        label = "工具类型",
                        selectedValue = tool_type,
                        options = toolOptions,
                        onValueChange = { tool_type = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // [找回] 消毒方式 (disinfect_method)
                    PruningSelectDropdown(
                        label = "消毒方式",
                        selectedValue = disinfect_method,
                        options = disinfectOptions,
                        onValueChange = { disinfect_method = it }
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
                        placeholder = { Text("记录剪口涂药、造型描述等", color = Color.Gray) },
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
// ⬇️ 内部组件 (前缀 Pruning)
// =================================================================

// 【删除处】原有的 PruningScanSection 已经被删除，转为调用引入的公共组件 TopScanCard

@Composable
private fun PruningInputWithScanField(
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
private fun PruningSelectDropdown(label: String, selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
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