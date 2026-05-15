package com.example.cxsysys.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary

/**
 * 双模式标识输入框组件 (纯展示与输入层，不包含自身状态)
 */
@Composable
fun DualModeIdentifierField(
    targetName: String,
    qrCodeValue: String,
    onQrCodeChange: (String) -> Unit,
    selfCodeValue: String,
    onSelfCodeChange: (String) -> Unit,
    isSelfCodeMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onScanClick: () -> Unit = {},
    showModeToggle: Boolean = true,
    validationState: FormValidationState? = null,
    fieldKey: String? = null,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val currentValue = if (isSelfCodeMode) selfCodeValue else qrCodeValue
    val hasError = validationState != null && fieldKey != null && validationState.hasError(fieldKey)

    val borderColor by animateColorAsState(
        targetValue = if (hasError) Color(0xFFE53935) else if (isSelfCodeMode) AgGreenPrimary else Color(0xFFE0E0E0),
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    // 自动清除错误：当值非空时
    LaunchedEffect(currentValue) {
        if (validationState != null && fieldKey != null && currentValue.isNotBlank() && hasError) {
            validationState.clearError(fieldKey)
        }
    }

    val handleToggle = {
        if (showModeToggle) {
            val newMode = !isSelfCodeMode
            onModeChange(newMode)
            if (newMode) onQrCodeChange("") else onSelfCodeChange("")
        }
    }

    Column(modifier = modifier) {
        // 必填标签行
        if (isRequired) {
            Row {
                Text(
                    text = "* ",
                    color = Color(0xFFE53935),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isSelfCodeMode) "${targetName}自编码" else "${targetName}二维码",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

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
            value = currentValue,
            onValueChange = {
                if (isSelfCodeMode) onSelfCodeChange(it)
            },
            readOnly = !isSelfCodeMode,
            label = if (!isRequired) {
                { Text(text = if (isSelfCodeMode) "${targetName}自编码" else "${targetName}二维码") }
            } else null,
            placeholder = {
                Text(
                    text = if (isSelfCodeMode) "请输入${targetName}自编码 (如: A-01)" else "请通过上方卡片扫描${targetName}二维码",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            isError = hasError,
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
                focusedBorderColor = borderColor,
                focusedLabelColor = if (hasError) Color(0xFFE53935) else AgGreenPrimary,
                unfocusedBorderColor = if (hasError) Color(0xFFE53935).copy(alpha = 0.7f) else if (isSelfCodeMode) Color.Black else Color(0xFFE0E0E0),
                errorBorderColor = Color(0xFFE53935),
                errorLabelColor = Color(0xFFE53935),
                errorCursorColor = Color(0xFFE53935),
                focusedTextColor = Color(0xFF333333),
                unfocusedTextColor = Color(0xFF333333),
                cursorColor = AgGreenPrimary
            ),
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp)
        )

        // 错误提示文字
        if (hasError) {
            Text(
                text = "此项为必填",
                color = Color(0xFFE53935),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 0.dp, top = 4.dp)
            )
        }
    }
}