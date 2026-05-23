package com.example.cxsysys.ui.screens.mine

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.AgGreenDark
import com.example.cxsysys.ui.theme.AgGreenLight
import com.example.cxsysys.ui.theme.BgGray
import com.example.cxsysys.viewmodel.AuthState
import com.example.cxsysys.viewmodel.AuthViewModel
import com.example.cxsysys.viewmodel.LoginMethod

@Composable
fun MineScreen(
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val formState by authViewModel.formState.collectAsState()

    // 错误提示处理
    LaunchedEffect(formState.errorMessage) {
        formState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            authViewModel.clearError()
        }
    }

    // 读取 App 版本信息
    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (e: Exception) {
        null
    }
    @Suppress("DEPRECATION")
    val versionName = packageInfo?.versionName ?: "未知"
    @Suppress("DEPRECATION")
    val versionCode = packageInfo?.versionCode?.toString() ?: "-"

    when (authState) {
        is AuthState.NotLoggedIn -> {
            LoginScreen(
                formState = formState,
                authViewModel = authViewModel,
                versionName = versionName,
                versionCode = versionCode
            )
        }
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AgGreenPrimary)
            }
        }
        is AuthState.LoggedIn -> {
            UserProfileScreen(
                authState = authState as AuthState.LoggedIn,
                onLogout = { authViewModel.logout() },
                versionName = versionName,
                versionCode = versionCode
            )
        }
        is AuthState.Error -> {
            LoginScreen(
                formState = formState.copy(errorMessage = (authState as AuthState.Error).message),
                authViewModel = authViewModel,
                versionName = versionName,
                versionCode = versionCode
            )
        }
    }
}

/**
 * 登录界面
 */
@Composable
private fun LoginScreen(
    formState: com.example.cxsysys.viewmodel.LoginFormState,
    authViewModel: AuthViewModel,
    versionName: String,
    versionCode: String
) {
    val scrollState = rememberScrollState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Logo 区域
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AgGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Forest,
                contentDescription = null,
                tint = AgGreenPrimary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "沉香溯源系统",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Text(
            text = "欢迎登录",
            fontSize = 14.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 登录方式切换
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // 登录方式切换标签
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    LoginMethodTab(
                        text = "验证码登录",
                        isSelected = formState.loginMethod == LoginMethod.SMS_CODE,
                        onClick = { authViewModel.switchLoginMethod(LoginMethod.SMS_CODE) }
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    LoginMethodTab(
                        text = "密码登录",
                        isSelected = formState.loginMethod == LoginMethod.PASSWORD,
                        onClick = { authViewModel.switchLoginMethod(LoginMethod.PASSWORD) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 根据登录方式显示不同的表单
                AnimatedContent(
                    targetState = formState.loginMethod,
                    transitionSpec = {
                        fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                    },
                    label = "loginFormTransition"
                ) { loginMethod ->
                    when (loginMethod) {
                        LoginMethod.SMS_CODE -> {
                            SmsLoginForm(
                                formState = formState,
                                authViewModel = authViewModel
                            )
                        }
                        LoginMethod.PASSWORD -> {
                            PasswordLoginForm(
                                formState = formState,
                                authViewModel = authViewModel,
                                passwordVisible = passwordVisible,
                                onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 版本号
        Text(
            text = "版本 $versionName (Build $versionCode)",
            fontSize = 12.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

/**
 * 验证码登录表单
 */
@Composable
private fun SmsLoginForm(
    formState: com.example.cxsysys.viewmodel.LoginFormState,
    authViewModel: AuthViewModel
) {
    Column {
        // 手机号输入
        OutlinedTextField(
            value = formState.phone,
            onValueChange = { authViewModel.updatePhone(it) },
            label = { Text("手机号") },
            placeholder = { Text("请输入手机号") },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null, tint = AgGreenPrimary)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgGreenPrimary,
                focusedLabelColor = AgGreenPrimary,
                cursorColor = AgGreenPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 验证码输入 + 发送按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = formState.smsCode,
                onValueChange = { authViewModel.updateSmsCode(it) },
                label = { Text("验证码") },
                placeholder = { Text("请输入验证码") },
                leadingIcon = {
                    Icon(Icons.Default.Sms, contentDescription = null, tint = AgGreenPrimary)
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgGreenPrimary,
                    focusedLabelColor = AgGreenPrimary,
                    cursorColor = AgGreenPrimary
                )
            )

            Button(
                onClick = { authViewModel.sendSmsCode() },
                enabled = !formState.isSendingSms && formState.smsCountdown == 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (formState.smsCountdown > 0) Color.LightGray else AgGreenPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = when {
                        formState.isSendingSms -> "发送中..."
                        formState.smsCountdown > 0 -> "${formState.smsCountdown}s"
                        else -> "获取验证码"
                    },
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 登录按钮
        Button(
            onClick = { authViewModel.loginBySms() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary),
            enabled = !formState.isLoggingIn
        ) {
            if (formState.isLoggingIn) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("登录", fontSize = 16.sp)
            }
        }
    }
}

/**
 * 密码登录表单
 */
@Composable
private fun PasswordLoginForm(
    formState: com.example.cxsysys.viewmodel.LoginFormState,
    authViewModel: AuthViewModel,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit
) {
    Column {
        // 账号输入
        OutlinedTextField(
            value = formState.account,
            onValueChange = { authViewModel.updateAccount(it) },
            label = { Text("手机号/用户名") },
            placeholder = { Text("请输入手机号或用户名") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null, tint = AgGreenPrimary)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgGreenPrimary,
                focusedLabelColor = AgGreenPrimary,
                cursorColor = AgGreenPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 密码输入
        OutlinedTextField(
            value = formState.password,
            onValueChange = { authViewModel.updatePassword(it) },
            label = { Text("密码") },
            placeholder = { Text("请输入密码") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = AgGreenPrimary)
            },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                        tint = Color.Gray
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgGreenPrimary,
                focusedLabelColor = AgGreenPrimary,
                cursorColor = AgGreenPrimary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 登录按钮
        Button(
            onClick = { authViewModel.loginByPassword() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary),
            enabled = !formState.isLoggingIn
        ) {
            if (formState.isLoggingIn) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("登录", fontSize = 16.sp)
            }
        }
    }
}

/**
 * 登录方式切换标签
 */
@Composable
private fun LoginMethodTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AgGreenPrimary else Color(0xFF888888)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(if (isSelected) AgGreenPrimary else Color.Transparent)
        )
    }
}

/**
 * 用户个人中心界面（已登录状态）
 */
@Composable
private fun UserProfileScreen(
    authState: AuthState.LoggedIn,
    onLogout: () -> Unit,
    versionName: String,
    versionCode: String
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // 退出确认弹窗
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("确认退出") },
            text = { Text("确定要退出登录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("退出", color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消", color = Color.Gray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .verticalScroll(scrollState)
    ) {
        // 用户信息头部
        Card(
            colors = CardDefaults.cardColors(containerColor = AgGreenPrimary),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 头像
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 用户名
                Text(
                    text = authState.realName ?: authState.userName ?: "用户",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // 手机号
                authState.phoneNumber?.let { phone ->
                    Text(
                        text = phone,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // 超级管理员标识
                if (authState.superAdmin) {
                    Text(
                        text = "超级管理员",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 功能菜单
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column {
                MenuItemRow(
                    icon = Icons.Default.Person,
                    title = "个人信息",
                    subtitle = "查看和编辑个人资料"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                MenuItemRow(
                    icon = Icons.Default.Business,
                    title = "企业管理",
                    subtitle = "企业ID: ${authState.enterpriseId}"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                MenuItemRow(
                    icon = Icons.Default.Lock,
                    title = "修改密码",
                    subtitle = "定期修改密码保障安全"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column {
                MenuItemRow(
                    icon = Icons.Default.Settings,
                    title = "设置",
                    subtitle = "应用设置与偏好"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                MenuItemRow(
                    icon = Icons.Default.Help,
                    title = "帮助与反馈",
                    subtitle = "常见问题与意见反馈"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                MenuItemRow(
                    icon = Icons.Default.Info,
                    title = "关于",
                    subtitle = "版本 $versionName"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 退出登录按钮
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFFE53935)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("退出登录", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 版本号
        Text(
            text = "版本 $versionName (Build $versionCode)",
            fontSize = 12.sp,
            color = Color(0xFF888888),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }
}

/**
 * 菜单项组件
 */
@Composable
private fun MenuItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AgGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AgGreenPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFCCCCCC),
            modifier = Modifier.size(20.dp)
        )
    }
}
