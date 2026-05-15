package com.example.cxsysys.ui.screens.plantation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cxsysys.ui.components.DualModeIdentifierField
import com.example.cxsysys.ui.components.PhotoSourcePickerDialog
import com.example.cxsysys.ui.components.TopScanCard
import com.example.cxsysys.ui.components.rememberCameraPhotoLauncher
import com.example.cxsysys.ui.components.ValidatedDropdownField
import com.example.cxsysys.ui.components.ValidatedOutlinedTextField
import com.example.cxsysys.ui.components.rememberFormValidationState
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import com.example.cxsysys.viewmodel.DiseaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiseasePestEntryScreen(
    onBackClick: () -> Unit,
    viewModel: DiseaseViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // --- ViewModel 状态 ---
    val isLoading by viewModel.isLoading.collectAsState()
    val submitSuccess by viewModel.submitSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 表单验证状态
    val validationState = rememberFormValidationState()

    // --- 表单状态 ---
    // 0: 按苗木个别录入 (默认), 1: 按地块批量录入
    var inputMode by remember { mutableStateOf(0) }

    // 将自编码模式状态上提至父页面
    var isSelfCodeMode by remember { mutableStateOf(false) }

    // 苗木只有二维码状态
    var plant_qr_code by remember { mutableStateOf("") }

    // 地块保持双模式
    var field_qr_code by remember { mutableStateOf("") }
    var field_self_code by remember { mutableStateOf("") }

    var description by remember { mutableStateOf("") }

    // 虫害复选框状态
    val diseasePestTypes = listOf("蚜虫", "白粉虱", "螨虫", "叶斑病", "屌丝虫", "炭疽病", "卷叶虫", "黄野螟", "枯萎病", "天牛", "根结线虫", "根腐病", "其他")
    val selectedPests = remember { mutableStateListOf<String>() }
    var hasPestError by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var record_date by remember { mutableStateOf(dateFormat.format(Date())) }

    // 图片选择状态
    val selectedImageUris = remember { mutableStateListOf<Uri>() }
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            selectedImageUris.addAll(uris)
        }
    )
    var showPhotoSourcePicker by remember { mutableStateOf(false) }
    val cameraPhotoLauncher = rememberCameraPhotoLauncher { uri ->
        selectedImageUris.add(uri)
    }

    // UI 状态
    var isScanning by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    // 监听提交结果
    LaunchedEffect(submitSuccess, errorMessage) {
        if (submitSuccess == true) {
            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onBackClick()
        } else if (submitSuccess == false && errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    // 真实扫码状态
    var showScanner by remember { mutableStateOf(false) }

    if (showScanner) {
        com.example.cxsysys.ui.components.ScannerScreen(
            onScanResult = { result ->
                showScanner = false
                if (inputMode == 0) {
                    plant_qr_code = result
                } else {
                    field_qr_code = result
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

    PhotoSourcePickerDialog(
        visible = showPhotoSourcePicker,
        onDismiss = { showPhotoSourcePicker = false },
        onTakePhoto = { cameraPhotoLauncher() },
        onPickImages = { multiplePhotoPickerLauncher.launch("image/*") }
    )

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
                        // 使用表单验证状态进行验证
                        val identifierValue = if (inputMode == 0) {
                            if (plant_qr_code.isEmpty()) null else "valid"
                        } else {
                            if (field_qr_code.isEmpty() && field_self_code.isEmpty()) null else "valid"
                        }
                        
                        val isValid = validationState.validateOnSubmit(
                            mapOf(
                                "identifier" to identifierValue,
                                "recordDate" to record_date,
                                "pestTypes" to if (selectedPests.isEmpty()) null else "valid"
                            )
                        )
                        hasPestError = selectedPests.isEmpty()

                        if (isValid && !hasPestError) {
                            viewModel.submitDisease(
                                context = context,
                                plantQrcode = if (inputMode == 0) plant_qr_code else null,
                                fieldQrcode = if (inputMode == 1) field_qr_code else null,
                                fieldCode = if (inputMode == 1) field_self_code else null,
                                recordDate = record_date,
                                diseaseType = selectedPests.joinToString(","),
                                diseaseDescription = description,
                                imageUris = selectedImageUris
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
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("提交中...", fontSize = 16.sp)
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

            // 2. 顶部扫码大图区
            AnimatedVisibility(
                visible = if (inputMode == 0) true else !isSelfCodeMode,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                TopScanCard(
                    isScanning = isScanning,
                    title = if (inputMode == 0) "点击扫描苗木二维码" else "点击扫描地块二维码",
                    subtitle = if (inputMode == 0) "关联苗木ID" else "关联地块编码",
                    onScanClick = { showScanner = true }
                )
            }

            // 3. 根据模式动态显示内容
            if (inputMode == 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("关联苗木", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AgGreenPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        DualModeIdentifierField(
                            targetName = "苗木",
                            qrCodeValue = plant_qr_code,
                            selfCodeValue = "",
                            onQrCodeChange = { plant_qr_code = it },
                            onSelfCodeChange = { },
                            isSelfCodeMode = false,
                            onModeChange = { },
                            onScanClick = { showScanner = true },
                            showModeToggle = false,
                            validationState = validationState,
                            fieldKey = "identifier",
                            isRequired = true
                        )
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("关联地块", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AgGreenPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        DualModeIdentifierField(
                            targetName = "定植地块",
                            qrCodeValue = field_qr_code,
                            selfCodeValue = field_self_code,
                            onQrCodeChange = { field_qr_code = it },
                            onSelfCodeChange = { field_self_code = it },
                            isSelfCodeMode = isSelfCodeMode,
                            onModeChange = { isSelfCodeMode = it },
                            onScanClick = { showScanner = true },
                            validationState = validationState,
                            fieldKey = "identifier",
                            isRequired = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("选择地块后，该病虫害记录将关联至该地块下的所有苗木。", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 4. 公共字段 (日期、描述、照片)
            Text("详细信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 记录日期
                    val hasDateError = validationState.hasError("recordDate")
                    Row {
                        Text(text = "* ", color = Color(0xFFE53935), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(text = "记录日期", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (hasDateError) Color(0xFFE53935) else Color(0xFF666666))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = record_date,
                        onValueChange = { record_date = it },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = hasDateError,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, "选择日期", tint = AgGreenPrimary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (hasDateError) Color(0xFFE53935) else AgGreenPrimary,
                            unfocusedBorderColor = if (hasDateError) Color(0xFFE53935) else Color.LightGray,
                            focusedLabelColor = if (hasDateError) Color(0xFFE53935) else AgGreenPrimary
                        )
                    )
                    if (hasDateError) {
                        Text("此项为必填", color = Color(0xFFE53935), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 虫害复选框区域
                    Row {
                        Text(text = "* ", color = Color(0xFFE53935), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(text = "主要病虫害（可多选）", fontWeight = FontWeight.Medium, color = if (hasPestError) Color(0xFFE53935) else Color(0xFF666666))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = if (hasPestError) Modifier
                            .border(1.dp, Color(0xFFE53935), RoundedCornerShape(8.dp))
                            .padding(8.dp) else Modifier
                    ) {
                        diseasePestTypes.forEach { pest ->
                            val isSelected = selectedPests.contains(pest)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) selectedPests.remove(pest) else selectedPests.add(pest)
                                    if (hasPestError && selectedPests.isNotEmpty()) hasPestError = false
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
                    if (hasPestError) {
                        Text("此项为必填", color = Color(0xFFE53935), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
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
                    
                    if (selectedImageUris.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(selectedImageUris) { uri ->
                                Box(modifier = Modifier.size(100.dp)) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Selected Image",
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { selectedImageUris.remove(uri) },
                                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, "Remove", tint = Color.Red)
                                    }
                                }
                            }
                            item {
                                DiseasePhotoUploadBox(onClick = { showPhotoSourcePicker = true })
                            }
                        }
                    } else {
                        DiseasePhotoUploadBox(onClick = { showPhotoSourcePicker = true })
                    }
                }
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

// === 内部组件 ===

@Composable
private fun DiseasePhotoUploadBox(onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5)).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray)
            Text("添加照片", color = Color.Gray, fontSize = 12.sp)
        }
    }
}
