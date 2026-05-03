package com.example.cxsysys.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import com.example.cxsysys.ui.theme.AgGreenPrimary
import java.io.File

@Composable
fun rememberCameraPhotoLauncher(onPhotoSelected: (Uri) -> Unit): () -> Unit {
    val context = LocalContext.current
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingUri
        if (success && uri != null) {
            onPhotoSelected(uri)
        } else {
            pendingUri = null
        }
    }
    fun launchCamera() {
        runCatching {
            createCameraImageUri(context)
        }.onSuccess { uri ->
            pendingUri = uri
            runCatching {
                takePictureLauncher.launch(uri)
            }.onFailure {
                pendingUri = null
                Toast.makeText(context, "无法打开系统相机", Toast.LENGTH_SHORT).show()
            }
        }.onFailure {
            Toast.makeText(context, "无法创建照片文件", Toast.LENGTH_SHORT).show()
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(context, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    return {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

@Composable
fun PhotoSourcePickerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickImages: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "添加照片",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.Gray)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PhotoSourceOption(
                    icon = Icons.Default.PhotoCamera,
                    title = "拍照",
                    subtitle = "打开系统相机进行拍摄",
                    tint = AgGreenPrimary,
                    background = Color(0xFFEAF6EA),
                    onClick = {
                        onDismiss()
                        onTakePhoto()
                    }
                )
                PhotoSourceOption(
                    icon = Icons.Default.PhotoLibrary,
                    title = "选取图片",
                    subtitle = "从相册中选择一张或多张已有图片",
                    tint = Color(0xFF1976D2),
                    background = Color(0xFFEAF2FD),
                    onClick = {
                        onDismiss()
                        onPickImages()
                    }
                )
            }
        },
        confirmButton = {},
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun PhotoSourceOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    background: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, tint.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(8.dp),
                color = background
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF263238)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF78909C)
                )
            }
        }
    }
}

private fun createCameraImageUri(context: Context): Uri {
    val imageDir = File(context.cacheDir, "camera_images").apply { mkdirs() }
    val imageFile = File(imageDir, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}
