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

// --- 数据模型 (模拟数据库表结构) ---

// 1. 供应商信息
data class Supplier(
    val id: Int,
    val name: String,
    val address: String,
    val phone: String,
    val type: String // 1-肥料供应商，2-农药供应商，3-肥料农药供应商
)

// 2. 肥料信息
data class Fertilizer(
    val id: Int,
    val supplierId: Int, // 关联供应商ID
    val name: String,
    val type: String,    // 有机肥/复合肥...
    val n: String,       // 氮
    val p: String,       // 磷
    val k: String,       // 钾
    val remark: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerEntryScreen(
    onBackClick: () -> Unit,
    onNavigateToFertilizerAdd: () -> Unit = {} // [新增] 用于跳转至肥料信息入库页面
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- 模拟数据库基础数据 ---
    val suppliers = remember { mutableStateListOf(
        Supplier(1, "茂名农资公司", "茂名市电白区人民路88号", "13800138000", "3-肥料农药供应商"),
        Supplier(2, "绿色生态肥业", "广州市天河区", "020-88888888", "1-肥料供应商")
    ) }
    val fertilizers = remember { mutableStateListOf(
        Fertilizer(1, 1, "高效复合肥", "复合肥", "15", "15", "15", "通用型"),
        Fertilizer(2, 2, "深海鱼蛋白", "有机肥", "5", "2", "1", "促进根系生长")
    ) }

    // --- 表单状态 ---
    // 【新增点】：将自编码模式状态上提至父页面
    var isSelfCodeMode by remember { mutableStateOf(false) }

    // 【修改点】：将原先的 field_id 拆分为二维码和自编码
    var fieldQrCode by remember { mutableStateOf("") }
    var fieldSelfCode by remember { mutableStateOf("") }

    val selectedFertilizers = remember { mutableStateListOf<Fertilizer>() }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var fertilizer_date by remember { mutableStateOf(dateFormat.format(Date())) }

    var time_slot by remember { mutableStateOf("") }
    var dosage_gram_per_plant by remember { mutableStateOf("") }
    var fertilizer_method by remember { mutableStateOf("") }
    var water_fertilizer by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }

    // UI 控制状态
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var isScanning by remember { mutableStateOf(false) }

    var showSelectFertilizerDialog by remember { mutableStateOf(false) }

    // 选项数据
    val timeSlotOptions = listOf("6-8时", "9-11时", "12-14时", "15-17时", "18-20时", "其他")
    val methodOptions = listOf("穴施", "沟施", "撒施", "环状施肥", "放射状施肥", "打洞填埋", "滴灌", "浇灌", "水肥一体化", "叶面施肥", "涂枝干", "其他")

    // 模拟扫码逻辑
    fun simulateScan() {
        scope.launch {
            isScanning = true
            Toast.makeText(context, "正在识别地块二维码...", Toast.LENGTH_SHORT).show()
            delay(1500)
            isScanning = false
            fieldQrCode = "FIELD-V10-B02" // 扫码成功填入二维码字段
            Toast.makeText(context, "扫码成功", Toast.LENGTH_SHORT).show()
        }
    }

    // 日历处理
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        fertilizer_date = dateFormat.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("确定", color = AgGreenPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消", color = Color.Gray) }
            }
        ) { DatePicker(state = datePickerState, showModeToggle = false) }
    }

    // 肥料选择弹窗逻辑更新
    if (showSelectFertilizerDialog) {
        SelectFertilizerDialog(
            suppliers = suppliers,
            fertilizers = fertilizers,
            onDismiss = { showSelectFertilizerDialog = false },
            onConfirm = { fertilizer ->
                if (!selectedFertilizers.any { it.id == fertilizer.id }) selectedFertilizers.add(fertilizer)
                showSelectFertilizerDialog = false
            },
            onAddClick = {
                showSelectFertilizerDialog = false
                onNavigateToFertilizerAdd()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("施肥作业录入", fontWeight = FontWeight.Bold) },
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
                        // 【修改点】：校验逻辑更新
                        if (fieldQrCode.isEmpty() && fieldSelfCode.isEmpty()) {
                            Toast.makeText(context, "请扫码或输入地块编码", Toast.LENGTH_SHORT).show()
                        } else if (selectedFertilizers.isEmpty()) {
                            Toast.makeText(context, "请至少选择一种肥料", Toast.LENGTH_SHORT).show()
                        } else if (dosage_gram_per_plant.isEmpty()) {
                            Toast.makeText(context, "请填写单株用量", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "保存成功！", Toast.LENGTH_LONG).show()
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
            // 1. 顶部扫码区 (加入平滑的收起动画)
            AnimatedVisibility(
                visible = !isSelfCodeMode,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                TopScanCard(
                    isScanning = isScanning,
                    title = "点击扫描地块二维码",
                    subtitle = "自动录入关联地块信息",
                    onScanClick = { simulateScan() }
                )
            }

            // 提示信息
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, null, tint = AgGreenPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("提示：若找不到所需的肥料，请在“选择肥料”中点击“新增”进行入库操作。", color = AgGreenPrimary, fontSize = 12.sp)
            }

            Text("作业基本信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 【修改点】：使用通用的双模式组件处理地块信息
                    DualModeIdentifierField(
                        targetName = "地块",
                        qrCodeValue = fieldQrCode,
                        onQrCodeChange = { fieldQrCode = it },
                        selfCodeValue = fieldSelfCode,
                        onSelfCodeChange = { fieldSelfCode = it },
                        isSelfCodeMode = isSelfCodeMode,
                        onModeChange = { isSelfCodeMode = it },
                        onScanClick = { simulateScan() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = fertilizer_date,
                        onValueChange = { fertilizer_date = it },
                        readOnly = true, // 防止点开弹出键盘
                        label = { Text("施肥日期") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, "选择日期", tint = AgGreenPrimary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FertilizerDropdownField(
                        label = "施肥时段",
                        selectedValue = time_slot,
                        options = timeSlotOptions,
                        onValueChange = { time_slot = it }
                    )
                }
            }

            Text("肥料与用量", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("肥料列表", fontWeight = FontWeight.Bold)
                        TextButton(onClick = { showSelectFertilizerDialog = true }) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Text("选择肥料", color = AgGreenPrimary)
                        }
                    }

                    if (selectedFertilizers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(BgGray, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                            Text("暂未选择肥料", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            selectedFertilizers.forEach { fert ->
                                val supplierName = suppliers.find { it.id == fert.supplierId }?.name ?: "未知厂商"
                                Row(modifier = Modifier.fillMaxWidth().border(1.dp, AgGreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(fert.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("$supplierName | ${fert.type}", color = Color.Gray, fontSize = 11.sp)
                                        Text("N:${fert.n} P:${fert.p} K:${fert.k}", color = Color(0xFF1976D2), fontSize = 11.sp)
                                    }
                                    IconButton(onClick = { selectedFertilizers.remove(fert) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = dosage_gram_per_plant,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) dosage_gram_per_plant = it },
                        label = { Text("单株用量") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("g/ml", modifier = Modifier.padding(end = 12.dp), color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FertilizerDropdownField(
                        label = "施用方法",
                        selectedValue = fertilizer_method,
                        options = methodOptions,
                        onValueChange = { fertilizer_method = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = water_fertilizer,
                        onValueChange = { water_fertilizer = it },
                        label = { Text("水肥配比") },
                        placeholder = { Text("如 5:1", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
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
                        placeholder = { Text("肥料配比、使用方法、植株反应等", color = Color.Gray) },
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
// ⬇️ 组件定义 (统一前缀以区分)
// =================================================================

// 【注】：原先手写的 FertilizerInputWithScanField 已经被删除，复用了统一样式组件

@Composable
fun SelectFertilizerDialog(
    suppliers: List<Supplier>,
    fertilizers: List<Fertilizer>,
    onDismiss: () -> Unit,
    onConfirm: (Fertilizer) -> Unit,
    onAddClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 300.dp, max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("选择要使用的肥料", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AgGreenPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    fertilizers.forEach { fertilizer ->
                        val supplierName = suppliers.find { it.id == fertilizer.supplierId }?.name ?: "未知厂商"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onConfirm(fertilizer) }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(fertilizer.name, fontWeight = FontWeight.Bold)
                                Text("$supplierName | ${fertilizer.type}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("N:${fertilizer.n} P:${fertilizer.p} K:${fertilizer.k}", color = Color(0xFF1976D2), fontSize = 11.sp)
                            }
                            Icon(Icons.Default.AddCircleOutline, null, tint = AgGreenPrimary)
                        }
                        HorizontalDivider(color = BgGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === 统一风格：三个等宽按钮 (顺序：新增 -> 删除 -> 取消) ===
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FertilizerDropdownField(label: String, selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
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