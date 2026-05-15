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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cxsysys.model.PlantingRequest
import com.example.cxsysys.ui.components.DualModeIdentifierField
import com.example.cxsysys.ui.components.TopScanCard
import com.example.cxsysys.ui.components.ValidatedDropdownField
import com.example.cxsysys.ui.components.ValidatedDateField
import com.example.cxsysys.ui.components.ValidatedOutlinedTextField
import com.example.cxsysys.ui.components.rememberFormValidationState
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import com.example.cxsysys.viewmodel.PlantingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantingEntryScreen(
    onBackClick: () -> Unit,
    viewModel: PlantingViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // 表单验证状态
    val validationState = rememberFormValidationState()

    val subspeciesList by viewModel.subspeciesList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMsg.collectAsState()
    val submitSuccess by viewModel.submitSuccess.collectAsState()
    
    // 页面加载时获取初始数据
    LaunchedEffect(Unit) {
        viewModel.fetchInitialData()
    }

    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    var showPrintConfirmDialog by remember { mutableStateOf(false) }
    
    // 提交成功后不直接退出，而是弹出打印提示
    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            Toast.makeText(context, "苗木定植信息保存成功！", Toast.LENGTH_SHORT).show()
            viewModel.clearSubmitSuccess()
            showPrintConfirmDialog = true
        }
    }

    // 【新增】：将自编码模式状态上提至父页面
    var isSelfCodeMode by remember { mutableStateOf(false) }

    // --- 表单状态 (对应 V10 plant 表字段) ---
    // 【修改】：地块 ID 拆分为 二维码 和 自编码 两个独立状态
    var fieldQrCode by remember { mutableStateOf("") }
    var fieldSelfCode by remember { mutableStateOf("") }

    // 【修改】：母树改为二维码状态
    var motherTreeQrCode by remember { mutableStateOf("") }

    // 沉香品种
    var subspeciesIdLabel by remember { mutableStateOf("") }
    var subspeciesId by remember { mutableStateOf<Int?>(null) }
    val subspeciesOptions = subspeciesList.map { it.subspeciesName }

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
    var showScanner by remember { mutableStateOf(false) }
    
    // 用于区分当前是在扫地块还是扫母树
    var scanTarget by remember { mutableStateOf("field") } // "field" 或 "motherTree"

    // 真实扫码界面
    if (showScanner) {
        com.example.cxsysys.ui.components.ScannerScreen(
            onScanResult = { result ->
                showScanner = false
                if (scanTarget == "field") {
                    fieldQrCode = result
                    Toast.makeText(context, "扫码成功", Toast.LENGTH_SHORT).show()
                } else if (scanTarget == "motherTree") {
                    motherTreeQrCode = result
                    Toast.makeText(context, "扫码成功", Toast.LENGTH_SHORT).show()
                }
            },
            onCancel = {
                showScanner = false
            }
        )
        return
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
            onDismissRequest = { 
                showPrintConfirmDialog = false
                onBackClick() // 取消打印也退出页面
            },
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
                            motherTreeSelfCode = motherTreeQrCode // 传入更新后的变量
                        )
                        onBackClick() // 打印后退出页面
                    }
                ) { Text("确定", color = AgGreenPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPrintConfirmDialog = false
                    onBackClick()
                }) {
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
                        // 使用表单验证状态进行验证
                        val isValid = validationState.validateOnSubmit(
                            mapOf(
                                "identifier" to if (fieldQrCode.isEmpty() && fieldSelfCode.isEmpty()) null else "valid",
                                "subspecies" to subspeciesIdLabel,
                                "generationWay" to generationWay,
                                "recordDate" to plantingDate,
                                "caveDepth" to caveDepth,
                                "caveWidth" to caveWidth,
                                "plantSpacing" to plantSpacing,
                                "plantCount" to plantCount
                            )
                        )

                        if (isValid) {
                            // 额外验证：定植数量上限
                            if ((plantCount.toIntOrNull() ?: 0) > 999) {
                                Toast.makeText(context, "定植株数超过上限(999)", Toast.LENGTH_SHORT).show()
                            } else {
                                entryDateTime = dateTimeFormat.format(Date())
                                val request = PlantingRequest(
                                    fieldQrcode = fieldQrCode.ifEmpty { null },
                                    fieldCode = fieldSelfCode.ifEmpty { null },
                                    mothertreeQrcode = motherTreeQrCode.ifEmpty { null },
                                    enterpriseSubspeciesId = subspeciesId!!,
                                    generationWay = generationWay,
                                    generation = generation,
                                    saplingDate = plantingDate,
                                    holeDepth = caveDepth.toDoubleOrNull() ?: 0.0,
                                    holeWidth = caveWidth.toDoubleOrNull() ?: 0.0,
                                    plantSpacing = plantSpacing.toDoubleOrNull() ?: 0.0,
                                    quantity = plantCount.toIntOrNull() ?: 0
                                )
                                viewModel.submitPlanting(request)
                            }
                        } else {
                            Toast.makeText(context, "请补全必填信息", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("保存信息", fontSize = 16.sp)
                    }
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
                    isScanning = false,
                    title = "点击扫描地块二维码",
                    subtitle = "直接关联地块信息",
                    onScanClick = {
                        scanTarget = "field"
                        showScanner = true
                    }
                )
            }

            // 2. 基础信息
            Text("基础档案", fontWeight = FontWeight.Bold, color = Color.Gray)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // 使用通用的双模式组件处理地块信息
                    DualModeIdentifierField(
                        targetName = "定植地块",
                        qrCodeValue = fieldQrCode,
                        onQrCodeChange = { fieldQrCode = it },
                        selfCodeValue = fieldSelfCode,
                        onSelfCodeChange = { fieldSelfCode = it },
                        isSelfCodeMode = isSelfCodeMode,
                        onModeChange = { isSelfCodeMode = it },
                        onScanClick = {
                            scanTarget = "field"
                            showScanner = true
                        },
                        validationState = validationState,
                        fieldKey = "identifier",
                        isRequired = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 【修改】：母树二维码，原生组件，只读且带有扫码图标
                    OutlinedTextField(
                        value = motherTreeQrCode,
                        onValueChange = { motherTreeQrCode = it },
                        readOnly = true, // 限制只能通过扫码录入
                        label = { Text("母树二维码 (选填)") },
                        placeholder = { Text("请点击右侧图标扫码", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                scanTarget = "motherTree"
                                showScanner = true
                            }) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = AgGreenPrimary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AgGreenPrimary,
                            focusedLabelColor = AgGreenPrimary,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 品种细分
                    ValidatedDropdownField(
                        label = "品种细分",
                        value = subspeciesIdLabel,
                        placeholder = "请选择品种",
                        options = subspeciesOptions,
                        onValueChange = { selectedLabel -> 
                            subspeciesIdLabel = selectedLabel
                            subspeciesId = subspeciesList.find { it.subspeciesName == selectedLabel }?.enterpriseSubspeciesId
                        },
                        fieldKey = "subspecies",
                        validationState = validationState,
                        isRequired = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 育苗方法
                    ValidatedDropdownField(
                        label = "育苗方法",
                        value = generationWay,
                        placeholder = "请选择育苗方法",
                        options = generationWayOptions,
                        onValueChange = { generationWay = it },
                        fieldKey = "generationWay",
                        validationState = validationState,
                        isRequired = true
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
                    ValidatedDateField(
                        value = plantingDate,
                        label = "定植日期",
                        fieldKey = "recordDate",
                        validationState = validationState,
                        isRequired = true,
                        onDateClick = { showDatePicker = true },
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
                        ValidatedOutlinedTextField(
                            value = caveDepth,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) caveDepth = it },
                            label = "穴深",
                            fieldKey = "caveDepth",
                            validationState = validationState,
                            isRequired = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = { Text("cm", color = Color.Gray) }
                        )
                        // 穴宽
                        ValidatedOutlinedTextField(
                            value = caveWidth,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) caveWidth = it },
                            label = "穴宽",
                            fieldKey = "caveWidth",
                            validationState = validationState,
                            isRequired = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = { Text("cm", color = Color.Gray) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 种植间距
                    ValidatedOutlinedTextField(
                        value = plantSpacing,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) plantSpacing = it },
                        label = "种植间距",
                        fieldKey = "plantSpacing",
                        validationState = validationState,
                        isRequired = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("m (米)", color = Color.Gray) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 定植数量
                    ValidatedOutlinedTextField(
                        value = plantCount,
                        onValueChange = { if (it.all { c -> c.isDigit() }) plantCount = it },
                        label = "定植数量",
                        fieldKey = "plantCount",
                        validationState = validationState,
                        isRequired = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("株", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) }
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
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
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