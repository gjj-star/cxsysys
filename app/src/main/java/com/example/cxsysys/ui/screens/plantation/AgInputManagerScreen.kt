package com.example.cxsysys.ui.screens.plantation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cxsysys.viewmodel.AgInputViewModel
import com.example.cxsysys.viewmodel.DictViewModel
import com.example.cxsysys.viewmodel.SubmitState
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import android.net.Uri
import android.provider.OpenableColumns
import java.io.FileOutputStream
import java.io.InputStream
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.cxsysys.ui.components.PhotoSourcePickerDialog
import com.example.cxsysys.ui.components.rememberCameraPhotoLauncher
import com.example.cxsysys.ui.components.rememberFormValidationState
import com.example.cxsysys.ui.components.ValidatedOutlinedTextField
import com.example.cxsysys.ui.components.ValidatedDropdownField

// 辅助函数：将 Uri 转换为临时的 File 对象
fun uriToFile(context: android.content.Context, uri: Uri): File? {
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(uri, null, null, null, null)
    var fileName = "temp_image_${System.currentTimeMillis()}.jpg"
    if (cursor != null && cursor.moveToFirst()) {
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1) {
            val originalName = cursor.getString(nameIndex)
            // 简单的安全过滤，防止路径穿越
            if (originalName != null && !originalName.contains("/") && !originalName.contains("\\")) {
                fileName = originalName
            }
        }
        cursor.close()
    }
    
    val tempFile = File(context.cacheDir, fileName)
    try {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

/**
 * 药肥基础信息管理页面
 * 包含：供应商录入、农药信息入库、肥料信息入库
 * @param mode 模式: "supplier", "pesticide", "fertilizer"
 */
@OptIn(ExperimentalMaterial3Api::class) // [修复] 添加注解以支持 TopAppBar
@Composable
fun AgInputManagerScreen(mode: String, onBackClick: () -> Unit) {
    val title = when (mode) {
        "supplier" -> "供应商信息录入"
        "pesticide" -> "农药信息入库"
        "fertilizer" -> "肥料信息入库"
        else -> "信息录入"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BgGray
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (mode) {
                "supplier" -> SupplierEntryContent(onSaveSuccess = onBackClick)
                "pesticide" -> PesticideInfoEntryContent(onSaveSuccess = onBackClick)
                "fertilizer" -> FertilizerInfoEntryContent(onSaveSuccess = onBackClick)
            }
        }
    }
}

// ------------------------------------------------------------------------
// 1. 供应商信息录入内容
// ------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierEntryContent(onSaveSuccess: () -> Unit, viewModel: AgInputViewModel = viewModel()) {
    val context = LocalContext.current
    val dictViewModel: DictViewModel = viewModel()
    val dictCache by dictViewModel.dictCache.collectAsState()
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("1-肥料供应商") }
    val typeOptions = dictViewModel.getOptions("supplier_type").ifEmpty { listOf("1-肥料供应商", "2-农药供应商", "3-肥料农药供应商") }
    val validationState = rememberFormValidationState()

    LaunchedEffect(Unit) {
        dictViewModel.preloadDicts("supplier_type")
    }

    val submitState by viewModel.submitState.collectAsState()

    LaunchedEffect(submitState) {
        when (submitState) {
            is SubmitState.Success -> {
                Toast.makeText(context, "供应商 [$name] 保存成功！", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onSaveSuccess()
            }
            is SubmitState.Error -> {
                Toast.makeText(context, (submitState as SubmitState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("基础信息", fontWeight = FontWeight.Bold, color = AgGreenPrimary)

        ValidatedOutlinedTextField(
            value = name, onValueChange = { name = it },
            label = "供应商名称", fieldKey = "name",
            validationState = validationState, isRequired = true
        )
        ValidatedOutlinedTextField(
            value = address, onValueChange = { address = it },
            label = "地址", fieldKey = "address",
            validationState = validationState, isRequired = true
        )
        ValidatedOutlinedTextField(
            value = phone, onValueChange = { phone = it },
            label = "电话", fieldKey = "phone",
            validationState = validationState, isRequired = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        ValidatedDropdownField(
            label = "类型", value = type, placeholder = "请选择类型",
            options = typeOptions, onValueChange = { type = it },
            fieldKey = "type", validationState = validationState, isRequired = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val isValid = validationState.validateOnSubmit(
                    mapOf("name" to name, "address" to address, "phone" to phone, "type" to type)
                )
                if (isValid) {
                    val typeCode = type.split("-")[0].toIntOrNull() ?: 1
                    viewModel.submitSupplier(name, address, phone, typeCode)
                } else {
                    Toast.makeText(context, "请补全必填信息", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary)
        ) {
            Text("保存", fontSize = 16.sp)
        }
    }
}

// ------------------------------------------------------------------------
// 2. 农药信息入库内容
// ------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesticideInfoEntryContent(onSaveSuccess: () -> Unit, viewModel: AgInputViewModel = viewModel()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val supplierList by viewModel.supplierList.collectAsState()
    val supplierOptions = supplierList.map { it.supplierName }
    
    LaunchedEffect(Unit) {
        viewModel.fetchPesticideSuppliers()
    }

    val validationState = rememberFormValidationState()
    var supplierName by remember { mutableStateOf("") }
    var supplierId by remember { mutableStateOf<Int?>(null) }
    var name by remember { mutableStateOf("") }
    var ingredient by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }

    // 生产日期
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var manufactureDate by remember { mutableStateOf(dateFormat.format(Date())) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    // 图片选择器
    var frontImageUri by remember { mutableStateOf<Uri?>(null) }
    var backImageUri by remember { mutableStateOf<Uri?>(null) }
    var photoTarget by remember { mutableStateOf<String?>(null) } // "front" or "back"
    var showPhotoSourcePicker by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            when (photoTarget) {
                "front" -> frontImageUri = uri
                "back" -> backImageUri = uri
            }
        }
    }

    val cameraPhotoLauncher = rememberCameraPhotoLauncher { uri ->
        when (photoTarget) {
            "front" -> frontImageUri = uri
            "back" -> backImageUri = uri
        }
    }

    val submitState by viewModel.submitState.collectAsState()

    LaunchedEffect(submitState) {
        when (submitState) {
            is SubmitState.Success -> {
                Toast.makeText(context, "农药 [$name] 入库成功！", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onSaveSuccess()
            }
            is SubmitState.Error -> {
                Toast.makeText(context, (submitState as SubmitState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    PhotoSourcePickerDialog(
        visible = showPhotoSourcePicker,
        onDismiss = { showPhotoSourcePicker = false },
        onTakePhoto = { cameraPhotoLauncher() },
        onPickImages = { galleryLauncher.launch("image/*") }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        manufactureDate = dateFormat.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("确定", color = AgGreenPrimary) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消", color = Color.Gray) } }
        ) { DatePicker(state = datePickerState, showModeToggle = false) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("农药详情", fontWeight = FontWeight.Bold, color = AgGreenPrimary)

                // 供应商选择
                ValidatedDropdownField(
                    label = "生产厂家 (供应商)",
                    value = supplierName.ifEmpty { "" },
                    placeholder = "请选择供应商",
                    options = supplierOptions,
                    onValueChange = { selected ->
                        supplierName = selected
                        supplierId = supplierList.find { it.supplierName == selected }?.supplierId
                    },
                    fieldKey = "supplier",
                    validationState = validationState,
                    isRequired = true
                )

                ValidatedOutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = "农药名称", fieldKey = "pestName",
                    validationState = validationState, isRequired = true
                )
                ValidatedOutlinedTextField(
                    value = ingredient, onValueChange = { ingredient = it },
                    label = "成分说明", fieldKey = "ingredient",
                    validationState = validationState, isRequired = true
                )

                // 生产日期
                val hasMfgDateError = validationState.hasError("mfgDate")
                val mfgBorderColor by animateColorAsState(
                    targetValue = if (hasMfgDateError) Color(0xFFE53935) else AgGreenPrimary,
                    animationSpec = tween(durationMillis = 200),
                    label = "mfgBorderColor"
                )
                Text(
                    text = "* 生产日期",
                    color = Color(0xFFE53935),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = manufactureDate,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = AgGreenPrimary) },
                        readOnly = true,
                        enabled = true,
                        isError = hasMfgDateError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = mfgBorderColor,
                            unfocusedBorderColor = if (hasMfgDateError) Color(0xFFE53935) else Color.LightGray,
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = Color.Gray
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                }
                if (hasMfgDateError) {
                    Text("此项为必填", color = Color(0xFFE53935), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                OutlinedTextField(
                    value = remark, onValueChange = { remark = it },
                    label = { Text("备注") }, modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 图片上传占位
        val hasImageError = validationState.hasError("images")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (hasImageError) {
                    Text("* 外包装照片（正面图+背面图必填）", fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                } else {
                    Text("外包装照片", fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 正面图
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgGray, RoundedCornerShape(8.dp))
                            .clickable {
                                photoTarget = "front"
                                showPhotoSourcePicker = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (frontImageUri != null) {
                            AsyncImage(
                                model = frontImageUri,
                                contentDescription = "正面图",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray)
                                Text("正面图", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                    // 背面图
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgGray, RoundedCornerShape(8.dp))
                            .clickable {
                                photoTarget = "back"
                                showPhotoSourcePicker = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (backImageUri != null) {
                            AsyncImage(
                                model = backImageUri,
                                contentDescription = "背面图",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray)
                                Text("背面图", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
                if (hasImageError) {
                    Text("此项为必填", color = Color(0xFFE53935), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        Button(
            onClick = {
                val isValid = validationState.validateOnSubmit(
                    mapOf(
                        "supplier" to if (supplierId != null) "valid" else null,
                        "pestName" to name,
                        "ingredient" to ingredient,
                        "mfgDate" to manufactureDate
                    )
                )
                val imagesOk = frontImageUri != null && backImageUri != null
                if (!imagesOk) validationState.setError("images", true)
                else validationState.clearError("images")

                if (isValid && imagesOk) {
                    val frontFile = frontImageUri?.let { uriToFile(context, it) }
                    val backFile = backImageUri?.let { uriToFile(context, it) }
                    viewModel.submitPesticide(
                        supplierId = supplierId!!,
                        pestName = name,
                        ingredient = ingredient,
                        manufactureDate = manufactureDate,
                        remark = remark,
                        frontPhotoFile = frontFile,
                        backPhotoFile = backFile
                    )
                } else {
                    Toast.makeText(context, "请补全必填信息", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary)
        ) {
            Text("保存入库", fontSize = 16.sp)
        }
    }
}

// ------------------------------------------------------------------------
// 3. 肥料信息入库内容
// ------------------------------------------------------------------------
@Composable
fun FertilizerInfoEntryContent(onSaveSuccess: () -> Unit, viewModel: AgInputViewModel = viewModel()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val dictViewModel: DictViewModel = viewModel()
    val dictCache by dictViewModel.dictCache.collectAsState()

    val supplierList by viewModel.supplierList.collectAsState()
    val supplierOptions = supplierList.map { it.supplierName }
    
    LaunchedEffect(Unit) {
        viewModel.fetchFertilizerSuppliers()
        dictViewModel.preloadDicts("fertilizer_type")
    }

    val typeOptions = dictViewModel.getOptions("fertilizer_type").ifEmpty { listOf("有机肥", "复合肥", "水溶肥", "缓释肥", "其他") }
    val validationState = rememberFormValidationState()

    var supplierName by remember { mutableStateOf("") }
    var supplierId by remember { mutableStateOf<Int?>(null) }
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("复合肥") }
    var n by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }
    var k by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }

    // 图片选择器
    var frontImageUri by remember { mutableStateOf<Uri?>(null) }
    var backImageUri by remember { mutableStateOf<Uri?>(null) }
    var photoTarget by remember { mutableStateOf<String?>(null) } // "front" or "back"
    var showPhotoSourcePicker by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            when (photoTarget) {
                "front" -> frontImageUri = uri
                "back" -> backImageUri = uri
            }
        }
    }

    val cameraPhotoLauncher = rememberCameraPhotoLauncher { uri ->
        when (photoTarget) {
            "front" -> frontImageUri = uri
            "back" -> backImageUri = uri
        }
    }

    val submitState by viewModel.submitState.collectAsState()

    LaunchedEffect(submitState) {
        when (submitState) {
            is SubmitState.Success -> {
                Toast.makeText(context, "肥料 [$name] 入库成功！", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onSaveSuccess()
            }
            is SubmitState.Error -> {
                Toast.makeText(context, (submitState as SubmitState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    PhotoSourcePickerDialog(
        visible = showPhotoSourcePicker,
        onDismiss = { showPhotoSourcePicker = false },
        onTakePhoto = { cameraPhotoLauncher() },
        onPickImages = { galleryLauncher.launch("image/*") }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("肥料详情", fontWeight = FontWeight.Bold, color = AgGreenPrimary)

                ValidatedDropdownField(
                    label = "生产厂家 (供应商)",
                    value = supplierName.ifEmpty { "" },
                    placeholder = "请选择供应商",
                    options = supplierOptions,
                    onValueChange = { selected ->
                        supplierName = selected
                        supplierId = supplierList.find { it.supplierName == selected }?.supplierId
                    },
                    fieldKey = "supplier",
                    validationState = validationState,
                    isRequired = true
                )

                ValidatedOutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = "肥料名称", fieldKey = "fertName",
                    validationState = validationState, isRequired = true
                )

                ValidatedDropdownField(
                    label = "肥料类型", value = type, placeholder = "请选择类型",
                    options = typeOptions, onValueChange = { type = it },
                    fieldKey = "fertType", validationState = validationState, isRequired = true
                )

                // 氮磷钾
                Text(
                    text = "* 氮磷钾含量",
                    color = Color(0xFFE53935),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ValidatedOutlinedTextField(
                        value = n, onValueChange = { n = it },
                        label = "N(氮)", fieldKey = "nutrientN",
                        validationState = validationState, isRequired = true,
                        placeholder = "g/ml",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    ValidatedOutlinedTextField(
                        value = p, onValueChange = { p = it },
                        label = "P(磷)", fieldKey = "nutrientP",
                        validationState = validationState, isRequired = true,
                        placeholder = "g/ml",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    ValidatedOutlinedTextField(
                        value = k, onValueChange = { k = it },
                        label = "K(钾)", fieldKey = "nutrientK",
                        validationState = validationState, isRequired = true,
                        placeholder = "g/ml",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = remark, onValueChange = { remark = it },
                    label = { Text("备注") }, modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 图片上传占位
        val hasImageError = validationState.hasError("images")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (hasImageError) {
                    Text("* 外包装照片（正面图+背面图必填）", fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                } else {
                    Text("外包装照片", fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 正面图
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgGray, RoundedCornerShape(8.dp))
                            .clickable {
                                photoTarget = "front"
                                showPhotoSourcePicker = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (frontImageUri != null) {
                            AsyncImage(
                                model = frontImageUri,
                                contentDescription = "正面图",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray)
                                Text("正面图", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                    // 背面图
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgGray, RoundedCornerShape(8.dp))
                            .clickable {
                                photoTarget = "back"
                                showPhotoSourcePicker = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (backImageUri != null) {
                            AsyncImage(
                                model = backImageUri,
                                contentDescription = "背面图",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray)
                                Text("背面图", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
                if (hasImageError) {
                    Text("此项为必填", color = Color(0xFFE53935), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        Button(
            onClick = {
                val isValid = validationState.validateOnSubmit(
                    mapOf(
                        "supplier" to if (supplierId != null) "valid" else null,
                        "fertName" to name,
                        "fertType" to type,
                        "nutrientN" to n,
                        "nutrientP" to p,
                        "nutrientK" to k
                    )
                )
                val imagesOk = frontImageUri != null && backImageUri != null
                if (!imagesOk) validationState.setError("images", true)
                else validationState.clearError("images")

                if (isValid && imagesOk) {
                    val frontFile = frontImageUri?.let { uriToFile(context, it) }
                    val backFile = backImageUri?.let { uriToFile(context, it) }
                    val nDouble = n.toDoubleOrNull() ?: 0.0
                    val pDouble = p.toDoubleOrNull() ?: 0.0
                    val kDouble = k.toDoubleOrNull() ?: 0.0
                    viewModel.submitFertilizer(
                        supplierId = supplierId!!,
                        fertName = name,
                        fertType = type,
                        nutrientN = nDouble,
                        nutrientP = pDouble,
                        nutrientK = kDouble,
                        remark = remark,
                        frontPhotoFile = frontFile,
                        backPhotoFile = backFile
                    )
                } else {
                    Toast.makeText(context, "请补全必填信息", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary)
        ) {
            Text("保存入库", fontSize = 16.sp)
        }
    }
}

// 通用组件：下拉选择框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgDropdownField(label: String, selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgGreenPrimary, focusedLabelColor = AgGreenPrimary),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onValueChange(option); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}