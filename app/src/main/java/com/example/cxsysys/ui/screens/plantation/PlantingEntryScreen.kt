package com.example.cxsysys.ui.screens.plantation

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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

// 引入提取的公共组件
import com.example.cxsysys.ui.components.TopScanCard
import com.example.cxsysys.ui.components.DualModeIdentifierField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantingEntryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // 【新增】：将自编码模式状态上提至父页面
    var isSelfCodeMode by remember { mutableStateOf(false) }

    // --- 表单状态 (对应 V10 plant 表字段) ---
    // 【修改】：地块 ID 拆分为 二维码 和 自编码 两个独立状态
    var fieldQrCode by remember { mutableStateOf("") }
    var fieldSelfCode by remember { mutableStateOf("") }

    // 【修改】：母树改为只能输入自编码的普通文本
    var motherTreeSelfCode by remember { mutableStateOf("") }

    // 沉香品种 (V10: subspecies_id 沉香品种细分id: 0-野生沉香，1-人工白木香，2-人工奇楠沉香)
    var subspeciesIdLabel by remember { mutableStateOf("2-人工奇楠沉香") }
    val subspeciesOptions = listOf("0-野生沉香", "1-人工白木香", "2-人工奇楠沉香")

    var generation by remember { mutableStateOf("1") }    // 苗木代数

    // (V10: generation_way 育苗方法: 嫁接/扦插/圈枝/组培/其他)
    var generationWay by remember { mutableStateOf("嫁接") }
    val generationWayOptions = listOf("嫁接", "扦插", "圈枝", "组培", "其他")

    // 种植规格
    var caveDepth by remember { mutableStateOf("") }      // 穴深
    var caveWidth by remember { mutableStateOf("") }      // 穴宽
    var plantSpacing by remember { mutableStateOf("") }   // 种植间距
    var plantCount by remember { mutableStateOf("") }     // 定植数量

    // 定植日期
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dateTimeFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    var plantingDate by remember { mutableStateOf(dateFormat.format(Date())) }
    var entryDateTime by remember { mutableStateOf("") }

    // UI 状态
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var isScanning by remember { mutableStateOf(false) }
    var showPrintConfirmDialog by remember { mutableStateOf(false) }

    // 模拟扫码 (精简：现在只有地块需要扫码)
    fun simulateScan() {
        scope.launch {
            isScanning = true
            Toast.makeText(context, "正在识别地块二维码...", Toast.LENGTH_SHORT).show()
            delay(1500)
            isScanning = false
            fieldQrCode = "FIELD-A-03" // 扫码成功填入二维码字段
            Toast.makeText(context, "扫码成功", Toast.LENGTH_SHORT).show()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        plantingDate = dateFormat.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("确定", color = AgGreenPrimary) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消", color = Color.Gray) } }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }

    if (showPrintConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPrintConfirmDialog = false },
            title = { Text("打印提示") },
            text = { Text("是否打印本次录入的 $plantCount 株苗木标签？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPrintConfirmDialog = false
                        launchPlantingBatchPrint(
                            context = context,
                            fieldCode = if (fieldSelfCode.isNotBlank()) fieldSelfCode else fieldQrCode,
                            plantingDate = plantingDate,
                            entryDateTime = entryDateTime,
                            plantCount = plantCount.toIntOrNull() ?: 0,
                            subspecies = subspeciesIdLabel,
                            generation = generation,
                            generationWay = generationWay,
                            motherTreeSelfCode = motherTreeSelfCode
                        )
                    }
                ) { Text("确定", color = AgGreenPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showPrintConfirmDialog = false }) {
                    Text("暂不打印", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("苗木定植录入", fontWeight = FontWeight.Bold) },
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
                        // 校验：二维码或自编码必填其一
                        if (fieldQrCode.isEmpty() && fieldSelfCode.isEmpty()) {
                            Toast.makeText(context, "请扫码或输入地块编码", Toast.LENGTH_SHORT).show()
                        } else if (plantCount.isEmpty()) {
                            Toast.makeText(context, "请输入定植数量", Toast.LENGTH_SHORT).show()
                        } else if ((plantCount.toIntOrNull() ?: 0) > 999) {
                            Toast.makeText(context, "定植株数超过上限(999)", Toast.LENGTH_SHORT).show()
                        } else {
                            entryDateTime = dateTimeFormat.format(Date())
                            Toast.makeText(context, "苗木定植信息保存成功！", Toast.LENGTH_SHORT).show()
                            showPrintConfirmDialog = true
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
            // 1. 扫码关联 (加入平滑的收起动画)
            AnimatedVisibility(
                visible = !isSelfCodeMode,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                TopScanCard(
                    isScanning = isScanning,
                    title = "点击扫描地块二维码",
                    subtitle = "直接关联地块信息",
                    onScanClick = { simulateScan() }
                )
            }

            // 2. 基础信息
            Text("基础档案", fontWeight = FontWeight.Bold, color = Color.Gray)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // 【修改】：使用通用的双模式组件处理地块信息
                    DualModeIdentifierField(
                        targetName = "定植地块",
                        qrCodeValue = fieldQrCode,
                        onQrCodeChange = { fieldQrCode = it },
                        selfCodeValue = fieldSelfCode,
                        onSelfCodeChange = { fieldSelfCode = it },
                        isSelfCodeMode = isSelfCodeMode,
                        onModeChange = { isSelfCodeMode = it },
                        onScanClick = { simulateScan() }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 【修改】：母树自编码，改为普通的单行文本输入框，无扫码功能
                    OutlinedTextField(
                        value = motherTreeSelfCode,
                        onValueChange = { motherTreeSelfCode = it },
                        label = { Text("母树自编码 (选填)") },
                        placeholder = { Text("选填，关联母树档案", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AgGreenPrimary,
                            focusedLabelColor = AgGreenPrimary,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 品种细分
                    PlantingSelectDropdown(
                        label = "品种细分",
                        selectedValue = subspeciesIdLabel,
                        options = subspeciesOptions,
                        onValueChange = { subspeciesIdLabel = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 育苗方法
                    PlantingSelectDropdown(
                        label = "育苗方法",
                        selectedValue = generationWay,
                        options = generationWayOptions,
                        onValueChange = { generationWay = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 代数
                    OutlinedTextField(
                        value = generation,
                        onValueChange = { generation = it },
                        label = { Text("苗木代数") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 定植日期
                    OutlinedTextField(
                        value = plantingDate,
                        onValueChange = { plantingDate = it },
                        readOnly = true, // 防止点开弹出键盘
                        label = { Text("定植日期") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, "选择日期", tint = AgGreenPrimary)
                            }
                        }
                    )
                }
            }

            // 3. 种植规格
            Text("种植规格", fontWeight = FontWeight.Bold, color = Color.Gray)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // 穴深
                        OutlinedTextField(
                            value = caveDepth,
                            onValueChange = { if (it.all { c -> c.isDigit() }) caveDepth = it },
                            label = { Text("穴深") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = { Text("cm", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )
                        // 穴宽
                        OutlinedTextField(
                            value = caveWidth,
                            onValueChange = { if (it.all { c -> c.isDigit() }) caveWidth = it },
                            label = { Text("穴宽") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = { Text("cm", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 种植间距
                    OutlinedTextField(
                        value = plantSpacing,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) plantSpacing = it },
                        label = { Text("种植间距") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("m (米)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 定植数量
                    OutlinedTextField(
                        value = plantCount,
                        onValueChange = { if (it.all { c -> c.isDigit() }) plantCount = it },
                        label = { Text("定植数量") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("株", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )
                }
            }

            // 提示信息
            PlantingInfoTip(text = "系统将根据‘地块标识’关联生成定植档案。若该苗木为采购幼苗，请确保已在育苗阶段完成基础信息录入。")

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// =================================================================
// ⬇️ 组件定义
// =================================================================

// 通用选择下拉框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantingSelectDropdown(label: String, selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
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

@Composable
private fun PlantingInfoTip(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.Info, null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = Color(0xFF0D47A1), fontSize = 12.sp, lineHeight = 18.sp)
    }
}

private fun launchPlantingBatchPrint(
    context: android.content.Context,
    fieldCode: String,
    plantingDate: String,
    entryDateTime: String,
    plantCount: Int,
    subspecies: String,
    generation: String,
    generationWay: String,
    motherTreeSelfCode: String
) {
    val intent = Intent().apply {
        setClassName(context, PRINTER_ACTIVITY_CLASS)
        putExtra(EXTRA_TARGET_TEMPLATE, TEMP_MM)
        putExtra(EXTRA_PRINT_SOURCE, PRINT_SOURCE_PLANTING_ENTRY)
        putExtra(EXTRA_ENTRY_FIELD_CODE, fieldCode)
        putExtra(EXTRA_ENTRY_PLANTING_DATE, plantingDate)
        putExtra(EXTRA_ENTRY_RECORD_TIME, entryDateTime)
        putExtra(EXTRA_ENTRY_PLANT_COUNT, plantCount)
        putExtra(EXTRA_ENTRY_SUBSPECIES, subspecies)
        putExtra(EXTRA_ENTRY_GENERATION, generation)
        putExtra(EXTRA_ENTRY_GENERATION_WAY, generationWay)
        putExtra(EXTRA_ENTRY_MOTHER_TREE_SELF_CODE, motherTreeSelfCode)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "打印模块暂不可用", Toast.LENGTH_SHORT).show()
    }
}

private const val PRINTER_ACTIVITY_CLASS = "com.example.printerfeature.MainActivity"
private const val EXTRA_TARGET_TEMPLATE = "target_template"
private const val TEMP_MM = "苗木二维码"
private const val EXTRA_PRINT_SOURCE = "print_source"
private const val PRINT_SOURCE_PLANTING_ENTRY = "planting_entry"
private const val EXTRA_ENTRY_FIELD_CODE = "entry_field_code"
private const val EXTRA_ENTRY_PLANTING_DATE = "entry_planting_date"
private const val EXTRA_ENTRY_RECORD_TIME = "entry_record_time"
private const val EXTRA_ENTRY_PLANT_COUNT = "entry_plant_count"
private const val EXTRA_ENTRY_SUBSPECIES = "entry_subspecies"
private const val EXTRA_ENTRY_GENERATION = "entry_generation"
private const val EXTRA_ENTRY_GENERATION_WAY = "entry_generation_way"
private const val EXTRA_ENTRY_MOTHER_TREE_SELF_CODE = "entry_mother_tree_self_code"
