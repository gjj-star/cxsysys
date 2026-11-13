package com.example.cxsysys.ui.screens.mine

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cxsysys.ui.components.PhotoSourcePickerDialog
import com.example.cxsysys.ui.components.UserAvatar
import com.example.cxsysys.ui.components.rememberCameraPhotoLauncher
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.AgGreenLight
import com.example.cxsysys.ui.theme.BgGray
import com.example.cxsysys.utils.TokenManager
import com.example.cxsysys.viewmodel.AuthState
import com.example.cxsysys.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInfoScreen(
    onBackClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val profileState by authViewModel.profileState.collectAsState()
    val sessionVersion by TokenManager.sessionVersion.collectAsState()
    val user = authState as? AuthState.LoggedIn
    val isBusy = profileState.isLoading || profileState.isSavingAvatar
    var showPhotoPicker by remember { mutableStateOf(false) }
    var pickerSession by remember { mutableStateOf<Long?>(null) }
    var photoRequestSession by remember { mutableStateOf<Long?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && photoRequestSession == TokenManager.sessionVersion.value && TokenManager.isLoggedIn()) {
            authViewModel.uploadAvatar(context, uri)
        }
        photoRequestSession = null
    }
    val cameraLauncher = rememberCameraPhotoLauncher { uri ->
        if (photoRequestSession == TokenManager.sessionVersion.value && TokenManager.isLoggedIn()) {
            authViewModel.uploadAvatar(context, uri)
        }
        photoRequestSession = null
    }

    LaunchedEffect(user?.userId, sessionVersion) {
        showPhotoPicker = false
        pickerSession = null
        photoRequestSession = null
        if (user != null) authViewModel.refreshProfile()
    }

    PhotoSourcePickerDialog(
        visible = showPhotoPicker && !isBusy,
        onDismiss = { showPhotoPicker = false },
        onTakePhoto = {
            if (pickerSession == TokenManager.sessionVersion.value) {
                photoRequestSession = pickerSession
                cameraLauncher()
            }
        },
        onPickImages = {
            if (pickerSession == TokenManager.sessionVersion.value) {
                photoRequestSession = pickerSession
                galleryLauncher.launch("image/*")
            }
        },
        title = "修改头像",
        pickImagesSubtitle = "从相册中选择一张头像图片",
        onClearPhoto = if (!user?.avatarUrl.isNullOrBlank()) {
            { if (pickerSession == TokenManager.sessionVersion.value) authViewModel.clearAvatar() }
        } else null
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人信息") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { authViewModel.refreshProfile() }, enabled = !isBusy) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新个人信息")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgGreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (profileState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AgGreenPrimary)
            }

            // 头像
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isBusy) {
                            pickerSession = sessionVersion
                            showPhotoPicker = true
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("头像", fontSize = 16.sp, color = Color(0xFF333333), modifier = Modifier.weight(1f))
                    if (profileState.isSavingAvatar) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AgGreenPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        UserAvatar(
                            avatarUrl = user?.avatarUrl,
                            userId = user?.userId ?: 0,
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(AgGreenLight),
                            iconSize = 28.dp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFCCCCCC))
                }
            }

            // 基本信息卡片
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileInfoRow(
                        icon = Icons.Default.Person,
                        label = "姓名",
                        value = (user?.realName?.takeIf(String::isNotBlank) ?: user?.userName).displayValue()
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    ProfileInfoRow(icon = Icons.Default.Phone, label = "手机号", value = user?.phoneNumber.displayValue())
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    ProfileInfoRow(icon = Icons.Default.Badge, label = "用户名", value = user?.userName.displayValue())
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    ProfileInfoRow(icon = Icons.Default.Business, label = "所属企业", value = user?.enterpriseName.displayValue())
                }
            }

            // 身份信息卡片
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileInfoRow(
                        icon = Icons.Default.AdminPanelSettings,
                        label = "角色",
                        value = user?.roles?.mapNotNull { it.roleName?.takeIf(String::isNotBlank) }
                            ?.joinToString("、")?.takeIf(String::isNotBlank)
                            ?: if (user?.superAdmin == true) "超级管理员" else "—"
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    ProfileInfoRow(
                        icon = Icons.Default.VerifiedUser,
                        label = "账号状态",
                        value = when (user?.status) {
                            1 -> "正常"
                            0 -> "停用"
                            else -> "—"
                        }
                    )
                }
            }

            profileState.errorMessage?.let { message ->
                Text(message, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { authViewModel.refreshProfile() }, enabled = !isBusy) {
                    Text("刷新资料", color = AgGreenPrimary)
                }
            }
        }
    }
}

private fun String?.displayValue(): String = this?.takeIf(String::isNotBlank) ?: "—"

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AgGreenPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, color = Color(0xFF666666), modifier = Modifier.width(72.dp))
        Text(value, fontSize = 15.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}
