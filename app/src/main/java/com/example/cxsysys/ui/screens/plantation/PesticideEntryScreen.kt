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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cxsysys.model.Pesticide
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import com.example.cxsysys.viewmodel.PesticideViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 引入提取的公共组件
import com.example.cxsysys.ui.components.TopScanCard
import com.example.cxsysys.ui.components.DualModeIdentifierField
import com.example.cxsysys.ui.components.ValidatedDropdownField
import com.example.cxsysys.ui.components.ValidatedDateField
import com.example.cxsysys.ui.components.ValidatedOutlinedTextField
import com.example.cxsysys.ui.components.rememberFormValidationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesticideEntryScreen(
    onBackClick: () -> Unit,
    onNavigateToPesticideAdd: () -> Unit = {}, // 用于跳转至农药信息入库页面
    viewModel: PesticideViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // 观察 ViewModel 状态
    val pesticides by viewModel.pesticides.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val submitSuccess by viewModel.submitSuccess.collectAsState()

    // 处理提交成功
    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            Toast.makeText(context, "保存成功！", Toast.LENGTH_LONG).show()
            viewModel.resetSubmitState()
            onBackClick()
        }
    }

    // 处理错误提示
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetSubmitState()
        }
    }

    // 表单验证状态
    val validationState = rememberFormValidationState()

    // --- 表单状态 ---
    // 录入模式：0-个别录入(苗木), 1-批量录入(地块)。默认为1 (大部分情境为批量)
    var inputMode by remember { mutableIntStateOf(1) }

    // 将自编码模式状态上提至父页面
    var isSelfCodeMode by remember { mutableStateOf(false) }

    // 【修改】：苗木只有二维码状态
    var plantQrCode by remember { mutableStateOf("") }

    // 地块保持双模式
    var fieldQrCode by remember { mutableStateOf("") }
    var fieldSelfCode by remember { mutableStateOf("") }

    var selectedPesticide by remember { mutableStateOf<Pesticide?>(null) }
    var hasPesticideError by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var apply_date by remember { mutableStateOf(dateFormat.format(Date())) }

    // 施药时段
    var pesticide_time by remember { mutableStateOf("9-11时") }
    val timeSlotOptions = listOf("6-8时", "9-11时", "12-14时", "15-17时", "18-20时")

    // 剂量与浓度字段
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

    // 真实扫码状态
    var showScanner by remember { mutableStateOf(false) }

    if (showScanner) {
        com.example.cxsysys.ui.components.ScannerScreen(
            onScanResult = { result ->
                showScanner = false
                if (inputMode == 0) {
                    plantQrCode = result
                } else {
                    fieldQrCode = result
                }
                Toast.makeText(context, "扫码成功", Toast.LENGTH_SHORT).show()
            },
            onCancel = {
                showScanner = false
            }
        )
        return // 全屏显示扫码界面
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
                if (hasPesticideError) hasPesticideError = false
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
                        // 使用验证状态进行验证
                        val identifierValue = if (inputMode == 0) {
                            plantQrCode
                        } else {
                            if (isSelfCodeMode) fieldSelfCode else fieldQrCode
                        }
                        
                        val isValid = validationState.validateOnSubmit(
                            mapOf(
                                "identifier" to identifierValue,
                                "recordDate" to apply_date,
                                "pesticideTime" to pesticide_time,
                                "selectedPesticide" to (selectedPesticide?.pestId?.toString() ?: ""),
                                "dosage" to dosage_ml_per_plant,
                                "method" to method,
                                "concentration" to concentration_ppm
                            )
                        )
                        hasPesticideError = selectedPesticide == null

                        if (isValid && !hasPesticideError) {
                            viewModel.submitPesticideWork(
                                plantQrcode = if (inputMode == 0) plantQrCode else null,
                                fieldQrcode = if (inputMode == 1) fieldQrCode else null,
                                fieldCode = if (inputMode == 1) fieldSelfCode else null,
                                date = apply_date,
                                period = pesticide_time,
                                pestIds = listOf(selectedPesticide!!.pestId),
                                pestDosage = dosage_ml_per_plant.toDoubleOrNull() ?: 0.0,
                                pestMethod = method,
                                pestWater = concentration_ppm.toDoubleOrNull() ?: 0.0,
                                record = remark
                            )
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
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
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

            // 2. 顶部扫码区 (加入平滑的收起动画)
            // 【修改】：苗木模式下卡片常驻，地块模式下根据 isSelfCodeMode 显隐
            AnimatedVisibility(
                visible = if (inputMode == 0) true else !isSelfCodeMode,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                TopScanCard(
                    isScanning = isScanning,
                    title = if (inputMode == 0) "点击扫描苗木二维码" else "点击扫描地块二维码",
                    subtitle = if (inputMode == 0) "直接录入关联苗木施药记录" else "批量录入关联地块施药记录",
                    onScanClick = { showScanner = true }
                )
            }

            Text("作业基本信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 关联对象输入：根据模式动态切换标签与组件
                    if (inputMode == 0) {
                        // 【修改】：苗木锁死为二维码模式
                        DualModeIdentifierField(
                            targetName = "苗木",
                            qrCodeValue = plantQrCode,
                            onQrCodeChange = { plantQrCode = it },
                            selfCodeValue = "",
                            onSelfCodeChange = { },
                            isSelfCodeMode = false, // 永远为 false，保持扫码模式
                            onModeChange = { },     // 不响应切换
                            onScanClick = { showScanner = true },
                            showModeToggle = false, // 隐藏右上角的切换按钮
                            validationState = validationState,
                            fieldKey = "identifier",
                            isRequired = true
                        )
                    } else {
                        // 地块保持双模式可切换
                        DualModeIdentifierField(
                            targetName = "定植地块",
                            qrCodeValue = fieldQrCode,
                            onQrCodeChange = { fieldQrCode = it },
                            selfCodeValue = fieldSelfCode,
                            onSelfCodeChange = { fieldSelfCode = it },
                            isSelfCodeMode = isSelfCodeMode,
                            onModeChange = { isSelfCodeMode = it },
                            onScanClick = { showScanner = true },
                            validationState = validationState,
                            fieldKey = "identifier",
                            isRequired = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 施药日期
                    ValidatedDateField(
                        value = apply_date,
                        label = "施药日期",
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // 施药时段
                    ValidatedDropdownField(
                        label = "施药时段",
                        value = pesticide_time,
                        placeholder = "请选择施药时段",
                        options = timeSlotOptions,
                        onValueChange = { pesticide_time = it },
                        fieldKey = "pesticideTime",
                        validationState = validationState,
                        isRequired = true
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "* ", color = Color(0xFFE53935), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("所选农药", fontWeight = FontWeight.Bold, color = if (hasPesticideError) Color(0xFFE53935) else Color.Unspecified)
                        }
                        TextButton(onClick = { showSelectPesticideDialog = true }) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Text("选择药剂", color = AgGreenPrimary)
                        }
                    }

                    if (selectedPesticide == null) {
                        Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(BgGray, RoundedCornerShape(4.dp)).then(if (hasPesticideError) Modifier.border(1.dp, Color(0xFFE53935), RoundedCornerShape(4.dp)) else Modifier), contentAlignment = Alignment.Center) {
                            Text("暂未选择农药药剂", color = if (hasPesticideError) Color(0xFFE53935) else Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth().border(1.dp, AgGreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selectedPesticide!!.pestName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(selectedPesticide!!.pestIngredient, color = Color.Gray, fontSize = 11.sp)
                            }
                            IconButton(onClick = { selectedPesticide = null }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
                        }
                    }
                    if (hasPesticideError && selectedPesticide == null) {
                        Text(text = "此项为必填", color = Color(0xFFE53935), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 单株用量
                    ValidatedOutlinedTextField(
                        value = dosage_ml_per_plant,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) dosage_ml_per_plant = it },
                        label = "单株用量",
                        fieldKey = "dosage",
                        validationState = validationState,
                        isRequired = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("ml", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 施药方式
                    ValidatedDropdownField(
                        label = "施药方式",
                        value = method,
                        placeholder = "请选择施药方式",
                        options = methodOptions,
                        onValueChange = { method = it },
                        fieldKey = "method",
                        validationState = validationState,
                        isRequired = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 稀释浓度
                    ValidatedOutlinedTextField(
                        value = concentration_ppm,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) concentration_ppm = it },
                        label = "稀释浓度",
                        fieldKey = "concentration",
                        validationState = validationState,
                        isRequired = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("ppm", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) }
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

// 【注】：原有的 PesticideInputWithScanField 已经被删除，复用了统一样式的 DualModeIdentifierField

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
                    if (pesticides.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("暂无农药数据，请先新增", color = Color.Gray)
                        }
                    } else {
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
                                    Text(pesticide.pestName, fontWeight = FontWeight.Bold)
                                    Text(pesticide.pestIngredient, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Icon(Icons.Default.AddCircleOutline, null, tint = AgGreenPrimary)
                            }
                            HorizontalDivider(color = BgGray)
                        }
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

