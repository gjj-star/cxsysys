package com.example.cxsysys.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary

/**
 * 双模式标识输入框组件 (纯展示与输入层，不包含自身状态)
 */
@Composable
fun DualModeIdentifierField(
    targetName: String, // 目标名称，如 "苗木" 或 "地块"
    qrCodeValue: String,
    onQrCodeChange: (String) -> Unit,
    selfCodeValue: String,
    onSelfCodeChange: (String) -> Unit,
    isSelfCodeMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onScanClick: () -> Unit = {}, // 【回归初心】：加回扫码回调，默认为空以兼容已有代码
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    // 引入 FocusRequester，用于让键盘 Icon 也能触发输入框聚焦
    val focusRequester = remember { FocusRequester() }

    // 仅用于文字按钮的切换逻辑
    val handleToggle = {
        val newMode = !isSelfCodeMode
        onModeChange(newMode)
        // 切换模式时清空另一个模式的数据，防止后端接收到脏数据
        if (newMode) onQrCodeChange("") else onSelfCodeChange("")
    }

    Column(modifier = modifier) {
        // 明确的文字引导切换按钮，放在右上角
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = if (isSelfCodeMode) "⇌ 切换扫描二维码模式" else "⇌ 切换输入自编码模式",
                fontSize = 13.sp,
                color = AgGreenPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { handleToggle() }
                    .padding(4.dp) // 增加点击区域防误触
            )
        }

        OutlinedTextField(
            value = if (isSelfCodeMode) selfCodeValue else qrCodeValue,
            onValueChange = {
                if (isSelfCodeMode) {
                    onSelfCodeChange(it)
                } else {
                    // 二维码模式保留注释，禁止手输
                    // onQrCodeChange(it)
                }
            },
            // 二维码模式下设为只读，阻止软键盘弹出
            readOnly = !isSelfCodeMode,

            label = {
                Text(text = if (isSelfCodeMode) "${targetName}自编码" else "${targetName}二维码")
            },
            placeholder = {
                Text(
                    text = if (isSelfCodeMode) "请输入${targetName}自编码 (如: A-01)" else "请通过上方卡片扫描${targetName}二维码",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester), // 绑定焦点请求器
            trailingIcon = {
                // 【核心修改】：根据模式渲染不同功能的专属图标
                if (isSelfCodeMode) {
                    // 自编码模式：显示键盘图标，点击请求焦点（等同于点击输入框本身，呼出软键盘）
                    IconButton(onClick = { focusRequester.requestFocus() }) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Show Keyboard",
                            tint = AgGreenPrimary
                        )
                    }
                } else {
                    // 二维码模式：显示扫码图标，点击执行扫码回调
                    IconButton(onClick = onScanClick) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR Code",
                            tint = AgGreenPrimary
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isSelfCodeMode) AgGreenPrimary else Color(0xFFE0E0E0),
                focusedLabelColor = AgGreenPrimary,
                unfocusedBorderColor = if (isSelfCodeMode) Color.Black else Color(0xFFE0E0E0)
            ),
            singleLine = true
        )
    }
}