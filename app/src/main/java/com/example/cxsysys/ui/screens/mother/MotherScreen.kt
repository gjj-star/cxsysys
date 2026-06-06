package com.example.cxsysys.ui.screens.mother

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cxsysys.model.MotherTreeCreateRequest
import com.example.cxsysys.model.MotherTreeItem
import com.example.cxsysys.model.Subspecies
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import com.example.cxsysys.utils.QrCodeGenerator
import com.example.cxsysys.viewmodel.DictViewModel
import com.example.cxsysys.viewmodel.MotherTreeViewModel

// 母树状态颜色映射（标签从数据字典获取，颜色按值索引）
private val STATUS_COLORS = mapOf(0 to AgGreenPrimary, 1 to Color(0xFFFFA000), 2 to Color.Red)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotherScreen(onNavigateToDetail: (Int) -> Unit) {
    val context = LocalContext.current
    val viewModel: MotherTreeViewModel = viewModel()
    val dictViewModel: DictViewModel = viewModel()
    val treeList by viewModel.treeList.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val errorMsg by viewModel.errorMsg.collectAsState()
    val submitSuccess by viewModel.submitSuccess.collectAsState()
    val subspeciesList by viewModel.subspeciesList.collectAsState()
    val dictCache by dictViewModel.dictCache.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var qrCodeContent by remember { mutableStateOf<String?>(null) }

    // 首次加载
    LaunchedEffect(Unit) {
        viewModel.fetchTreeList()
        viewModel.fetchSubspeciesList()
        dictViewModel.preloadDicts("mothertree_status")
    }

    // 错误提示
    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // 新增成功提示
    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            Toast.makeText(context, "新增母树成功", Toast.LENGTH_SHORT).show()
            showAddDialog = false
            viewModel.clearSubmitSuccess()
        }
    }

    // 新增母树弹窗
    if (showAddDialog) {
        AddMotherTreeDialog(
            subspeciesList = subspeciesList,
            onDismiss = { showAddDialog = false },
            onSubmit = { request ->
                viewModel.createMotherTree(request)
            }
        )
    }

    // 二维码弹窗
    qrCodeContent?.let { content ->
        QrCodeDialog(
            content = content,
            onDismiss = { qrCodeContent = null }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("母树资源库", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = { viewModel.fetchTreeList(keyword = searchText.ifBlank { null }) }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AgGreenPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgGray)
                .padding(padding)
        ) {
            // 搜索栏
            SearchBar(
                value = searchText,
                onValueChange = { searchText = it },
                onSearch = {
                    viewModel.fetchTreeList(keyword = searchText.ifBlank { null })
                },
                modifier = Modifier.padding(16.dp)
            )

            when {
                isLoading && treeList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AgGreenPrimary)
                    }
                }
                treeList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Park,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("暂无母树数据", color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                }
                else -> {
                    val listState = rememberLazyListState()

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        items(
                            items = treeList,
                            key = { it.mothertreeId }
                        ) { tree ->
                            MotherTreeCard(
                                tree = tree,
                                statusLabel = dictViewModel.getLabel("mothertree_status", tree.status),
                                onClick = { onNavigateToDetail(tree.mothertreeId) },
                                onQrCodeClick = { qrCodeContent = tree.mothertreeQrcode }
                            )
                        }

                        // 加载更多指示器
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = AgGreenPrimary,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }

                        // 加载更多按钮
                        if (hasMore && !isLoadingMore) {
                            item {
                                TextButton(
                                    onClick = { viewModel.loadMore() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("加载更多", color = AgGreenPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("搜索母树编号、品种...", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AgGreenPrimary,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                focusManager.clearFocus()
            }
        )
    )
}

@Composable
fun MotherTreeCard(tree: MotherTreeItem, statusLabel: String, onClick: () -> Unit, onQrCodeClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：图标
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Park,
                    contentDescription = null,
                    tint = AgGreenPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 中间：信息
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tree.mothertreeQrcode, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (tree.dnaVerified) {
                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "DNA已认证",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("品种：${tree.subspeciesName}", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                Text("树龄：${tree.treeAge}年", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            // 右侧：状态指示
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    Icons.Default.QrCode2,
                    contentDescription = "QR",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onQrCodeClick() }
                )
                Spacer(modifier = Modifier.height(8.dp))
                val statusColor = STATUS_COLORS[tree.status] ?: Color.Gray
                Text(statusLabel, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMotherTreeDialog(
    subspeciesList: List<Subspecies>,
    onDismiss: () -> Unit,
    onSubmit: (MotherTreeCreateRequest) -> Unit
) {
    var subspeciesId by remember { mutableStateOf("") }
    var treeAge by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var subspeciesExpanded by remember { mutableStateOf(false) }

    val selectedSubspecies = subspeciesList.find { it.enterpriseSubspeciesId.toString() == subspeciesId }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("新增母树档案", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AgGreenPrimary)

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 品种选择下拉
                    ExposedDropdownMenuBox(
                        expanded = subspeciesExpanded,
                        onExpandedChange = { subspeciesExpanded = !subspeciesExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSubspecies?.subspeciesName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("母树品种细分") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subspeciesExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = subspeciesExpanded,
                            onDismissRequest = { subspeciesExpanded = false }
                        ) {
                            subspeciesList.forEach { subspecies ->
                                DropdownMenuItem(
                                    text = { Text("${subspecies.subspeciesName} (${subspecies.subspeciesCode})") },
                                    onClick = {
                                        subspeciesId = subspecies.enterpriseSubspeciesId.toString()
                                        subspeciesExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = treeAge, onValueChange = { treeAge = it },
                        label = { Text("母树树龄") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = longitude, onValueChange = { longitude = it },
                            label = { Text("经度") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = latitude, onValueChange = { latitude = it },
                            label = { Text("纬度") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    OutlinedTextField(
                        value = photoUrl, onValueChange = { photoUrl = it },
                        label = { Text("母树照片地址（多个用逗号分隔）") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("取消", color = Color.Gray) }
                    Button(
                        onClick = {
                            val sid = subspeciesId.toIntOrNull()
                            if (sid == null) return@Button
                            val photoUrls = photoUrl.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            onSubmit(
                                MotherTreeCreateRequest(
                                    subspeciesId = sid,
                                    treeAge = treeAge,
                                    longitude = longitude,
                                    latitude = latitude,
                                    photoUrl = photoUrls
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary)
                    ) { Text("保存") }
                }
            }
        }
    }
}

/**
 * 二维码展示弹窗：将 mothertree_qrcode 字符串生成二维码图片并居中显示
 */
@Composable
fun QrCodeDialog(content: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // 在后台线程生成二维码，避免主线程卡顿
    LaunchedEffect(content) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            qrBitmap = QrCodeGenerator.generate(content, 512)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("母树二维码", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AgGreenPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    val bitmap = qrBitmap
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(260.dp)
                        )
                    } else {
                        CircularProgressIndicator(color = AgGreenPrimary, modifier = Modifier.size(32.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) { Text("关闭") }
            }
        }
    }
}
