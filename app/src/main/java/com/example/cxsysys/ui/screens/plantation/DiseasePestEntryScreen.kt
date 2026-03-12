package com.example.cxsysys.ui.screens.plantation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // [修改] 添加 ExperimentalLayoutApi 以支持 FlowRow
@Composable
fun DiseasePestEntryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- 表单状态 ---
    // 0: 按苗木个别录入 (默认), 1: 按地块批量录入
    var inputMode by remember { mutableStateOf(0) }

    var plant_id by remember { mutableStateOf("") }
    var field_id by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // [新增] 虫害复选框状态
    val diseasePestTypes = listOf("蚜虫", "白粉虱", "螨虫", "叶斑病", "屌丝虫", "炭疽病", "卷叶虫", "黄野螟", "枯萎病", "天牛", "根结线虫", "根腐病", "其他")
    val selectedPests = remember { mutableStateListOf<String>() }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var record_date by remember { mutableStateOf(dateFormat.format(Date())) }

    // UI 状态
    var isScanning by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    // 模拟扫码
    fun simulateScan() {
        scope.launch {
            isScanning = true
            Toast.makeText(context, "正在识别苗木二维码...", Toast.LENGTH_SHORT).show()
            delay(1500)
            isScanning = false
            plant_id = "TREE-2023-PEST-001"
            Toast.makeText(context, "扫码成功", Toast.LENGTH_SHORT).show()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        record_date = dateFormat.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("确定", color = AgGreenPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消", color = Color.Gray) }
            }
        ) { DatePicker(state = datePickerState, showModeToggle = false) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("病虫害信息录入", fontWeight = FontWeight.Bold) },
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
                        // 根据模式进行校验
                        val isValid = if (inputMode == 0) {
                            plant_id.isNotEmpty()
                        } else {
                            field_id.isNotEmpty()
                        }

                        if (!isValid) {
                            val msg = if (inputMode == 0) "请填写苗木ID" else "请选择地块"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        } else {
                            val target = if (inputMode == 0) "苗木: $plant_id" else "地块: $field_id"
                            // [修改] Toast 显示选中的虫害
                            val pestInfo = if (selectedPests.isNotEmpty()) "虫害: ${selectedPests.joinToString(",")}" else "未选虫害"
                            Toast.makeText(context, "保存成功！\n$target\n$pestInfo", Toast.LENGTH_SHORT).show()
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
            // 1. 录入模式切换器
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
                        text = "个别录入 (扫码)",
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

            // 2. 根据模式动态显示内容
            if (inputMode == 0) {
                // --- 模式A: 按苗木个别录入 ---

                // 扫码卡片
                DiseaseScanSection(isScanning = isScanning, onScanClick = { simulateScan() })

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("关联苗木", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AgGreenPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        // plant_id 输入框
                        DiseaseInputWithScanField(
                            label = "苗木ID / 二维码",
                            value = plant_id,
                            onValueChange = { plant_id = it },
                            onScanClick = { simulateScan() },
                            placeholder = "扫码或手动输入"
                        )
                    }
                }
            } else {
                // --- 模式B: 按地块批量录入 ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("关联地块", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AgGreenPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        // field_id 下拉框
                        DiseaseDropdownField(
                            label = "选择地块 (field_id)",
                            value = field_id,
                            placeholder = "请选择发生病害的地块",
                            onClick = { Toast.makeText(context, "地块下拉框开发中", Toast.LENGTH_SHORT).show() }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 提示文字
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("选择地块后，该病虫害记录将关联至该地块下的所有苗木。", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. 公共字段 (日期、描述、照片)
            Text("详细信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // record_date
                    OutlinedTextField(
                        value = record_date,
                        onValueChange = { record_date = it },
                        label = { Text("记录日期") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, "选择日期", tint = AgGreenPrimary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // [新增] 虫害复选框区域
                    Text("主要病虫害（可多选）", fontWeight = FontWeight.Medium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        diseasePestTypes.forEach { pest ->
                            val isSelected = selectedPests.contains(pest)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) selectedPests.remove(pest) else selectedPests.add(pest)
                                },
                                label = { Text(pest) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AgGreenPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = AgGreenPrimary
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("病虫害描述") },
                        placeholder = { Text("请详细描述症状...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // photo_url
                    Text("病虫害照片 (支持多张)", fontWeight = FontWeight.Medium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    DiseasePhotoUploadBox(onClick = {
                        Toast.makeText(context, "图片上传功能开发中", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
    }
}

// === 内部组件 (前缀 Disease) ===

@Composable
private fun DiseaseScanSection(isScanning: Boolean, onScanClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { if (!isScanning) onScanClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)) // 深色背景
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
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "点击扫描苗木二维码",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "关联苗木ID",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiseaseInputWithScanField(label: String, value: String, onValueChange: (String) -> Unit, onScanClick: () -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) }, placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = { IconButton(onClick = onScanClick) { Icon(Icons.Default.QrCodeScanner, null, tint = AgGreenPrimary) } },
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary)
    )
}

@Composable
private fun DiseaseDropdownField(label: String, value: String, placeholder: String, onClick: () -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = {}, label = { Text(label) }, placeholder = { Text(placeholder) }, readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary),
        interactionSource = remember { MutableInteractionSource() }.also { src ->
            LaunchedEffect(src) { src.interactions.collect { if (it is PressInteraction.Release) onClick() } }
        }
    )
}

@Composable
private fun DiseasePhotoUploadBox(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5)).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray)
            Text("点击上传照片", color = Color.Gray, fontSize = 12.sp)
        }
    }
}