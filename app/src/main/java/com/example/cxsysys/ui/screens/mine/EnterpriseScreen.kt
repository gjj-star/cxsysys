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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cxsysys.model.EnterpriseMember
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import com.example.cxsysys.viewmodel.EnterpriseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseScreen(
    onBackClick: () -> Unit,
    enterpriseViewModel: EnterpriseViewModel = viewModel()
) {
    val state by enterpriseViewModel.state.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("企业管理") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 企业基本信息
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = AgGreenPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("企业信息", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    EnterpriseInfoRow(label = "企业名称", value = state.enterpriseInfo?.enterpriseName.displayValue())
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    EnterpriseInfoRow(label = "企业ID", value = state.enterpriseInfo?.enterpriseId?.toString().displayValue())
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    EnterpriseInfoRow(label = "联系电话", value = state.enterpriseInfo?.phoneNumber.displayValue())
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    EnterpriseInfoRow(label = "企业地址", value = state.enterpriseInfo?.address.displayValue())
                    if (state.isLoadingInfo) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AgGreenPrimary)
                    }
                    state.infoError?.let { message ->
                        EnterpriseError(message = message, onRetry = enterpriseViewModel::retryInfo)
                    }
                }
            }

            // 成员仅展示资料，不包含编辑操作。
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = AgGreenPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("成员管理", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    state.members.forEachIndexed { index, member ->
                        if (index > 0) HorizontalDivider(color = Color(0xFFF0F0F0))
                        EnterpriseMemberRow(member)
                    }
                    if (state.membersLoaded && state.members.isEmpty()) {
                        Text("暂无成员", fontSize = 14.sp, color = Color(0xFF999999))
                    }
                    if (state.isLoadingMembers) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AgGreenPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("加载成员中…", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                    state.membersError?.let { message ->
                        EnterpriseError(message = message, onRetry = enterpriseViewModel::retryMembers)
                    }
                    if (state.hasMore && !state.isLoadingMembers && state.membersError == null) {
                        TextButton(
                            onClick = enterpriseViewModel::loadMoreMembers,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("加载更多", color = AgGreenPrimary)
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun EnterpriseInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, color = Color(0xFF666666), modifier = Modifier.width(80.dp))
        Text(value, fontSize = 15.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

private fun String?.displayValue(): String = this?.takeIf { it.isNotBlank() } ?: "—"

@Composable
private fun EnterpriseMemberRow(member: EnterpriseMember) {
    val name = member.realName?.takeIf { it.isNotBlank() }
        ?: member.userName?.takeIf { it.isNotBlank() } ?: "未命名成员"
    val roles = if (member.superAdmin) {
        "超级管理员"
    } else {
        member.roles.mapNotNull { it.roleName?.takeIf(String::isNotBlank) }
            .distinct().joinToString("、").ifBlank { "—" }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 文件接口只允许本人读取，成员列表使用默认图标。
        Icon(Icons.Default.Person, contentDescription = null, tint = AgGreenPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
            val details = listOfNotNull(
                member.userName?.takeIf { it.isNotBlank() && it != name },
                roles
            ).joinToString(" · ")
            Text(details, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun EnterpriseError(message: String, onRetry: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(message, fontSize = 13.sp, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
        TextButton(onClick = onRetry) { Text("重试", color = AgGreenPrimary) }
    }
}
