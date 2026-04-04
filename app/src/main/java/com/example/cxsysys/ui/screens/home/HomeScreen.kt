package com.example.cxsysys.ui.screens.home

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cxsysys.model.WeatherResponse
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import com.example.cxsysys.viewmodel.HomeViewModel

// 打印模块入口类名：使用字符串避免在关闭打印模块时出现编译期依赖
private const val PRINTER_ACTIVITY_CLASS = "com.example.printerfeature.MainActivity"
private const val EXTRA_TARGET_TEMPLATE = "target_template"
private const val TEMP_MM = "苗木二维码"
private const val TEMP_CJG = "加工二维码"
private const val TEMP_CP = "产成品二维码"
private const val TEMP_DP = "大棚二维码"
private const val TEMP_MC = "苗床二维码"
private const val TEMP_DK = "地块二维码"

/**
 * 首页/工作台
 * @param onNavigateToModule 回调函数：(模块名称) -> Unit
 * 点击任意模块直接触发此回调，交由 MainScreen 处理跳转
 */
@Composable
fun HomeScreen(
    onNavigateToModule: (String) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current

    // 观察天气数据状态
    val weatherData by homeViewModel.weatherData.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()

    // 进入页面时自动获取天气
    LaunchedEffect(Unit) {
        homeViewModel.fetchWeather()
    }

    // 控制病虫防治弹窗状态
    var showPestDialog by remember { mutableStateOf(false) }
    // 控制药肥信息弹窗状态
    var showAgInputDialog by remember { mutableStateOf(false) }
    // [新增] 控制二维码生成弹窗状态 (用于控制二维码生成卡片显示)
    var showQrDialog by remember { mutableStateOf(false) }

    // 定义功能模块数据
    val actionItems = listOf(
        ActionItem("幼苗培育", Icons.Default.Grass, Color(0xFFE8F5E9), AgGreenPrimary, "sapling_entry"),
        ActionItem("苗木定植", Icons.Default.Park, Color(0xFFE3F2FD), Color(0xFF1976D2), "planting_entry"),
        ActionItem("生长记录", Icons.Default.Timeline, Color(0xFFE3F2FD), Color(0xFF1565C0), "growth_entry"),
        ActionItem("施肥作业", Icons.Default.Spa, Color(0xFFFFF3E0), Color(0xFFFFA000), "fertilizer_entry"),
        ActionItem("病虫防治", Icons.Default.BugReport, Color(0xFFFFEBEE), Color(0xFFD32F2F), "disease_dialog"),
        ActionItem("灌溉记录", Icons.Default.WaterDrop, Color(0xFFE0F7FA), Color(0xFF0097A7), "irrigation_entry"),
        ActionItem("剪枝记录", Icons.Default.ContentCut, Color(0xFFFBE9E7), Color(0xFFFF5722), "pruning_entry"),
        ActionItem("打孔结香", Icons.Default.BlurOn, Color(0xFFECEFF1), Color(0xFF607D8B), "punch_entry"),
        ActionItem("采收香木", Icons.Default.Forest, Color(0xFFEFEBE9), Color(0xFF795548), "harvest_entry"),
        ActionItem("药肥信息", Icons.Default.LocalPharmacy, Color(0xFFE1F5FE), Color(0xFF0288D1), "ag_input_dialog"),
        // [新增] 二维码生成模块 (入口项定义)
        //更名为二维码打印模块
        ActionItem("二维码打印", Icons.Default.QrCode, Color(0xFFE8F5E9), AgGreenPrimary, "qr_code_dialog")
    )

    // 1. 病虫防治选择弹窗 - UI优化版
    if (showPestDialog) {
        AlertDialog(
            onDismissRequest = { showPestDialog = false },
            containerColor = Color.White,
            title = null,
            icon = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "请选择您要执行的操作:",
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )

                    MenuOptionCard(
                        title = "病虫害信息",
                        subtitle = "记录植株病虫害详情及照片",
                        icon = Icons.Default.BugReport,
                        backgroundColor = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF2E7D32),
                        onClick = {
                            showPestDialog = false
                            onNavigateToModule("disease_pest_entry")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MenuOptionCard(
                        title = "施药作业",
                        subtitle = "记录农药使用及施药方式",
                        icon = Icons.Default.Science,
                        backgroundColor = Color(0xFFFFF3E0),
                        contentColor = Color(0xFFEF6C00),
                        onClick = {
                            showPestDialog = false
                            onNavigateToModule("pesticide_entry")
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showPestDialog = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("取消", color = Color(0xFF666666), fontSize = 16.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    // 2. 药肥信息选择弹窗
    if (showAgInputDialog) {
        AlertDialog(
            onDismissRequest = { showAgInputDialog = false },
            containerColor = Color.White,
            title = null,
            icon = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "请选择要录入的基础信息:",
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )

                    MenuOptionCard(
                        title = "供应商信息",
                        subtitle = "管理肥料与农药的供应商",
                        icon = Icons.Default.Store,
                        backgroundColor = Color(0xFFE0F2F1),
                        contentColor = Color(0xFF00695C),
                        onClick = {
                            showAgInputDialog = false
                            onNavigateToModule("ag_input_supplier")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MenuOptionCard(
                        title = "农药信息",
                        subtitle = "农药入库与成分管理",
                        icon = Icons.Default.Science,
                        backgroundColor = Color(0xFFF3E5F5),
                        contentColor = Color(0xFF7B1FA2),
                        onClick = {
                            showAgInputDialog = false
                            onNavigateToModule("ag_input_pesticide")
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MenuOptionCard(
                        title = "肥料信息",
                        subtitle = "肥料入库与类型管理",
                        icon = Icons.Default.Spa,
                        backgroundColor = Color(0xFFFFF8E1),
                        contentColor = Color(0xFFFF8F00),
                        onClick = {
                            showAgInputDialog = false
                            onNavigateToModule("ag_input_fertilizer")
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showAgInputDialog = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("取消", color = Color(0xFF666666), fontSize = 16.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    // [修改] 3. 二维码生成选择弹窗 - 对应到模块页面
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            containerColor = Color.White,
            title = null,
            icon = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "请选择您要执行的操作:",
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )

                    MenuOptionCard(
                        title = "地块二维码",
                        subtitle = "为特定种植区域打印溯源码",
                        icon = Icons.Default.LocationOn,
                        backgroundColor = Color(0xFFE1F5FE),
                        contentColor = Color(0xFF1565C0),
                        onClick = {
                            showQrDialog = false
                            launchPrinterFeature(context, TEMP_DK)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MenuOptionCard(
                        title = "大棚二维码",
                        subtitle = "为生产大棚打印唯一识别码",
                        icon = Icons.Default.Domain,
                        backgroundColor = Color(0xFFF3E5F5),
                        contentColor = Color(0xFF6A1B9A),
                        onClick = {
                            showQrDialog = false
                            launchPrinterFeature(context, TEMP_DP)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MenuOptionCard(
                        title = "苗床二维码",
                        subtitle = "为育苗区域打印溯源识别码",
                        icon = Icons.Default.GridView,
                        backgroundColor = Color(0xFFFFEBEE),
                        contentColor = Color(0xFFD32F2F),
                        onClick = {
                            showQrDialog = false
                            launchPrinterFeature(context, TEMP_MC)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MenuOptionCard(
                        title = "苗木二维码",
                        subtitle = "为苗木打印全生命周期码",
                        icon = Icons.Default.Park,
                        backgroundColor = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF2E7D32),
                        onClick = {
                            showQrDialog = false
                            launchPrinterFeature(context, TEMP_MM)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MenuOptionCard(
                        title = "加工二维码",
                        subtitle = "为加工过程打印溯源码",
                        icon = Icons.Default.Build,
                        backgroundColor = Color(0xFFFFF3E0),
                        contentColor = Color(0xFFEF6C00),
                        onClick = {
                            showQrDialog = false
                            launchPrinterFeature(context, TEMP_CJG)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MenuOptionCard(
                        title = "产成品二维码",
                        subtitle = "为产成品打印最终溯源码",
                        icon = Icons.Default.Inventory2,
                        backgroundColor = Color(0xFFE0F7FA),
                        contentColor = Color(0xFF00838F),
                        onClick = {
                            showQrDialog = false
                            launchPrinterFeature(context, TEMP_CP)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showQrDialog = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("取消", color = Color(0xFF666666), fontSize = 16.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BgGray).padding(16.dp)
    ) {
        WeatherCard(
            weatherData = weatherData,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRetry = { homeViewModel.fetchWeather() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "农事作业",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(actionItems) { item ->
                ActionCard(item) {
                    when (item.routeId) {
                        "disease_dialog" -> showPestDialog = true
                        "ag_input_dialog" -> showAgInputDialog = true
                        // [新增] 拦截二维码生成点击
                        "qr_code_dialog" -> showQrDialog = true
                        else -> onNavigateToModule(item.routeId)
                    }
                }
            }
        }
    }
}

// === 自定义组件 ===

@Composable
fun MenuOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = contentColor)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 12.sp, color = contentColor.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun WeatherCard(
    weatherData: WeatherResponse?,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit
) {
    val weatherIcon: ImageVector = when {
        weatherData == null && isLoading -> Icons.Default.Refresh
        errorMessage != null -> Icons.Default.CloudOff
        else -> when (weatherData?.weather) {
            "晴", "晴间多云" -> Icons.Default.WbSunny
            "多云", "阴" -> Icons.Default.Cloud
            "小雨", "阵雨", "雷阵雨", "雷阵雨伴有冰雹" -> Icons.Default.Grain
            "中雨", "大雨", "暴雨", "大暴雨", "特大暴雨" -> Icons.Default.WaterDrop
            "小雪", "中雪", "大雪", "暴雪" -> Icons.Default.AcUnit
            "雾", "霾" -> Icons.Default.VisibilityOff
            else -> Icons.Default.WbSunny
        }
    }

    val title = when {
        isLoading -> "正在获取天气..."
        errorMessage != null -> "天气获取失败"
        weatherData != null -> "今日天气 · ${weatherData.city}种植基地"
        else -> "今日天气 · 茂名种植基地"
    }

    val temperature = when {
        isLoading -> "--°C  加载中"
        errorMessage != null || weatherData == null -> "--°C  暂无数据"
        else -> "${weatherData.temperature}°C  ${weatherData.weather}"
    }

    val detail = when {
        isLoading -> "正在连接服务器..."
        errorMessage != null -> "点击卡片重试"
        weatherData != null -> "湿度 ${weatherData.humidity}% | ${weatherData.windDirection}${weatherData.windPower}"
        else -> "-- | --"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = AgGreenPrimary),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(
                if (errorMessage != null) Modifier.clickable { onRetry() }
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = temperature, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = detail, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            }

            Icon(imageVector = weatherIcon, contentDescription = "Weather", tint = Color.White, modifier = Modifier.size(56.dp))
        }
    }
}

@Composable
fun ActionCard(item: ActionItem, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(90.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(item.bgColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = item.icon, contentDescription = null, tint = item.iconColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = item.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
        }
    }
}

data class ActionItem(
    val name: String,
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color,
    val routeId: String
)

private fun launchPrinterFeature(context: android.content.Context, template: String) {
    val intent = Intent().apply {
        setClassName(context, PRINTER_ACTIVITY_CLASS)
        putExtra(EXTRA_TARGET_TEMPLATE, template)
    }

    val canHandleIntent = intent.resolveActivity(context.packageManager) != null
    if (canHandleIntent) {
        context.startActivity(intent)
    }
}
