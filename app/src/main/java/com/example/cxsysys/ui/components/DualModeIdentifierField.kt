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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    onScanClick: () -> Unit = {}, // 预设扫码回调
    showModeToggle: Boolean = true, // 【新增】：是否允许切换模式，设为 false 时固定在当前模式并隐藏切换按钮
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val handleToggle = {
        if (showModeToggle) {
            val newMode = !isSelfCodeMode
            onModeChange(newMode)
            if (newMode) onQrCodeChange("") else onSelfCodeChange("")
        }
    }

    Column(modifier = modifier) {
        // 【修改】：根据参数决定是否渲染右上角的切换文字
        if (showModeToggle) {
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
                        .padding(4.dp)
                )
            }
        }

        OutlinedTextField(
            value = if (isSelfCodeMode) selfCodeValue else qrCodeValue,
            onValueChange = {
                if (isSelfCodeMode) onSelfCodeChange(it)
            },
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
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            trailingIcon = {
                if (isSelfCodeMode) {
                    IconButton(onClick = {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }) {
                        Icon(imageVector = Icons.Default.Keyboard, contentDescription = "Show Keyboard", tint = AgGreenPrimary)
                    }
                } else {
                    IconButton(onClick = onScanClick) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan QR Code", tint = AgGreenPrimary)
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