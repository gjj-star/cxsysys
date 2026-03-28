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

// 引入提取的公共组件
import com.example.cxsysys.ui.components.TopScanCard
import com.example.cxsysys.ui.components.DualModeIdentifierField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestEntryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- 表单状态 ---
    // 录入模式：0-个别录入(苗木), 1-批量录入(地块)。默认为1
    var inputMode by remember { mutableIntStateOf(1) }

    // 将自编码模式状态上提至父页面 (仅地块会用到)
    var isSelfCodeMode by remember { mutableStateOf(false) }

    // 【修改】：删除了打孔枝干相关状态，苗木只有二维码状态
    var plantQrCode by remember { mutableStateOf("") }

    // 地块依然保持双模式
    var fieldQrCode by remember { mutableStateOf("") }
    var fieldSelfCode by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var harvest_date by remember { mutableStateOf(dateFormat.format(Date())) }

    // 采收信息
    var weight by remember { mutableStateOf("") } // 采收重量

    var remark by remember { mutableStateOf("") }

    // UI 控制状态
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

            // 数据回填
            if (inputMode == 0) {
                plantQrCode = "HARVEST-TREE-001"
            } else {
                fieldQrCode = "HARVEST-FIELD-A01"
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
                        harvest_date = dateFormat.format(Date(millis))
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
                title = { Text("采收香木录入", fontWeight = FontWeight.Bold) },
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
                        // 【修改点】：校验逻辑更新，移除了枝干的判断
                        val isTargetValid = if (inputMode == 0) {
                            plantQrCode.isNotEmpty()
                        } else {
                            fieldQrCode.isNotEmpty() || fieldSelfCode.isNotEmpty()
                        }

                        if (!isTargetValid) {
                            val msg = if (inputMode == 0) "请扫码提供苗木标识信息" else "请扫码或输入地块编码"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        } else if (weight.isEmpty()) {
                            Toast.makeText(context, "请输入采收重量", Toast.LENGTH_SHORT).show()
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
                        text = "个别录入 (苗木)", // 恢复成纯苗木
                        color = if (inputMode == 0) Color.White else Color.Gray,
                        fontWeight = if (inputMode == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
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
                        fontWeight = if (inputMode == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }

            // 2. 顶部扫码区 (加入平滑的收起动画)
            val scanTitle = if (inputMode == 0) "点击扫描苗木二维码" else "点击扫描地块二维码"
            val scanSubtitle = if (inputMode == 0) "录入整株苗木采收记录" else "批量录入关联地块采收记录"

            // 判断是否需要显示顶部大卡片。因为个别录入(苗木)不支持自编码，所以 inputMode == 0 时常驻
            val shouldShowScanCard = if (inputMode == 0) true else !isSelfCodeMode

            AnimatedVisibility(
                visible = shouldShowScanCard,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                TopScanCard(
                    isScanning = isScanning,
                    title = scanTitle,
                    subtitle = scanSubtitle,
                    onScanClick = { simulateScan() }
                )
            }

            Text("作业基本信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // 根据模式动态显示不同输入结构
                    if (inputMode == 0) {
                        // 个别模式：直接锁死为苗木二维码模式
                        DualModeIdentifierField(
                            targetName = "苗木",
                            qrCodeValue = plantQrCode,
                            onQrCodeChange = { plantQrCode = it },
                            selfCodeValue = "",
                            onSelfCodeChange = { },
                            isSelfCodeMode = false, // 永远为 false，保持扫码模式
                            onModeChange = { },     // 不响应切换
                            onScanClick = { simulateScan() },
                            showModeToggle = false  // 隐藏右上角的切换按钮
                        )
                    } else {
                        // 批量模式：地块保持双模式
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 采收日期
                    OutlinedTextField(
                        value = harvest_date,
                        onValueChange = { harvest_date = it },
                        readOnly = true, // 防止键盘弹起
                        label = { Text("采收日期") },
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

            Text("香木信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 采收重量
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) weight = it },
                        label = { Text("采收重量") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = { Text("g/克", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) },
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
                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        label = { Text("备注 (选填)") },
                        placeholder = { Text("采收方法、香木形态等描述", color = Color.Gray) },
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