package com.example.cxsysys.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cxsysys.ui.theme.AgGreenPrimary
import com.example.cxsysys.ui.theme.BgGray
import com.example.cxsysys.ui.theme.TextBlack
import com.example.cxsysys.ui.theme.TextGray

@Composable
private fun LoginLockIcon() {
    Box(
        modifier = Modifier.size(64.dp).background(AgGreenPrimary.copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = AgGreenPrimary, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun LoginRequiredDialog(onDismiss: () -> Unit, onLogin: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        icon = { LoginLockIcon() },
        title = { Text("请先登录", color = TextBlack, fontWeight = FontWeight.Bold) },
        text = { Text("登录后即可使用农事作业与资源管理功能。", color = TextGray) },
        confirmButton = {
            Button(
                onClick = onLogin,
                colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) { Text("去登录") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("暂不登录", color = TextGray) }
        }
    )
}

@Composable
fun LoginRequiredScreen(title: String, onLogin: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(BgGray).padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LoginLockIcon()
            Spacer(Modifier.height(20.dp))
            Text("登录后查看$title", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextBlack)
            Spacer(Modifier.height(8.dp))
            Text("请先登录，查看和管理您的种植档案。", fontSize = 14.sp, color = TextGray)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onLogin,
                modifier = Modifier.widthIn(min = 160.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgGreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) { Text("去登录", fontSize = 16.sp) }
        }
    }
}
