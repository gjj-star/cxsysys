package com.example.cxsysys.ui.screens.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import com.example.cxsysys.utils.TokenManager
import com.example.cxsysys.viewmodel.AuthState
import com.example.cxsysys.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val passwordState by authViewModel.passwordState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val sessionVersion by TokenManager.sessionVersion.collectAsState()
    var formSession by remember { mutableStateOf(sessionVersion) }
    var submittedHere by remember(sessionVersion) { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(sessionVersion) {
        formSession = sessionVersion
        oldPassword = ""
        newPassword = ""
        confirmPassword = ""
        oldPasswordVisible = false
        newPasswordVisible = false
        confirmPasswordVisible = false
        if (!passwordState.isSubmitting) authViewModel.resetPasswordState()
    }

    LaunchedEffect(passwordState.success) {
        if (passwordState.success && authViewModel.passwordState.value.success) {
            if (!submittedHere) {
                authViewModel.resetPasswordState()
                return@LaunchedEffect
            }
            submittedHere = false
            oldPassword = ""
            newPassword = ""
            confirmPassword = ""
            oldPasswordVisible = false
            newPasswordVisible = false
            confirmPasswordVisible = false
            if (authState is AuthState.LoggedIn && TokenManager.isLoggedIn()) {
                authViewModel.resetPasswordState()
                onBackClick()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("修改密码") },
                navigationIcon = {
                    IconButton(onClick = {
                        submittedHere = false
                        onBackClick()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgGreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgGray)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "请输入您的旧密码和新密码来完成密码修改",
                fontSize = 14.sp,
                color = Color(0xFF888888)
            )

            // 旧密码
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                enabled = !passwordState.isSubmitting,
                label = { Text("旧密码") },
                placeholder = { Text("请输入旧密码") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AgGreenPrimary) },
                trailingIcon = {
                    IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                        Icon(
                            if (oldPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = Color.Gray
                        )
                    }
                },
                visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgGreenPrimary,
                    focusedLabelColor = AgGreenPrimary,
                    cursorColor = AgGreenPrimary
                )
            )

            // 新密码
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                enabled = !passwordState.isSubmitting,
                label = { Text("新密码") },
                placeholder = { Text("请输入新密码（至少6位）") },
                leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null, tint = AgGreenPrimary) },
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = Color.Gray
                        )
                    }
                },
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgGreenPrimary,
                    focusedLabelColor = AgGreenPrimary,
                    cursorColor = AgGreenPrimary
                )
            )

            // 确认新密码
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                enabled = !passwordState.isSubmitting,
                label = { Text("确认新密码") },
                placeholder = { Text("请再次输入新密码") },
                leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null, tint = AgGreenPrimary) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = Color.Gray
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AgGreenPrimary,
                    focusedLabelColor = AgGreenPrimary,
                    cursorColor = AgGreenPrimary
                )
            )

            // 密码提示
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("密码长度至少6位，建议包含字母和数字", fontSize = 13.sp, color = Color(0xFF795500))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            passwordState.errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            // 提交按钮
            Button(
                onClick = {
                    if (formSession == TokenManager.sessionVersion.value) {
                        submittedHere = true
                        authViewModel.changePassword(oldPassword, newPassword, confirmPassword)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary),
                enabled = !passwordState.isSubmitting && oldPassword.isNotBlank()
                    && newPassword.isNotBlank() && confirmPassword.isNotBlank()
            ) {
                if (passwordState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    if (passwordState.isSubmitting) "提交中..." else "确认修改",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
