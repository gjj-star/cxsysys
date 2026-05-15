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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cxsysys.ui.components.PhotoSourcePickerDialog
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import com.example.cxsysys.viewmodel.GrowthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// 引入公共组件
import com.example.cxsysys.ui.components.TopScanCard
import com.example.cxsysys.ui.components.DualModeIdentifierField
import com.example.cxsysys.ui.components.rememberCameraPhotoLauncher
import com.example.cxsysys.ui.components.ValidatedDropdownField
import com.example.cxsysys.ui.components.ValidatedDateField
import com.example.cxsysys.ui.components.ValidatedOutlinedTextField
import com.example.cxsysys.ui.components.rememberFormValidationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthEntryScreen(
    onBackClick: () -> Unit,
    viewModel: GrowthViewModel = viewModel()
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
    var inputMode by remember { mutableIntStateOf(0) } // 0-个别录入(苗木), 1-批量录入(地块)

    // 将自编码模式状态上提至父页面
    var isSelfCodeMode by remember { mutableStateOf(false) }

    // 苗木只有二维码状态
    var plantQrCode by remember { mutableStateOf("") }

    // 地块依然保留双模式
    var fieldQrCode by remember { mutableStateOf("") }
    var fieldSelfCode by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var recordDate by remember { mutableStateOf(dateFormat.format(Date())) }

    // 生长数据
    var treeHeight by remember { mutableStateOf("") }
    var groundDiameter by remember { mutableStateOf("") }
    var brestHeightDiameter by remember { mutableStateOf("") }
    var crownWidth by remember { mutableStateOf("") }
    var plantQuantity by remember { mutableStateOf("") }

    var straightness by remember { mutableStateOf("") }
    val straightnessOptions = listOf("1 级：通直", "2 级：轻度弯曲", "3 级：严重弯曲")

    var remark by remember { mutableStateOf("") }

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
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showScanner by remember { mutableStateOf(false) }

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

    // 真实扫码界面
    if (showScanner) {
        com.example.cxsysys.ui.components.ScannerScreen(
            onScanResult = { result ->
                if (inputMode == 0) {
                    plantQrCode = result
                } else {
                    fieldQrCode = result
                }
                showScanner = false
                Toast.makeText(context, "扫码成功", Toast.LENGTH_SHORT).show()
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
                    datePickerState.selectedDateMillis?.let { millis -> recordDate = dateFormat.format(Date(millis)) }
                    showDatePicker = false
                }) { Text("确定", color = AgGreenPrimary) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消", color = Color.Gray) } }
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
                title = { Text("生长记录录入", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        // 使用表单验证状态进行验证
                        val identifierValue = if (inputMode == 0) {
                            if (plantQrCode.isEmpty()) null else "valid"
                        } else {
                            if (fieldQrCode.isEmpty() && fieldSelfCode.isEmpty()) null else "valid"
                        }
                        
                        val isValid = validationState.validateOnSubmit(
                            mapOf(
                                "identifier" to identifierValue,
                                "recordDate" to recordDate,
                                "treeHeight" to treeHeight,
                                "groundDiameter" to groundDiameter,
                                "brestHeightDiameter" to brestHeightDiameter,
                                "crownWidth" to crownWidth,
                                "plantQuantity" to plantQuantity,
                                "straightness" to straightness
                            )
                        )

                        if (isValid) {
                            viewModel.submitGrowth(
                                context = context,
                                plantQrcode = if (inputMode == 0) plantQrCode else null,
                                fieldQrcode = if (inputMode == 1) fieldQrCode else null,
                                fieldCode = if (inputMode == 1) fieldSelfCode else null,
                                recordDate = recordDate,
                                height = treeHeight.toDoubleOrNull() ?: 0.0,
                                crownWidth = crownWidth.toDoubleOrNull() ?: 0.0,
                                diameter = groundDiameter.toDoubleOrNull() ?: 0.0,
                                chestDiameter = brestHeightDiameter.toDoubleOrNull() ?: 0.0,
                                straightness = straightness,
                                plantQuantity = plantQuantity.toIntOrNull() ?: 0,
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
            // 模式切换器
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
                ) { Text("个别录入 (苗木)", color = if (inputMode == 0) Color.White else Color.Gray, fontWeight = if (inputMode == 0) FontWeight.Bold else FontWeight.Normal) }
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().background(if (inputMode == 1) AgGreenPrimary else Color.Transparent).clickable { inputMode = 1 },
                    contentAlignment = Alignment.Center
                ) { Text("批量录入 (地块)", color = if (inputMode == 1) Color.White else Color.Gray, fontWeight = if (inputMode == 1) FontWeight.Bold else FontWeight.Normal) }
            }

            AnimatedVisibility(
                visible = if (inputMode == 0) true else !isSelfCodeMode,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                TopScanCard(
                    isScanning = false,
                    title = if (inputMode == 0) "点击扫描苗木二维码" else "点击扫描地块二维码",
                    subtitle = if (inputMode == 0) "直接录入苗木生长记录" else "批量录入地块生长记录",
                    onScanClick = { showScanner = true }
                )
            }

            Text("基础信息", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {

                    if (inputMode == 0) {
                        DualModeIdentifierField(
                            targetName = "苗木",
                            qrCodeValue = plantQrCode,
                            onQrCodeChange = { plantQrCode = it },
                            selfCodeValue = "",
                            onSelfCodeChange = { },
                            isSelfCodeMode = false,
                            onModeChange = { },
                            onScanClick = { showScanner = true },
                            showModeToggle = false,
                            validationState = validationState,
                            fieldKey = "identifier",
                            isRequired = true
                        )
                    } else {
                        DualModeIdentifierField(
                            targetName = "地块",
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

                    ValidatedDateField(
                        value = recordDate,
                        label = "记录日期",
                        fieldKey = "recordDate",
                        validationState = validationState,
                        isRequired = true,
                        onDateClick = { showDatePicker = true },
                        trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.CalendarToday, "选择日期", tint = AgGreenPrimary) } }
                    )
                }
            }

            Text("生长指标", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ValidatedOutlinedTextField(
                            value = treeHeight,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) treeHeight = it },
                            label = "树高",
                            fieldKey = "treeHeight",
                            validationState = validationState,
                            isRequired = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = { Text("cm", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) }
                        )
                        ValidatedOutlinedTextField(
                            value = groundDiameter,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) groundDiameter = it },
                            label = "地径",
                            fieldKey = "groundDiameter",
                            validationState = validationState,
                            isRequired = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = { Text("cm", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ValidatedOutlinedTextField(
                            value = brestHeightDiameter,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) brestHeightDiameter = it },
                            label = "胸径",
                            fieldKey = "brestHeightDiameter",
                            validationState = validationState,
                            isRequired = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = { Text("cm", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) }
                        )
                        ValidatedOutlinedTextField(
                            value = crownWidth,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) crownWidth = it },
                            label = "幅冠",
                            fieldKey = "crownWidth",
                            validationState = validationState,
                            isRequired = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = { Text("m", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ValidatedOutlinedTextField(
                        value = plantQuantity,
                        onValueChange = { if (it.all { c -> c.isDigit() }) plantQuantity = it },
                        label = "植株主干分枝数",
                        fieldKey = "plantQuantity",
                        validationState = validationState,
                        isRequired = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("个", color = Color.Gray, modifier = Modifier.padding(end = 12.dp)) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ValidatedDropdownField(
                        label = "主干通直度",
                        value = straightness,
                        placeholder = "请选择通直度等级",
                        options = straightnessOptions,
                        onValueChange = { straightness = it },
                        fieldKey = "straightness",
                        validationState = validationState,
                        isRequired = true
                    )
                }
            }

            Text("照片与补充说明", fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                GrowthPhotoUploadBox(onClick = { showPhotoSourcePicker = true })
                            }
                        }
                    } else {
                        GrowthPhotoUploadBox(onClick = { showPhotoSourcePicker = true })
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = remark, onValueChange = { remark = it }, label = { Text("生长情况描述 (选填)") }, placeholder = { Text("如：长势良好、叶片发黄等", color = Color.Gray) }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary))
                }
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun GrowthPhotoUploadBox(onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F5F5)).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AddAPhoto, contentDescription = "Upload", tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text("添加照片", color = Color.Gray, fontSize = 12.sp)
        }
    }
}
