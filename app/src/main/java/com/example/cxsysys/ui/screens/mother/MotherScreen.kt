package com.example.cxsysys.ui.screens.mother

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState // [新增] 导入滚动状态
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll // [新增] 导入滚动修饰符
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray

// 母树数据模型
data class MotherTree(
    val id: Int,
    val code: String,
    val species: String,
    val age: String,
    val location: String,
    val hasDna: Boolean,
    val status: String // 正常, 冻结, 死亡
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotherScreen(onNavigateToDetail: (String) -> Unit) { // [修改] 增加回调参数
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    // 模拟数据前缀改为 "母树-"
    val trees = listOf(
        MotherTree(1, "母树-2012-A001", "金丝油 (203)", "12年", "A区-核心育种基地", true, "正常"),
        MotherTree(2, "母树-2015-B088", "奇楠1号 (201)", "9年", "B区-种质资源库", true, "正常"),
        MotherTree(3, "母树-2018-C012", "虎斑 (205)", "6年", "C区-示范林", false, "冻结"),
        MotherTree(4, "母树-2010-A005", "糖结 (202)", "14年", "A区-核心育种基地", true, "正常"),
        MotherTree(5, "母树-2020-D099", "黑油 (208)", "4年", "D区-新培植区", false, "正常")
    )

    // 新增母树弹窗
    if (showAddDialog) {
        AddMotherTreeDialog(onDismiss = { showAddDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("母树资源库", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = { /* 筛选逻辑 */ }) {
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
                modifier = Modifier.padding(16.dp)
            )

            // 列表内容
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                items(trees.size) { index ->
                    val tree = trees[index]
                    MotherTreeCard(tree) {
                        onNavigateToDetail(tree.code) // [修改] 调用跳转
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
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
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
    )
}

@Composable
fun MotherTreeCard(tree: MotherTree, onClick: () -> Unit) {
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
            // 左侧：图片/图标
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
                    Text(tree.code, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    // DNA 认证徽章
                    if (tree.hasDna) {
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
                Text("品种：${tree.species}", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                Text("树龄：${tree.age} | 位置：${tree.location}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            // 右侧：状态指示
            Column(horizontalAlignment = Alignment.End) {
                val statusColor = if (tree.status == "正常") AgGreenPrimary else Color.Red
                Icon(Icons.Default.QrCode2, contentDescription = "QR", tint = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(tree.status, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun AddMotherTreeDialog(onDismiss: () -> Unit) {
    // [修改] 为满足V10需要补充具体状态变量
    var qrCode by remember { mutableStateOf("") }
    var dnaBarcode by remember { mutableStateOf("") }
    var subspeciesId by remember { mutableStateOf("") }
    var treeAge by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }

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

                // [修改] 增加 verticalScroll 防止字段过多导致屏幕装不下
                Column(
                    modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = qrCode, onValueChange = { qrCode = it },
                        label = { Text("母树二维码") },
                        placeholder = { Text("自动生成或扫码") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.QrCodeScanner, null) } // 保留扫描功能
                    )

                    OutlinedTextField(
                        value = dnaBarcode, onValueChange = { dnaBarcode = it }, // [新增]
                        label = { Text("母树DNA条形码") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = subspeciesId, onValueChange = { subspeciesId = it },
                        label = { Text("母树品种细分") }, // [修改] (原“品种细分”改成“母树品种细分”)
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = treeAge, onValueChange = { treeAge = it },
                        label = { Text("母树树龄") }, // [修改] (原“树龄”改成“母树树龄”)
                        modifier = Modifier.fillMaxWidth()
                    )

                    // [修改] 将原来的“经纬度”拆分成单独的经度和纬度
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = longitude, onValueChange = { longitude = it }, label = { Text("经度") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = latitude, onValueChange = { latitude = it }, label = { Text("纬度") }, modifier = Modifier.weight(1f))
                    }

                    OutlinedTextField(
                        value = photoUrl, onValueChange = { photoUrl = it }, // [新增]
                        label = { Text("母树照片地址") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("取消", color = Color.Gray) }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary)
                    ) { Text("保存") }
                }
            }
        }
    }
}