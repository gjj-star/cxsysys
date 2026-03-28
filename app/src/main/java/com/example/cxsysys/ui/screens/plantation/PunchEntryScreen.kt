package com.example.cxsysys.ui.screens.plantation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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

// 引入双模式扫码组件与大卡片组件
import com.example.cxsysys.ui.components.TopScanCard
import com.example.cxsysys.ui.components.DualModeIdentifierField

/**
 * 打孔结香录入页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PunchEntryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- 表单状态 (对应 V10 数据库字段) ---
    // 录入模式：0-个别录入(苗木), 1-批量录入(地块)。默认为1
    var inputMode by remember { mutableIntStateOf(1) }

    // 将自编码模式状态上提至父页面 (仅地块会用到)
    var isSelfCodeMode by remember { mutableStateOf(false) }

    // 【修改】：苗木只有二维码状态
    var plant_qr_code by remember { mutableStateOf("") }

    // 地块保持双模式
    var field_qr_code by remember { mutableStateOf("") }
    var field_self_code by remember { mutableStateOf("") }

    // punch_date varchar(8) 打孔日期
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var punch_date by remember { mutableStateOf(dateFormat.format(Date())) }

    // time_slot varchar(10) 打孔时段 (6-8时，9-11时，12-14时，15-17时，18-20时)
    var time_slot by remember { mutableStateOf("9-11时") }
    val timeSlotOptions = listOf("6-8时", "9-11时", "12-14时", "15-17时", "18-20时")

    // 结香规格字段
    var hole_depth by remember { mutableStateOf("") }      // hole_depth 平均孔深 (cm)
    var hole_diameter by remember { mutableStateOf("") }   // hole_diameter 孔径 (mm)
    var hole_pitch by remember { mutableStateOf("") }      // hole_pitch 平均孔距 (cm)

    // remark text 备注 (工具、孔口处理等描述)
    var remark by remember { mutableStateOf("") }

    // UI 状态
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var isScanning by remember { mutableStateOf(false) }

    // 模拟扫码逻辑
    fun simulateScan() {
        scope.launch {
            isScanning = true
            val msg = if (inputMode == 0) "正在识别苗木二维码..." else "正在识别地块二维码..."
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            delay(1500)
            isScanning = false
            // 扫码成功后，赋值给对应的 qr_code 变量
            if (inputMode == 0) {
                plant_qr_code = "TREE-PUNCH-V10-099"
            } else {
                field_qr_code = "FIELD-PUNCH-V10-C03"
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
                        punch_date = dateFormat.format(Date(millis))
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
                title = { Text("打孔结香录入", fontWeight = FontWeight.Bold) },
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
                        // 【修改点】：校验逻辑更新，苗木只看二维码
                        val targetValid = if (inputMode == 0) {
                            plant_qr_code.isNotEmpty()
                        } else {
                            field_qr_code.isNotEmpty() || field_self_code.isNotEmpty()
                        }

                        if (!targetValid) {
                            val msg = if (inputMode == 0) "请扫码提供苗木标识信息" else "请扫码或输入地块编码"
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
            // 1. 模式切换器
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
                    Text("批量录入 (地块)", color = if (inputMode == 1) Color.White else Color.Gray, fontWeight = if (inputMode == 1) FontWeight.Bold else FontWeight.Normal)
                }
            }

            // 2. 顶部扫码区 (加入平滑的收起动画)
            // 【修改】：判断是否需要显示顶部大卡片。如果是苗木模式，则常驻显示；地块模式跟随 isSelfCodeMode 状态。
            val shouldShowScanCard = if (inputMode == 0) true else !isSelfCodeMode

            AnimatedVisibility(
                visible = shouldShowScanCard,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                TopScanCard(
                    isScanning = isScanning,
                    title = if (inputMode == 0) "点击扫描苗木二维码" else "点击扫描地块二维码",
                    subtitle = if (inputMode == 0) "直接录入苗木打孔结香信息" else "批量录入地块打孔结香信息",
                    onScanClick = { simulateScan() }
                )
            }

            Text("作业基本信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (inputMode == 0) {
                        // 【修改】：苗木锁死为二维码模式
                        DualModeIdentifierField(
                            targetName = "苗木",
                            qrCodeValue = plant_qr_code,
                            onQrCodeChange = { plant_qr_code = it },
                            selfCodeValue = "",
                            onSelfCodeChange = { },
                            isSelfCodeMode = false, // 永远为 false，保持扫码模式
                            onModeChange = { },     // 不响应切换
                            onScanClick = { simulateScan() },
                            showModeToggle = false  // 隐藏右上角的切换按钮
                        )
                    } else {
                        // 地块保持双模式可切换
                        DualModeIdentifierField(
                            targetName = "定植地块",
                            qrCodeValue = field_qr_code,
                            onQrCodeChange = { field_qr_code = it },
                            selfCodeValue = field_self_code,
                            onSelfCodeChange = { field_self_code = it },
                            isSelfCodeMode = isSelfCodeMode,
                            onModeChange = { isSelfCodeMode = it },
                            onScanClick = { simulateScan() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // punch_date
                    OutlinedTextField(
                        value = punch_date,
                        onValueChange = { punch_date = it },
                        readOnly = true, // 防止键盘弹起
                        label = { Text("打孔日期") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, "选择日期", tint = AgGreenPrimary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // time_slot
                    PunchingSelectDropdown(
                        label = "打孔时段",
                        selectedValue = time_slot,
                        options = timeSlotOptions,
                        onValueChange = { time_slot = it }
                    )
                }
            }

            Text("结香规格", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // hole_depth
                    OutlinedTextField(
                        value = hole_depth,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) hole_depth = it },
                        label = { Text("平均孔深") },
                        placeholder = { Text("cm/厘米") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("cm", color = Color.Gray, modifier = Modifier.padding(end = 8.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // hole_diameter
                    OutlinedTextField(
                        value = hole_diameter,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) hole_diameter = it },
                        label = { Text("孔径") },
                        placeholder = { Text("mm/毫米") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("mm", color = Color.Gray, modifier = Modifier.padding(end = 8.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // hole_pitch
                    OutlinedTextField(
                        value = hole_pitch,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) hole_pitch = it },
                        label = { Text("平均孔距") },
                        placeholder = { Text("cm/厘米") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("cm", color = Color.Gray, modifier = Modifier.padding(end = 8.dp)) },
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
                    // remark
                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        label = { Text("备注") },
                        placeholder = { Text("工具、孔口处理等描述", color = Color.Gray) },
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

// 【注】：原先的 PunchingScanSection 已经被删除，复用了统一样式的 TopScanCard 和 DualModeIdentifierField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PunchingSelectDropdown(label: String, selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
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