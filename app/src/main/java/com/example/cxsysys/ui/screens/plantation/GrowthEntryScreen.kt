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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthEntryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- 表单状态 ---
    // 录入模式：0-个别录入(苗木), 1-批量录入(地块)。生长记录默认多为个别录入
    var inputMode by remember { mutableIntStateOf(0) }

    var plant_id by remember { mutableStateOf("") }
    var field_id by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var recordDate by remember { mutableStateOf(dateFormat.format(Date())) }

    // 生长数据
    var treeHeight by remember { mutableStateOf("") }
    var treeDiameter by remember { mutableStateOf("") }
    var plantQuantity by remember { mutableStateOf("") } // [新增] 植株主干分枝数

    var remark by remember { mutableStateOf("") }

    // UI 状态
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var isScanning by remember { mutableStateOf(false) }

    // 模拟扫码逻辑 (统一风格)
    fun simulateScan() {
        scope.launch {
            isScanning = true
            val msg = if (inputMode == 0) "正在识别苗木二维码..." else "正在识别地块二维码..."
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            delay(1500)
            isScanning = false
            if (inputMode == 0) {
                plant_id = "TREE-GROWTH-V10-001"
            } else {
                field_id = "FIELD-GROWTH-V10-A01"
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
                        recordDate = dateFormat.format(Date(millis))
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
                title = { Text("生长记录录入", fontWeight = FontWeight.Bold) },
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

            // 2. 顶部扫码区 (风格统一，支持动态切换)
            GrowthScanSection(
                isScanning = isScanning,
                inputMode = inputMode,
                onScanClick = { simulateScan() }
            )

            Text("基础信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 关联对象输入：根据模式动态切换标签与扫码提示
                    if (inputMode == 0) {
                        GrowthInputWithScanField(
                            label = "苗木二维码",
                            value = plant_id,
                            onValueChange = { plant_id = it },
                            onScanClick = { simulateScan() },
                            placeholder = "手动输入苗木二维码"
                        )
                    } else {
                        GrowthInputWithScanField(
                            label = "定植地块",
                            value = field_id,
                            onValueChange = { field_id = it },
                            onScanClick = { simulateScan() },
                            placeholder = "手动输入地块自编码"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 记录日期
                    OutlinedTextField(
                        value = recordDate,
                        onValueChange = { recordDate = it },
                        label = { Text("记录日期") },
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

            Text("生长指标", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 使用 Row 和 weight(1f) 保证“树高”与“树直径”在视觉上高度一致、完美对齐
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 树高
                        OutlinedTextField(
                            value = treeHeight,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) treeHeight = it },
                            label = { Text("树高") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = { Text("cm", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )

                        // 树直径
                        OutlinedTextField(
                            value = treeDiameter,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) treeDiameter = it },
                            label = { Text("树直径") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = { Text("cm", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // [新增] 植株主干分枝数
                    OutlinedTextField(
                        value = plantQuantity,
                        onValueChange = { if (it.all { c -> c.isDigit() }) plantQuantity = it },
                        label = { Text("植株主干分枝数") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("个", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )
                }
            }

            // [修改] 标题修改为“照片与补充说明”
            Text("照片与补充说明", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 照片上传模块
                    GrowthPhotoUploadBox(
                        onClick = { Toast.makeText(context, "打开相机/相册...", Toast.LENGTH_SHORT).show() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 备注
                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        label = { Text("生长情况描述 (选填)") },
                        placeholder = { Text("如：长势良好、叶片发黄等", color = Color.Gray) },
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
// ⬇️ 内部组件 (前缀 Growth)
// =================================================================

// 统一风格的顶部扫码模块
@Composable
private fun GrowthScanSection(isScanning: Boolean, inputMode: Int, onScanClick: () -> Unit) {
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
                    val subtitle = if (inputMode == 0) "直接录入苗木生长记录" else "批量录入地块生长记录"
                    Text(subtitle, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

// 统一格式的带扫码功能输入框，移除了原有的 leadingIcon 保持全系统统一
@Composable
private fun GrowthInputWithScanField(
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
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AgGreenPrimary,
            focusedLabelColor = AgGreenPrimary
        )
    )
}

@Composable
private fun GrowthPhotoUploadBox(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AddAPhoto, contentDescription = "Upload", tint = Color.Gray, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            // [修改] 文本修改为苗木生长状态照片
            Text("点击上传苗木生长状态照片", color = Color.Gray, fontSize = 14.sp)
        }
    }
}