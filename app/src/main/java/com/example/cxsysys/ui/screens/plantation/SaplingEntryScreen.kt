package com.example.cxsysys.ui.screens.plantation

import android.widget.Toast
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cxsysys.model.SaplingRequest
import com.example.cxsysys.ui.components.DualModeIdentifierField
import com.example.cxsysys.ui.components.TopScanCard
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import com.example.cxsysys.viewmodel.SaplingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaplingEntryScreen(
    onBackClick: () -> Unit,
    viewModel: SaplingViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // 观察 ViewModel 状态
    val greenhouseList by viewModel.greenhouseList.collectAsState()
    val seedbedList by viewModel.seedbedList.collectAsState()
    val subspeciesList by viewModel.subspeciesList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMsg.collectAsState()
    val submitSuccess by viewModel.submitSuccess.collectAsState()

    // 页面加载时获取初始数据
    LaunchedEffect(Unit) {
        viewModel.fetchInitialData()
    }

    // 错误和成功提示处理
    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            Toast.makeText(context, "幼苗信息已保存！", Toast.LENGTH_SHORT).show()
            viewModel.clearSubmitSuccess()
            onBackClick()
        }
    }

    // 将接口返回的列表转换为字符串选项供 DropdownField 使用
    val greenhouseOptions = greenhouseList.map { it.greenhouseCode }
    val seedbedOptions = seedbedList.map { "${it.seedbedCode} (空闲)" }
    val subspeciesOptions = subspeciesList.map { it.subspeciesName }
    val generationWayOptions = listOf("嫁接", "扦插", "圈枝", "组培", "其他")

    // --- 表单状态 ---
    var greenhouse_name by remember { mutableStateOf("") }
    var greenhouse_id by remember { mutableStateOf<Int?>(null) }
    var seedbed_code by remember { mutableStateOf("") }
    var seedbed_id by remember { mutableStateOf<Int?>(null) }

    var mother_tree_qr by remember { mutableStateOf("") }

    var generation by remember { mutableStateOf("") }
    var subspecies by remember { mutableStateOf("") }
    var subspecies_id by remember { mutableStateOf<Int?>(null) }
    var generation_way by remember { mutableStateOf("") }

    // 日期处理
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var sapling_date by remember { mutableStateOf(dateFormat.format(Date())) }
    var entry_date by remember { mutableStateOf(dateFormat.format(Date())) }

    var initial_quantity by remember { mutableStateOf("") }

    // UI 状态
    var showSaplingDatePicker by remember { mutableStateOf(false) }
    var showEntryDatePicker by remember { mutableStateOf(false) }
    val saplingDatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val entryDatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    var isScanning by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    if (showScanner) {
        com.example.cxsysys.ui.components.ScannerScreen(
            onScanResult = { result ->
                showScanner = false
                mother_tree_qr = result
                Toast.makeText(context, "扫码成功：已关联母树", Toast.LENGTH_SHORT).show()
            },
            onCancel = {
                showScanner = false
            }
        )
        return // 全屏显示扫码界面
    }

    // --- 日期选择器逻辑 ---
    if (showSaplingDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showSaplingDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    saplingDatePickerState.selectedDateMillis?.let { millis ->
                        sapling_date = dateFormat.format(Date(millis))
                    }
                    showSaplingDatePicker = false
                }) { Text("确定", color = AgGreenPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showSaplingDatePicker = false }) { Text("取消", color = Color.Gray) }
            }
        ) { DatePicker(state = saplingDatePickerState, showModeToggle = false) }
    }

    if (showEntryDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEntryDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    entryDatePickerState.selectedDateMillis?.let { millis ->
                        entry_date = dateFormat.format(Date(millis))
                    }
                    showEntryDatePicker = false
                }) { Text("确定", color = AgGreenPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showEntryDatePicker = false }) { Text("取消", color = Color.Gray) }
            }
        ) { DatePicker(state = entryDatePickerState, showModeToggle = false) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("幼苗培育录入", fontWeight = FontWeight.Bold) },
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
                        if (seedbed_id != null && subspecies_id != null && initial_quantity.isNotEmpty() && generation.isNotEmpty() && generation_way.isNotEmpty()) {
                            val request = SaplingRequest(
                                seedbedId = seedbed_id!!,
                                mothertreeQrcode = mother_tree_qr.ifEmpty { null },
                                enterpriseSubspeciesId = subspecies_id!!,
                                generation = generation,
                                generationWay = generation_way,
                                saplingDate = sapling_date,
                                entryDate = entry_date,
                                initialQuantity = initial_quantity.toInt()
                            )
                            viewModel.submitSapling(request)
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
            TopScanCard(
                isScanning = isScanning,
                title = "点击扫描母树二维码(选填)",
                subtitle = "快速关联繁育母树档案 ",
                onScanClick = { showScanner = true }
            )

            // 1. 位置信息卡片 (苗床关联)
            Text("位置信息", fontWeight = FontWeight.Bold, color = Color.Gray)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SaplingDropdownField(
                        label = "选择种植大棚",
                        value = greenhouse_name,
                        placeholder = "请选择大棚",
                        options = greenhouseOptions,
                        onValueChange = { selectedName ->
                            greenhouse_name = selectedName
                            val selectedGh = greenhouseList.find { it.greenhouseCode == selectedName }
                            if (selectedGh != null) {
                                greenhouse_id = selectedGh.greenhouseId
                                seedbed_code = ""
                                seedbed_id = null
                                viewModel.fetchSeedbedsByGreenhouse(selectedGh.greenhouseId)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SaplingDropdownField(
                        label = "选择空闲苗床",
                        value = seedbed_code,
                        placeholder = if (greenhouse_name.isEmpty()) "请先选择大棚" else "请选择空闲苗床",
                        options = if (greenhouse_name.isNotEmpty() && seedbedOptions.isEmpty()) listOf("无空闲苗床") else seedbedOptions,
                        onValueChange = { selectedNameWithStatus ->
                            if (selectedNameWithStatus != "无空闲苗床") {
                                seedbed_code = selectedNameWithStatus
                                val code = selectedNameWithStatus.replace(" (空闲)", "")
                                seedbed_id = seedbedList.find { it.seedbedCode == code }?.seedbedId
                            }
                        },
                        enabled = greenhouse_name.isNotEmpty()
                    )
                }
            }

            // 2. 幼苗基本属性
            Text("幼苗属性", fontWeight = FontWeight.Bold, color = Color.Gray)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    DualModeIdentifierField(
                        targetName = "母树",
                        qrCodeValue = mother_tree_qr,
                        onQrCodeChange = { mother_tree_qr = it },
                        selfCodeValue = "",
                        onSelfCodeChange = { },
                        isSelfCodeMode = false,
                        onModeChange = { },
                        onScanClick = { showScanner = true },
                        showModeToggle = false
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SaplingDropdownField(
                        label = "沉香品种细分",
                        value = subspecies,
                        placeholder = "请选择品种",
                        options = subspeciesOptions,
                        onValueChange = { selectedName ->
                            subspecies = selectedName
                            subspecies_id = subspeciesList.find { it.subspeciesName == selectedName }?.enterpriseSubspeciesId
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = generation,
                            onValueChange = { if (it.length <= 2) generation = it },
                            label = { Text("代数", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) },
                            placeholder = { Text("如: 1", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            SaplingDropdownField(
                                label = "育苗方法",
                                value = generation_way,
                                placeholder = "选择方法",
                                options = generationWayOptions,
                                onValueChange = { generation_way = it }
                            )
                        }
                    }
                }
            }

            // 3. 时间与数量
            Text("时间与数量", fontWeight = FontWeight.Bold, color = Color.Gray)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = sapling_date,
                            onValueChange = {},
                            label = { Text("嫁接/扦插/播种日期") },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = AgGreenPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showSaplingDatePicker = true })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = entry_date,
                            onValueChange = {},
                            label = { Text("入棚日期") },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = AgGreenPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showEntryDatePicker = true })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = initial_quantity,
                        onValueChange = { if (it.all { char -> char.isDigit() }) initial_quantity = it },
                        label = { Text("本苗床幼苗初始数量") },
                        placeholder = { Text("请输入数量") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


// === 内部组件 ===

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaplingDropdownField(
    label: String,
    value: String,
    placeholder: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) },
            placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) },
            readOnly = true,
            singleLine = true,
            enabled = enabled,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgGreenPrimary,
                focusedLabelColor = AgGreenPrimary,
                disabledContainerColor = BgGray.copy(alpha = 0.5f),
                disabledBorderColor = Color.LightGray
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}