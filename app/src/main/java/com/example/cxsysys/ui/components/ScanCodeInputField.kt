package com.example.cxsysys.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.cxsysys.ui.theme.AgGreenPrimary

/**
 * 通用扫码输入框组件
 * 提取自原有的 PunchingInputWithScanField 私有函数，完全保留原有UI和样式
 */
@Composable
fun ScanCodeInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onScanClick: () -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = modifier,
        trailingIcon = {
            IconButton(onClick = onScanClick) {
                Icon(Icons.Default.DocumentScanner, contentDescription = "Scan", tint = AgGreenPrimary)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AgGreenPrimary,
            focusedLabelColor = AgGreenPrimary
        )
    )
}