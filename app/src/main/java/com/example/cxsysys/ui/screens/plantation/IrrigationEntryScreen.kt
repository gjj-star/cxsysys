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

// 引入公共组件
import com.example.cxsysys.ui.components.TopScanCard
import com.example.cxsysys.ui.components.DualModeIdentifierField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IrrigationEntryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- 表单状态 ---
    // 录入模式：0-个别录入(苗木), 1-批量记录。默认为1
    var inputMode by remember { mutableIntStateOf(1) }

    // 将原本唯一的模式状态拆分为 3 个独立的状态，互不干扰
    var isPlantSelfCodeMode by remember { mutableStateOf(false) }
    var isRegionSelfCodeMode by remember { mutableStateOf(false) }
    var isSeedbedSelfCodeMode by remember { mutableStateOf(false) }

    // 将所有标识对象拆分为二维码与自编码状态
    var plantQrCode by remember { mutableStateOf("") }
    var plantSelfCode by remember { mutableStateOf("") }

    // 批量录入层级字段 (种植园 -> 大棚/地块 -> 苗床)
    var plantation_name by remember { mutableStateOf("") }
    val plantationOptions = listOf("茂名核心种植园", "电白试验园", "化州生态园")

    var region_type by remember { mutableStateOf("") }
    val regionTypeOptions = listOf("地块", "大棚")

    var regionQrCode by remember { mutableStateOf("") }
    var regionSelfCode by remember { mutableStateOf("") }

    var seedbedQrCode by remember { mutableStateOf("") }
    var seedbedSelfCode by remember { mutableStateOf("") }

    // 灌溉基本信息
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var irrigation_date by remember { mutableStateOf(dateFormat.format(Date())) }

    var time_slot by remember { mutableStateOf("9-11时") }
    val timeSlotOptions = listOf("6-8时", "9-11时", "12-14时", "15-17时", "18-20时")

    var irrigation_method by remember { mutableStateOf("滴灌") }
    val methodOptions = listOf("滴灌", "喷灌", "浇灌", "漫灌", "水肥一体化", "其他")

    var remark by remember { mutableStateOf("") }

    // UI 控制状态
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var isScanning by remember { mutableStateOf(false) }

    // 【极极极关键修复】：智能判断当前处于哪个输入步骤，跟随它的模式决定是否显示大卡片
    val showTopScanCard = when {
        inputMode == 0 -> !isPlantSelfCodeMode // 个别模式：看苗木
        inputMode == 1 -> {
            if (region_type == "大棚") {
                val isRegionFilled = regionQrCode.isNotEmpty() || regionSelfCode.isNotEmpty()
                if (!isRegionFilled) {
                    !isRegionSelfCodeMode // 第一步：如果大棚还没填，显隐跟随大棚的模式
                } else {
                    !isSeedbedSelfCodeMode // 第二步：如果大棚填完了，显隐跟随苗床的模式
                }
            } else {
                !isRegionSelfCodeMode // 地块模式：只有一步，直接跟随地块模式
            }
        }
        else -> true
    }

    // 模拟扫码逻辑
    fun simulateScan() {
        // 【防御性编程】：如果正在扫描，或者卡片按理说不该显示（防止动画退出期间的幽灵点击），直接拦截
        if (isScanning || !showTopScanCard) return

        scope.launch {
            isScanning = true
            val msg = if (inputMode == 0) "正在识别苗木二维码..." else "正在识别区域/苗床二维码..."
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            delay(1500)
            isScanning = false

            // 回填逻辑，同时【强制】将对应模式切换回“扫码模式(false)”，确保 UI 一定能渲染出扫出来的 qrCode
            if (inputMode == 0) {
                isPlantSelfCodeMode = false
                plantQrCode = "TREE-IRR-001"
            } else {
                isRegionSelfCodeMode = false
                isSeedbedSelfCodeMode = false
                plantation_name = "茂名核心种植园"
                region_type = "大棚"
                regionQrCode = "GH-A-01"
                seedbedQrCode = "BED-012"
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
                        val isPlantValid = plantQrCode.isNotEmpty() || plantSelfCode.isNotEmpty()
                        val isRegionValid = regionQrCode.isNotEmpty() || regionSelfCode.isNotEmpty()

                        if (inputMode == 0 && !isPlantValid) {
                            Toast.makeText(context, "请扫码或输入苗木编码", Toast.LENGTH_SHORT).show()
                        } else if (inputMode == 1 && (plantation_name.isEmpty() || region_type.isEmpty() || !isRegionValid)) {
                            Toast.makeText(context, "请完整填写灌溉区域信息", Toast.LENGTH_SHORT).show()
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

            // 2. 顶部扫码区 (加入平滑的收起动画)
            AnimatedVisibility(
                visible = showTopScanCard, // 使用刚计算出的智能布尔值
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                TopScanCard(
                    isScanning = isScanning,
                    title = if (inputMode == 0) "点击扫描苗木二维码" else "点击扫描区域/苗床二维码",
                    subtitle = if (inputMode == 0) "直接录入苗木灌溉记录" else "批量录入区域(地块/大棚)灌溉记录",
                    onScanClick = { simulateScan() }
                )
            }

            Text("作业基本信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 层级联动的关联对象输入
                    if (inputMode == 0) {
                        DualModeIdentifierField(
                            targetName = "苗木",
                            qrCodeValue = plantQrCode,
                            onQrCodeChange = { plantQrCode = it },
                            selfCodeValue = plantSelfCode,
                            onSelfCodeChange = { plantSelfCode = it },
                            isSelfCodeMode = isPlantSelfCodeMode,
                            onModeChange = { isPlantSelfCodeMode = it },// 绑定苗木的独立状态
                            onScanClick = { simulateScan() }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        // 第一级：种植园
                        IrrigationSelectDropdown(
                            label = "种植园",
                            selectedValue = plantation_name,
                            options = plantationOptions,
                            onValueChange = {
                                plantation_name = it
                                // 切换园子时重置所有下级数据和模式
                                region_type = ""
                                regionQrCode = ""
                                regionSelfCode = ""
                                seedbedQrCode = ""
                                seedbedSelfCode = ""
                                isRegionSelfCodeMode = false
                                isSeedbedSelfCodeMode = false
                            }
                        )

                        // 第二级：选择地块或大棚
                        AnimatedVisibility(visible = plantation_name.isNotEmpty()) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                IrrigationSelectDropdown(
                                    label = "区域类型",
                                    selectedValue = region_type,
                                    options = regionTypeOptions,
                                    onValueChange = {
                                        region_type = it
                                        regionQrCode = ""
                                        regionSelfCode = ""
                                        seedbedQrCode = ""
                                        seedbedSelfCode = ""
                                        isRegionSelfCodeMode = false
                                        isSeedbedSelfCodeMode = false
                                    }
                                )
                            }
                        }

                        // 第三级：具体地块或大棚编号
                        AnimatedVisibility(visible = region_type.isNotEmpty()) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                DualModeIdentifierField(
                                    targetName = region_type,
                                    qrCodeValue = regionQrCode,
                                    onQrCodeChange = { regionQrCode = it },
                                    selfCodeValue = regionSelfCode,
                                    onSelfCodeChange = { regionSelfCode = it },
                                    isSelfCodeMode = isRegionSelfCodeMode,
                                    onModeChange = { isRegionSelfCodeMode = it }, // 绑定大棚/地块的独立状态
                                    onScanClick = { simulateScan() }
                                )
                            }
                        }

                        // 第四级：大棚下的苗床 (仅当类型为大棚时显示)
                        AnimatedVisibility(visible = region_type == "大棚") {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                val isRegionFilled = regionQrCode.isNotEmpty() || regionSelfCode.isNotEmpty()
                                if (isRegionFilled) {
                                    DualModeIdentifierField(
                                        targetName = "苗床",
                                        qrCodeValue = seedbedQrCode,
                                        onQrCodeChange = { seedbedQrCode = it },
                                        selfCodeValue = seedbedSelfCode,
                                        onSelfCodeChange = { seedbedSelfCode = it },
                                        isSelfCodeMode = isSeedbedSelfCodeMode,
                                        onModeChange = { isSeedbedSelfCodeMode = it }, // 绑定苗床的独立状态
                                        onScanClick = { simulateScan() }
                                    )
                                } else {
                                    OutlinedTextField(
                                        value = "",
                                        onValueChange = {},
                                        label = { Text("苗床编号") },
                                        placeholder = { Text("请先确定上方大棚编号", color = Color.Gray, fontSize = 14.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = false,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledBorderColor = Color(0xFFE0E0E0),
                                            disabledLabelColor = Color.Gray
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 灌溉日期
                    OutlinedTextField(
                        value = irrigation_date,
                        onValueChange = { irrigation_date = it },
                        readOnly = true, // 防止键盘弹起
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

                    // 灌溉时段
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