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
import androidx.compose.ui.text.style.TextOverflow
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

    // 将模式状态拆分，互不干扰 (因为苗木现在只扫码，所以只需保留大棚的模式状态)
    var isRegionSelfCodeMode by remember { mutableStateOf(false) }

    // 【修改】：苗木只有二维码状态
    var plantQrCode by remember { mutableStateOf("") }

    // 批量录入层级字段 (种植园 -> 大棚/地块 -> 苗床)
    var plantation_name by remember { mutableStateOf("") }
    val plantationOptions = listOf("茂名核心种植园", "电白试验园", "化州生态园")

    var region_type by remember { mutableStateOf("") }
    val regionTypeOptions = listOf("地块", "大棚")

    var regionQrCode by remember { mutableStateOf("") }
    var regionSelfCode by remember { mutableStateOf("") }

    // 【新增】：苗床多选状态与模拟数据
    val selectedSeedbeds = remember { mutableStateListOf<String>() }
    // 模拟根据大棚返回的苗床列表
    val getSeedbedsByRegion = { regionCode: String ->
        if (regionCode.isNotEmpty()) listOf("苗床A-01", "苗床A-02", "苗床A-03", "苗床B-01") else emptyList()
    }
    // 动态获取当前大棚的苗床列表
    val availableSeedbeds = remember(regionQrCode, regionSelfCode) {
        val currentRegion = if (regionSelfCode.isNotEmpty()) regionSelfCode else regionQrCode
        getSeedbedsByRegion(currentRegion)
    }

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

    // 智能判断当前处于哪个输入步骤，决定是否显示大卡片
    val showTopScanCard = when {
        inputMode == 0 -> true // 苗木只支持扫码，卡片常驻
        inputMode == 1 -> {
            if (region_type == "大棚" || region_type == "地块") {
                !isRegionSelfCodeMode // 大棚/地块第一步：显隐跟随其模式
            } else {
                true // 还没选区域类型时，默认显示
            }
        }
        else -> true
    }

    // 模拟扫码逻辑
    fun simulateScan() {
        if (isScanning || !showTopScanCard) return

        scope.launch {
            isScanning = true
            val msg = if (inputMode == 0) "正在识别苗木二维码..." else "正在识别区域二维码..."
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            delay(1500)
            isScanning = false

            if (inputMode == 0) {
                plantQrCode = "TREE-IRR-001"
            } else {
                isRegionSelfCodeMode = false
                plantation_name = "茂名核心种植园"
                region_type = "大棚"
                regionQrCode = "GH-A-01"

                // 【联动】：扫码成功获取大棚后，默认全选该大棚的苗床
                val newSeedbeds = getSeedbedsByRegion(regionQrCode)
                selectedSeedbeds.clear()
                selectedSeedbeds.addAll(newSeedbeds)
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
                        val isRegionValid = regionQrCode.isNotEmpty() || regionSelfCode.isNotEmpty()

                        if (inputMode == 0 && plantQrCode.isEmpty()) {
                            Toast.makeText(context, "请扫码提供苗木编码", Toast.LENGTH_SHORT).show()
                        } else if (inputMode == 1 && (plantation_name.isEmpty() || region_type.isEmpty() || !isRegionValid)) {
                            Toast.makeText(context, "请完整填写灌溉区域信息", Toast.LENGTH_SHORT).show()
                        } else if (inputMode == 1 && region_type == "大棚" && selectedSeedbeds.isEmpty()) {
                            Toast.makeText(context, "请至少选择一个苗床", Toast.LENGTH_SHORT).show()
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

            // 2. 顶部扫码区
            AnimatedVisibility(
                visible = showTopScanCard,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                TopScanCard(
                    isScanning = isScanning,
                    title = if (inputMode == 0) "点击扫描苗木二维码" else "点击扫描区域二维码",
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
                        // 【修改】：苗木锁死为二维码模式
                        DualModeIdentifierField(
                            targetName = "苗木",
                            qrCodeValue = plantQrCode,
                            onQrCodeChange = { plantQrCode = it },
                            selfCodeValue = "",
                            onSelfCodeChange = { },
                            isSelfCodeMode = false, // 永远为 false，保持扫码模式
                            onModeChange = { },
                            onScanClick = { simulateScan() },
                            showModeToggle = false  // 隐藏右上角切换按钮
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
                                selectedSeedbeds.clear()
                                isRegionSelfCodeMode = false
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
                                        selectedSeedbeds.clear()
                                        isRegionSelfCodeMode = false
                                    }
                                )
                            }
                        }

                        // 第三级：具体地块或大棚编号
                        AnimatedVisibility(visible = region_type.isNotEmpty()) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                // 【联动修改】：当手动输入大棚自编码时，也默认全选苗床
                                DualModeIdentifierField(
                                    targetName = region_type,
                                    qrCodeValue = regionQrCode,
                                    onQrCodeChange = { regionQrCode = it },
                                    selfCodeValue = regionSelfCode,
                                    onSelfCodeChange = {
                                        regionSelfCode = it
                                        val newSeedbeds = getSeedbedsByRegion(it)
                                        selectedSeedbeds.clear()
                                        selectedSeedbeds.addAll(newSeedbeds)
                                    },
                                    isSelfCodeMode = isRegionSelfCodeMode,
                                    onModeChange = { isRegionSelfCodeMode = it },
                                    onScanClick = { simulateScan() }
                                )
                            }
                        }

                        // 第四级：大棚下的苗床多选 (仅当类型为大棚时显示)
                        AnimatedVisibility(visible = region_type == "大棚") {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                val isRegionFilled = regionQrCode.isNotEmpty() || regionSelfCode.isNotEmpty()
                                if (isRegionFilled) {
                                    // 【修改】：使用专门的多选组件
                                    MultiSelectSeedbedDropdown(
                                        label = "选择苗床",
                                        options = availableSeedbeds,
                                        selectedOptions = selectedSeedbeds,
                                        onSelectionChange = { changedOption, isChecked ->
                                            if (isChecked) selectedSeedbeds.add(changedOption)
                                            else selectedSeedbeds.remove(changedOption)
                                        }
                                    )
                                } else {
                                    OutlinedTextField(
                                        value = "",
                                        onValueChange = {},
                                        label = { Text("选择苗床") },
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

// =================================================================
// ⬇️ 内部组件
// =================================================================

// 【修复后的多选下拉框组件】
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectSeedbedDropdown(
    label: String,
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChange: (String, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // 生成展示文本：如果全选则显示“已全选(x项)”，否则显示具体的项，超长用省略号
    val displayText = when {
        selectedOptions.isEmpty() -> "请选择苗床"
        selectedOptions.size == options.size -> "已全选 (${options.size}个苗床)"
        else -> selectedOptions.joinToString(", ")
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            singleLine = true, // 【关键修复】：直接使用 singleLine=true 即可自动实现结尾省略号
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary),
            modifier = Modifier.fillMaxWidth().menuAnchor()
            // 【删除了原本报错的 textStyle = LocalTextStyle... 】
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White).fillMaxWidth(0.9f)
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("暂无数据", color = Color.Gray) }, onClick = {})
            } else {
                options.forEach { option ->
                    val isChecked = selectedOptions.contains(option)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = null, // 交给 Row 点击处理
                                    colors = CheckboxDefaults.colors(checkedColor = AgGreenPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(option)
                            }
                        },
                        onClick = {
                            onSelectionChange(option, !isChecked)
                            // 注意：多选时不关闭下拉菜单，方便连续点击
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    )
                }
            }
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