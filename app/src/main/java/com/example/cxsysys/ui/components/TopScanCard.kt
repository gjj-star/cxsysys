package com.example.cxsysys.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary

/**
 * 顶部大块扫码引导卡片组件
 * 提取自打孔结香页面的 PunchingScanSection 蓝图
 * * @param isScanning 是否正在显示“识别中”的加载动画
 * @param title 卡片主标题（例如：“点击扫描苗木二维码”）
 * @param subtitle 卡片副标题（例如：“直接录入苗木打孔结香信息”）
 * @param onScanClick 点击卡片触发的扫码回调
 * @param modifier 样式修饰符（默认高度180dp，与蓝图保持一致）
 */
@Composable
fun TopScanCard(
    isScanning: Boolean,
    title: String,
    subtitle: String,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
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
                        modifier = Modifier.size(56.dp) // 采用 Punch 页面的大尺寸
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = subtitle,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}