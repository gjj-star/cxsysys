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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
fun SupplierEntryContent(onSaveSuccess: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("1-肥料供应商") }
    val typeOptions = listOf("1-肥料供应商", "2-农药供应商", "3-肥料农药供应商")

    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("基础信息", fontWeight = FontWeight.Bold, color = AgGreenPrimary)

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("供应商名称") }, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = address, onValueChange = { address = it },
            label = { Text("地址") }, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phone, onValueChange = { phone = it },
            label = { Text("电话") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        AgDropdownField(label = "类型", selectedValue = type, options = typeOptions, onValueChange = { type = it })

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isNotEmpty()) {
                    Toast.makeText(context, "供应商 [$name] 保存成功！", Toast.LENGTH_SHORT).show()
                    onSaveSuccess()
                } else {
                    Toast.makeText(context, "请输入供应商名称", Toast.LENGTH_SHORT).show()
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
fun PesticideInfoEntryContent(onSaveSuccess: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 模拟的供应商列表（用于下拉选择）
    val dummySuppliers = listOf("茂名农资公司", "专业植保站", "利民化工")

    var supplierName by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var ingredient by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }

    // 生产日期
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var manufactureDate by remember { mutableStateOf(dateFormat.format(Date())) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

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
                AgDropdownField(
                    label = "生产厂家 (供应商)",
                    selectedValue = if(supplierName.isEmpty()) "" else supplierName,
                    options = dummySuppliers,
                    onValueChange = { supplierName = it }
                )

                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("农药名称") }, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ingredient, onValueChange = { ingredient = it },
                    label = { Text("成分说明") }, modifier = Modifier.fillMaxWidth()
                )

                // 生产日期
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = manufactureDate,
                        onValueChange = {},
                        label = { Text("生产日期") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = AgGreenPrimary) },
                        readOnly = true,
                        enabled = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AgGreenPrimary,
                            focusedLabelColor = AgGreenPrimary,
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = Color.Gray
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                }

                OutlinedTextField(
                    value = remark, onValueChange = { remark = it },
                    label = { Text("备注") }, modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 图片上传占位
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("外包装照片", fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.weight(1f).height(100.dp).background(BgGray, RoundedCornerShape(8.dp)).clickable {},
                        contentAlignment = Alignment.Center
                    ) { Text("+ 正面图", color = Color.Gray) }
                    Box(
                        modifier = Modifier.weight(1f).height(100.dp).background(BgGray, RoundedCornerShape(8.dp)).clickable {},
                        contentAlignment = Alignment.Center
                    ) { Text("+ 背面图", color = Color.Gray) }
                }
            }
        }

        Button(
            onClick = {
                if (supplierName.isNotEmpty() && name.isNotEmpty()) {
                    Toast.makeText(context, "农药 [$name] 入库成功！", Toast.LENGTH_SHORT).show()
                    onSaveSuccess()
                } else {
                    Toast.makeText(context, "请完善农药信息", Toast.LENGTH_SHORT).show()
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
fun FertilizerInfoEntryContent(onSaveSuccess: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val dummySuppliers = listOf("茂名农资公司", "绿色生态肥业", "云天化")
    val typeOptions = listOf("有机肥", "复合肥", "水溶肥", "缓释肥", "其他")

    var supplierName by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("复合肥") }
    var n by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }
    var k by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }

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

                AgDropdownField(
                    label = "生产厂家 (供应商)",
                    selectedValue = if(supplierName.isEmpty()) "" else supplierName,
                    options = dummySuppliers,
                    onValueChange = { supplierName = it }
                )

                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("肥料名称") }, modifier = Modifier.fillMaxWidth()
                )

                AgDropdownField(label = "肥料类型", selectedValue = type, options = typeOptions, onValueChange = { type = it })

                // 氮磷钾
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = n, onValueChange = { n = it },
                        label = { Text("N(氮)") },
                        placeholder = { Text("g/ml", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = p, onValueChange = { p = it },
                        label = { Text("P(磷)") },
                        placeholder = { Text("g/ml", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = k, onValueChange = { k = it },
                        label = { Text("K(钾)") },
                        placeholder = { Text("g/ml", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = remark, onValueChange = { remark = it },
                    label = { Text("备注") }, modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 图片上传占位
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("外包装照片", fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.weight(1f).height(100.dp).background(BgGray, RoundedCornerShape(8.dp)).clickable {},
                        contentAlignment = Alignment.Center
                    ) { Text("+ 正面图", color = Color.Gray) }
                    Box(
                        modifier = Modifier.weight(1f).height(100.dp).background(BgGray, RoundedCornerShape(8.dp)).clickable {},
                        contentAlignment = Alignment.Center
                    ) { Text("+ 背面图", color = Color.Gray) }
                }
            }
        }

        Button(
            onClick = {
                if (supplierName.isNotEmpty() && name.isNotEmpty()) {
                    Toast.makeText(context, "肥料 [$name] 入库成功！", Toast.LENGTH_SHORT).show()
                    onSaveSuccess()
                } else {
                    Toast.makeText(context, "请完善肥料信息", Toast.LENGTH_SHORT).show()
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
            modifier = Modifier.fillMaxWidth().menuAnchor()
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