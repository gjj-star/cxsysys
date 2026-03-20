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
// 引入刚刚提取的顶部大卡片公共组件
import com.example.cxsysys.ui.components.TopScanCard

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
    // [修改] 将树直径改为地径和胸径
    var groundDiameter by remember { mutableStateOf("") } // 地径
    var brestHeightDiameter by remember { mutableStateOf("") } // 胸径
    // [新增] 幅冠
    var crownWidth by remember { mutableStateOf("") }

    var plantQuantity by remember { mutableStateOf("") } // 植株主干分枝数

    // [新增] 主干通直度 (下拉框状态)
    var straightness by remember { mutableStateOf("") }
    var straightnessExpanded by remember { mutableStateOf(false) }
    val straightnessOptions = listOf("1 级：通直", "2 级：轻度弯曲", "3 级：严重弯曲")

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

            // 2. 顶部扫码区 (复用公共组件 TopScanCard)
            TopScanCard(
                isScanning = isScanning,
                title = if (inputMode == 0) "点击扫描苗木二维码" else "点击扫描地块二维码",
                subtitle = if (inputMode == 0) "直接录入苗木生长记录" else "批量录入地块生长记录",
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
                    // 第一行：树高、地径
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

                        // 地径
                        OutlinedTextField(
                            value = groundDiameter,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) groundDiameter = it },
                            label = { Text("地径") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = { Text("cm", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 第二行：胸径、幅冠
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 胸径
                        OutlinedTextField(
                            value = brestHeightDiameter,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) brestHeightDiameter = it },
                            label = { Text("胸径") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = { Text("cm", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )

                        // 幅冠
                        OutlinedTextField(
                            value = crownWidth,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) crownWidth = it },
                            label = { Text("幅冠") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = { Text("m", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 第三行：植株主干分枝数
                    OutlinedTextField(
                        value = plantQuantity,
                        onValueChange = { if (it.all { c -> c.isDigit() }) plantQuantity = it },
                        label = { Text("植株主干分枝数") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("个", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 第四行：主干通直度 (下拉框)
                    ExposedDropdownMenuBox(
                        expanded = straightnessExpanded,
                        onExpandedChange = { straightnessExpanded = !straightnessExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = straightness,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("主干通直度") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = straightnessExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )
                        ExposedDropdownMenu(
                            expanded = straightnessExpanded,
                            onDismissRequest = { straightnessExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            straightnessOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        straightness = selectionOption
                                        straightnessExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }
            }

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
            Text("点击上传苗木生长状态照片", color = Color.Gray, fontSize = 14.sp)
        }
    }
}