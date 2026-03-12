package com.example.cxsysys.ui.screens.children

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 农事阶段枚举
enum class FarmingStage(val label: String) {
    Growth("生长记录"),
    Fertilizer("施肥作业"),
    Disease("病虫害"),
    Pesticide("施药信息"),
    Irrigation("灌溉记录"),
    Pruning("剪枝修整"),
    Punch("打孔结香"),
    Harvest("采收香木")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantingScreen(onNavigateToDetail: (String) -> Unit) {
    val context = LocalContext.current

    // --- 状态管理 ---
    var searchText by remember { mutableStateOf("") }

    // 级联筛选状态
    var selectedField by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedStage by remember { mutableStateOf<FarmingStage?>(null) } // 阶段

    // 批量选择状态 (存储选中的记录ID)
    var selectedBatchIds by remember { mutableStateOf(setOf<String>()) }

    // 日期选择器
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // 辅助状态
    val isStageLevel = selectedStage != null // 是否筛选到了"阶段"等级

    // 重置所有筛选
    fun resetFilters() {
        selectedField = null
        selectedDate = null
        selectedStage = null
        searchText = ""
        selectedBatchIds = emptySet() // 清空选中
    }

    // 处理日期回调
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        selectedDate = dateFormat.format(Date(millis))
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
            Column(modifier = Modifier.background(Color.White)) {
                CenterAlignedTopAppBar(
                    title = { Text("苗木档案管理", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                    actions = {
                        IconButton(onClick = { Toast.makeText(context, "导出当前视图报表", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Print, contentDescription = "Export")
                        }
                    }
                )

                // 1. 顶部检索框
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(50.dp)
                        .background(BgGray, RoundedCornerShape(25.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchText.isEmpty()) {
                            Text("扫描树牌或输入苗木编码...", color = Color.Gray)
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "启动扫码...", Toast.LENGTH_SHORT).show()
                        searchText = "苗木-A-001"
                    }) {
                        Icon(Icons.Default.QrCodeScanner, null, tint = AgGreenPrimary)
                    }
                }

                // 2. 级联筛选组件区 (美化版 - 等宽切割)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp), // 减小间距，让组件更紧凑
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // (1) 地块选择 (Weight 1f)
                    StyledFilterChip(
                        label = selectedField ?: "地块",
                        isActive = true,
                        isSelected = selectedField != null,
                        modifier = Modifier.weight(1f),
                        options = listOf("A区-01", "A区-02", "A区-03", "B区-01"),
                        onOptionSelected = {
                            selectedField = it
                            selectedDate = null
                            selectedStage = null
                            selectedBatchIds = emptySet()
                        }
                    )

                    // (2) 日期选择 (Weight 1f)
                    StyledFilterButton(
                        label = selectedDate ?: "日期",
                        isActive = selectedField != null,
                        isSelected = selectedDate != null,
                        modifier = Modifier.weight(1f),
                        onClick = { showDatePicker = true }
                    )

                    // (3) 阶段选择 (Weight 1f)
                    StyledFilterChip(
                        label = selectedStage?.label ?: "阶段",
                        isActive = selectedDate != null,
                        isSelected = selectedStage != null,
                        modifier = Modifier.weight(1f),
                        options = FarmingStage.entries.map { it.label },
                        onOptionSelected = { label ->
                            selectedStage = FarmingStage.entries.find { it.label == label }
                            selectedBatchIds = emptySet()
                        }
                    )

                    // (4) 重置按钮 (固定宽度，不参与平分)
                    if (selectedField != null || searchText.isNotEmpty()) {
                        IconButton(
                            onClick = { resetFilters() },
                            modifier = Modifier.width(32.dp) // 固定宽度，避免挤压
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.Gray)
                        }
                    }
                }

                HorizontalDivider(color = BgGray, thickness = 1.dp)
            }
        },
        floatingActionButton = {
            // 批量修改 FAB
            ExtendedFloatingActionButton(
                onClick = {
                    if (isStageLevel) {
                        if (selectedBatchIds.isEmpty()) {
                            Toast.makeText(context, "请先勾选需要修改的记录", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "对选中的 ${selectedBatchIds.size} 条 [${selectedStage?.label}] 记录进行批量修改", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "无法批量修改，请先筛选至“阶段”", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = if (isStageLevel) AgGreenPrimary else Color.Gray,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Edit, null) },
                text = { Text("批量修改${if (selectedBatchIds.isNotEmpty()) " (${selectedBatchIds.size})" else ""}") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgGray)
                .padding(padding)
        ) {
            if (isStageLevel) {
                // 模式 B: 显示具体操作记录 (支持批量勾选)
                OperationList(
                    stage = selectedStage!!,
                    field = selectedField!!,
                    date = selectedDate!!,
                    selectedIds = selectedBatchIds,
                    onToggleSelect = { id ->
                        selectedBatchIds = if (selectedBatchIds.contains(id)) {
                            selectedBatchIds - id
                        } else {
                            selectedBatchIds + id
                        }
                    }
                )
            } else {
                // 模式 A: 显示苗木列表 (不可勾选)
                ForestList(
                    searchText = searchText,
                    filterField = selectedField,
                    onItemClick = onNavigateToDetail
                )
            }
        }
    }
}

// === 列表视图组件 ===

@Composable
fun ForestList(searchText: String, filterField: String?, onItemClick: (String) -> Unit) {
    // 模拟数据
    val allTrees = listOf(
        ForestTree("苗木-A-001", "金丝油", "3.5米", 0, "A区-03", "2020-05-01"),
        ForestTree("苗木-A-002", "金丝油", "3.2米", 0, "A区-03", "2020-05-01"),
        ForestTree("苗木-B-088", "奇楠1号", "2.8米", 1, "B区-01", "2021-03-12") // 1=冻结
    )

    val filteredTrees = allTrees.filter { tree ->
        (searchText.isEmpty() || tree.code.contains(searchText)) &&
                (filterField == null || tree.location.contains(filterField))
    }

    if (filteredTrees.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到符合条件的苗木", color = Color.Gray)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if(filterField == null) "所有苗木档案" else "筛选结果: $filterField",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
            items(filteredTrees.size) { index ->
                ForestTreeCard(filteredTrees[index], onClick = { onItemClick(filteredTrees[index].code) })
            }
        }
    }
}

@Composable
fun OperationList(
    stage: FarmingStage,
    field: String,
    date: String,
    selectedIds: Set<String>,
    onToggleSelect: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = AgGreenPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("当前显示：$field · $date · ${stage.label} 记录", color = AgGreenPrimary, fontSize = 12.sp)
                }
            }
        }

        items(5) { index ->
            val uniqueId = "op-$index"
            val treeCode = "苗木-${field.takeLast(2)}-00${index + 1}"

            OperationCard(
                treeCode = treeCode,
                stage = stage,
                detail = when(stage) {
                    FarmingStage.Fertilizer -> "复合肥 50g | 穴施"
                    FarmingStage.Disease -> "发现卷叶虫 | 轻度"
                    FarmingStage.Pruning -> "疏剪 | 3枝"
                    else -> "常规作业记录"
                },
                isSelected = selectedIds.contains(uniqueId),
                onToggleSelect = { onToggleSelect(uniqueId) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(60.dp)) // 避让FAB
        }
    }
}

// === 卡片组件 ===

data class ForestTree(
    val code: String,
    val species: String,
    val height: String,
    val status: Int,
    val location: String,
    val date: String
)

@Composable
fun ForestTreeCard(item: ForestTree, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(BgGray, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Forest, null, tint = Color.DarkGray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(item.code, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    val (statusText, statusColor) = when(item.status) {
                        0 -> "正常" to AgGreenPrimary
                        1 -> "冻结" to Color(0xFFFFA000)
                        else -> "死亡" to Color.Red
                    }
                    Text(statusText, color = statusColor, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("${item.species} | 树高 ${item.height}", fontSize = 13.sp, color = Color.Gray)
                Text("位置：${item.location}", fontSize = 13.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun OperationCard(
    treeCode: String,
    stage: FarmingStage,
    detail: String,
    isSelected: Boolean,
    onToggleSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if(isSelected) Color(0xFFF1F8E9) else Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if(isSelected) 2.dp else 1.dp),
        border = if(isSelected) androidx.compose.foundation.BorderStroke(1.dp, AgGreenPrimary) else null,
        modifier = Modifier.clickable { onToggleSelect() }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(checkedColor = AgGreenPrimary)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(treeCode, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(stage.label, fontSize = 12.sp, color = AgGreenPrimary, fontWeight = FontWeight.Medium)
                Text(detail, fontSize = 13.sp, color = Color.Gray)
            }

            IconButton(onClick = { /* 单独修改逻辑 */ }) {
                Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// === 美化后的筛选组件 (样式微调) ===

@Composable
fun StyledFilterChip(
    label: String,
    isActive: Boolean,
    isSelected: Boolean,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) { // modifier 已应用 weight(1f)
        StyledFilterButton(
            label = label,
            isActive = isActive,
            isSelected = isSelected,
            hasDropdown = true,
            onClick = { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun StyledFilterButton(
    label: String,
    isActive: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    hasDropdown: Boolean = false,
    modifier: Modifier = Modifier // 接收外部传入的 modifier (weight)
) {
    val backgroundColor = when {
        isSelected -> AgGreenPrimary.copy(alpha = 0.1f)
        isActive -> Color.Transparent
        else -> Color(0xFFF5F5F5)
    }

    val borderColor = when {
        isSelected -> AgGreenPrimary
        isActive -> Color.Gray
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> AgGreenPrimary
        isActive -> Color.Black
        else -> Color.LightGray
    }

    Surface(
        modifier = modifier // 这里只应用 weight，不应有 padding 或 size 限制
            .height(36.dp)
            .clickable(enabled = isActive) { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp) // 内部 padding 减小
        ) {
            Text(
                text = label,
                fontSize = 12.sp, // 字体稍微缩小以适应等分
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (hasDropdown) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}