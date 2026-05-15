package com.example.cxsysys.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary

/**
 * 必填字段标签组件 - 在标签文字后追加红色星号 *
 *
 * @param label 标签文字
 * @param modifier 修饰符
 */
@Composable
fun RequiredFieldLabel(
    label: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text(
            text = "* ",
            color = Color(0xFFE53935),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 表单验证状态管理
 *
 * 用于跟踪表单字段的验证状态，在用户点击提交时触发验证。
 * 如果某个必填字段为空，对应的 errorState 会被设置为 true，
 * 文本框边框会变为红色。
 */
@Stable
class FormValidationState {
    // 存储每个字段的错误状态 (字段标识 -> 是否有错误)
    private val _errorStates = mutableStateMapOf<String, Boolean>()

    // 是否已经触发过提交验证
    var hasSubmitted by mutableStateOf(false)
        private set

    /**
     * 检查指定字段是否有错误
     */
    fun hasError(fieldKey: String): Boolean {
        return _errorStates[fieldKey] ?: false
    }

    /**
     * 设置字段错误状态
     */
    fun setError(fieldKey: String, hasError: Boolean) {
        _errorStates[fieldKey] = hasError
    }

    /**
     * 触发提交验证 - 检查所有必填字段
     *
     * @param requiredFields Map<字段标识, 字段当前值>
     * @return true 如果所有必填字段都已填写
     */
    fun validateOnSubmit(requiredFields: Map<String, String?>): Boolean {
        hasSubmitted = true
        var allValid = true

        requiredFields.forEach { (key, value) ->
            val isEmpty = value.isNullOrBlank()
            _errorStates[key] = isEmpty
            if (isEmpty) allValid = false
        }

        return allValid
    }

    /**
     * 清除所有错误状态
     */
    fun clearAll() {
        _errorStates.clear()
        hasSubmitted = false
    }

    /**
     * 清除指定字段的错误状态（通常在用户输入时调用）
     */
    fun clearError(fieldKey: String) {
        if (hasSubmitted) {
            _errorStates[fieldKey] = false
        }
    }
}

/**
 * 创建并记住一个 FormValidationState 实例
 */
@Composable
fun rememberFormValidationState(): FormValidationState {
    return remember { FormValidationState() }
}

/**
 * 带验证功能的 OutlinedTextField
 *
 * 当 validationState 中该字段有错误时，边框和标签会变为红色。
 * 当用户开始输入时，自动清除该字段的错误状态。
 *
 * @param value 当前值
 * @param onValueChange 值变化回调
 * @param label 标签文字
 * @param fieldKey 字段唯一标识（用于验证状态管理）
 * @param validationState 表单验证状态
 * @param isRequired 是否必填（默认 false）
 * @param placeholder 占位文字
 * @param readOnly 是否只读
 * @param enabled 是否可用
 * @param singleLine 是否单行
 * @param keyboardOptions 键盘选项
 * @param visualTransformation 视觉转换
 * @param trailingIcon 尾部图标
 * @param modifier 修饰符
 * @param textStyle 文字样式
 */
@Composable
fun ValidatedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    fieldKey: String,
    validationState: FormValidationState,
    isRequired: Boolean = false,
    placeholder: String? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
) {
    val hasError = validationState.hasError(fieldKey)

    // 动画化边框颜色变化
    val borderColor by animateColorAsState(
        targetValue = if (hasError) Color(0xFFE53935) else AgGreenPrimary,
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    Column(modifier = modifier) {
        // 标签行：必填字段显示红色星号
        if (isRequired) {
            Row {
                Text(
                    text = "* ",
                    color = Color(0xFFE53935),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                // 用户输入时清除错误状态
                if (newValue.isNotBlank()) {
                    validationState.clearError(fieldKey)
                }
            },
            label = if (!isRequired) {
                { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) }
            } else null,
            placeholder = placeholder?.let {
                { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) }
            },
            readOnly = readOnly,
            enabled = enabled,
            singleLine = singleLine,
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            isError = hasError,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = if (hasError) Color(0xFFE53935).copy(alpha = 0.7f) else Color.LightGray,
                errorBorderColor = Color(0xFFE53935),
                focusedLabelColor = if (hasError) Color(0xFFE53935) else AgGreenPrimary,
                errorLabelColor = Color(0xFFE53935),
                focusedTextColor = Color(0xFF333333),
                unfocusedTextColor = Color(0xFF333333),
                cursorColor = AgGreenPrimary,
                errorCursorColor = Color(0xFFE53935)
            )
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

/**
 * 带验证功能的下拉选择框
 *
 * @param label 标签文字
 * @param value 当前选中值
 * @param placeholder 占位文字
 * @param options 选项列表
 * @param onValueChange 选中值变化回调
 * @param fieldKey 字段唯一标识
 * @param validationState 表单验证状态
 * @param isRequired 是否必填
 * @param enabled 是否可用
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidatedDropdownField(
    label: String,
    value: String,
    placeholder: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    fieldKey: String,
    validationState: FormValidationState,
    isRequired: Boolean = false,
    enabled: Boolean = true
) {
    val hasError = validationState.hasError(fieldKey)
    var expanded by remember { mutableStateOf(false) }

    // 动画化边框颜色变化
    val borderColor by animateColorAsState(
        targetValue = if (hasError) Color(0xFFE53935) else AgGreenPrimary,
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    Column {
        // 标签行：必填字段显示红色星号
        if (isRequired) {
            Row {
                Text(
                    text = "* ",
                    color = Color(0xFFE53935),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                label = if (!isRequired) {
                    { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) }
                } else null,
                placeholder = {
                    Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                },
                readOnly = true,
                singleLine = true,
                enabled = enabled,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                isError = hasError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = if (hasError) Color(0xFFE53935).copy(alpha = 0.7f) else Color.LightGray,
                    errorBorderColor = Color(0xFFE53935),
                    focusedLabelColor = if (hasError) Color(0xFFE53935) else AgGreenPrimary,
                    errorLabelColor = Color(0xFFE53935),
                    disabledContainerColor = Color(0xFFF5F5F5).copy(alpha = 0.5f),
                    disabledBorderColor = Color.LightGray
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            // 选择后清除错误状态
                            validationState.clearError(fieldKey)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

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

/**
 * 带验证功能的日期选择字段
 *
 * @param value 当前日期值
 * @param label 标签文字
 * @param fieldKey 字段唯一标识
 * @param validationState 表单验证状态
 * @param isRequired 是否必填
 * @param onDateClick 点击触发日期选择
 * @param trailingIcon 尾部图标
 * @param modifier 修饰符
 */
@Composable
fun ValidatedDateField(
    value: String,
    label: String,
    fieldKey: String,
    validationState: FormValidationState,
    isRequired: Boolean = false,
    onDateClick: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val hasError = validationState.hasError(fieldKey)

    val borderColor by animateColorAsState(
        targetValue = if (hasError) Color(0xFFE53935) else AgGreenPrimary,
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    // 自动清除错误：当值非空时
    LaunchedEffect(value) {
        if (value.isNotBlank() && hasError) {
            validationState.clearError(fieldKey)
        }
    }

    Column(modifier = modifier) {
        if (isRequired) {
            Row {
                Text(
                    text = "* ",
                    color = Color(0xFFE53935),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().clickable { onDateClick() }) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                label = if (!isRequired) {
                    { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) }
                } else null,
                readOnly = true,
                enabled = false,
                singleLine = true,
                trailingIcon = trailingIcon,
                isError = hasError,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = if (hasError) Color(0xFFE53935).copy(alpha = 0.7f) else Color.LightGray,
                    errorBorderColor = Color(0xFFE53935),
                    focusedLabelColor = if (hasError) Color(0xFFE53935) else AgGreenPrimary,
                    errorLabelColor = Color(0xFFE53935),
                    disabledTextColor = Color(0xFF333333),
                    disabledBorderColor = if (hasError) Color(0xFFE53935).copy(alpha = 0.7f) else Color.LightGray,
                    disabledLabelColor = if (hasError) Color(0xFFE53935) else Color(0xFF666666),
                    disabledContainerColor = Color.Transparent
                )
            )
        }

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

/**
 * 带验证功能的图片上传区域
 *
 * @param label 标签文字
 * @param fieldKey 字段唯一标识
 * @param validationState 表单验证状态
 * @param isRequired 是否必填
 * @param hasImages 是否已选择图片
 * @param content 实际的图片选择UI
 * @param modifier 修饰符
 */
@Composable
fun ValidatedImageSection(
    label: String,
    fieldKey: String,
    validationState: FormValidationState,
    isRequired: Boolean = false,
    hasImages: Boolean,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasError = isRequired && validationState.hasError(fieldKey) && !hasImages

    val borderColor by animateColorAsState(
        targetValue = if (hasError) Color(0xFFE53935) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    Column(modifier = modifier) {
        if (isRequired) {
            Row {
                Text(
                    text = "* ",
                    color = Color(0xFFE53935),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (hasError) Modifier.border(
                        width = 1.dp,
                        color = borderColor,
                        shape = MaterialTheme.shapes.small
                    ) else Modifier
                )
                .padding(if (hasError) 4.dp else 0.dp)
        ) {
            content()
        }

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
