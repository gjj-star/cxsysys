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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpFeedbackScreen(onBackClick: () -> Unit) {
    var feedbackText by remember { mutableStateOf("") }
    var showSubmitDialog by remember { mutableStateOf(false) }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("提示") },
            text = { Text("反馈提交功能将在接口就绪后启用，当前仅为界面预览") },
            confirmButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text("知道了") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("帮助与反馈") },
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
            // 常见问题
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Help, contentDescription = null, tint = AgGreenPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("常见问题", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FaqItem(question = "如何登录系统？", answer = "支持手机验证码和密码两种登录方式")
                    FaqItem(question = "如何修改个人信息？", answer = "进入个人信息页面进行编辑")
                    FaqItem(question = "二维码如何使用？", answer = "点击列表中的二维码图标查看详情")
                }
            }

            // 意见反馈
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Feedback, contentDescription = null, tint = AgGreenPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("意见反馈", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("请输入您的建议或问题") },
                        placeholder = { Text("描述您遇到的问题或改进建议...") },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AgGreenPrimary,
                            focusedLabelColor = AgGreenPrimary,
                            cursorColor = AgGreenPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showSubmitDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary),
                        enabled = feedbackText.isNotBlank()
                    ) {
                        Text("提交反馈", fontSize = 15.sp)
                    }
                }
            }

            // 联系方式
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContactSupport, contentDescription = null, tint = AgGreenPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("联系我们", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ContactRow(icon = Icons.Default.Email, label = "邮箱", value = "support@example.com")
                    ContactRow(icon = Icons.Default.Phone, label = "电话", value = "400-000-0000")
                }
            }
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = AgGreenPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(question, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
        }
        Text(answer, fontSize = 13.sp, color = Color(0xFF888888), modifier = Modifier.padding(start = 24.dp, top = 4.dp))
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AgGreenPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, color = Color(0xFF666666), modifier = Modifier.width(48.dp))
        Text(value, fontSize = 14.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium)
    }
}
