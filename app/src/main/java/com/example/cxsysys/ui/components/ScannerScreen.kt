package com.example.cxsysys.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.cxsysys.utils.QrCodeAnalyzer
import com.example.cxsysys.utils.ScanFrameArea
import java.util.concurrent.Executors

/**
 * 扫码独立页面组件
 * @param onScanResult 扫码成功回调，返回解析出的文本
 * @param onCancel 取消扫码回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onScanResult: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    var scanFrameArea by remember { mutableStateOf<ScanFrameArea?>(null) }
    val currentScanFrameArea by rememberUpdatedState(scanFrameArea)
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    BackHandler {
        onCancel()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("扫描二维码", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .onSizeChanged { size ->
                    scanFrameArea = calculateScanFrameArea(
                        width = size.width,
                        height = size.height,
                        density = density.density
                    )
                }
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                var isScanned by remember { mutableStateOf(false) }

                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalyzer = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(
                                        Executors.newSingleThreadExecutor(),
                                        QrCodeAnalyzer(
                                            scanFrameAreaProvider = { currentScanFrameArea },
                                            onQrCodeScanned = { result ->
                                                if (!isScanned) {
                                                    isScanned = true
                                                    // 过滤结果：只保留字母、数字和连字符 "-"
                                                    val filteredResult = result.replace(Regex("[^a-zA-Z0-9\\-]"), "")
                                                    if (filteredResult.isNotEmpty()) {
                                                        // 切回主线程回调
                                                        previewView.post {
                                                            onScanResult(filteredResult)
                                                        }
                                                    } else {
                                                        isScanned = false // 如果过滤后为空，继续扫
                                                    }
                                                }
                                            }
                                        )
                                    )
                                }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalyzer
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(context))
                    }
                )

                // 扫码框 UI (简单的中间挖空效果)
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 可以在这里画四个角，目前为了简洁只留边框提示
                    ScannerFrameOverlay(modifier = Modifier.fillMaxSize())
                }
                
                Text(
                    text = "将二维码放入框内，即可自动扫描",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp)
                )

            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("需要相机权限才能扫码", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { launcher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("去授权")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerFrameOverlay(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val frameSize = calculateScanFrameSize(size.width, 280.dp.toPx())
        val frameLeft = calculateScanFrameLeft(size.width, frameSize)
        val frameTop = calculateScanFrameTop(size.height, frameSize, 24.dp.toPx())
        val frameRect = Rect(
            left = frameLeft,
            top = frameTop,
            right = frameLeft + frameSize,
            bottom = frameTop + frameSize
        )
        val cornerRadius = 18.dp.toPx()
        val overlayPath = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(0f, 0f, size.width, size.height))
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    rect = frameRect,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            )
        }

        drawPath(overlayPath, color = Color.Black.copy(alpha = 0.52f))

        drawRoundRect(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(frameRect.left, frameRect.top),
            size = Size(frameSize, frameSize),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 1.dp.toPx())
        )

        val cornerLength = 46.dp.toPx()
        val cornerStrokeWidth = 5.dp.toPx()
        val cornerColor = Color(0xFF6EE7B7)
        val inset = 2.dp.toPx()

        drawLine(cornerColor, Offset(frameRect.left + inset, frameRect.top), Offset(frameRect.left + cornerLength, frameRect.top), strokeWidth = cornerStrokeWidth, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(frameRect.left, frameRect.top + inset), Offset(frameRect.left, frameRect.top + cornerLength), strokeWidth = cornerStrokeWidth, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(frameRect.right - cornerLength, frameRect.top), Offset(frameRect.right - inset, frameRect.top), strokeWidth = cornerStrokeWidth, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(frameRect.right, frameRect.top + inset), Offset(frameRect.right, frameRect.top + cornerLength), strokeWidth = cornerStrokeWidth, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(frameRect.left + inset, frameRect.bottom), Offset(frameRect.left + cornerLength, frameRect.bottom), strokeWidth = cornerStrokeWidth, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(frameRect.left, frameRect.bottom - cornerLength), Offset(frameRect.left, frameRect.bottom - inset), strokeWidth = cornerStrokeWidth, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(frameRect.right - cornerLength, frameRect.bottom), Offset(frameRect.right - inset, frameRect.bottom), strokeWidth = cornerStrokeWidth, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(frameRect.right, frameRect.bottom - cornerLength), Offset(frameRect.right, frameRect.bottom - inset), strokeWidth = cornerStrokeWidth, cap = StrokeCap.Round)
    }
}

private fun calculateScanFrameArea(
    width: Int,
    height: Int,
    density: Float
): ScanFrameArea? {
    if (width <= 0 || height <= 0) return null

    val frameSize = calculateScanFrameSize(width.toFloat(), 280f * density)
    val frameLeft = calculateScanFrameLeft(width.toFloat(), frameSize)
    val frameTop = calculateScanFrameTop(height.toFloat(), frameSize, 24f * density)

    return ScanFrameArea(
        leftRatio = frameLeft / width,
        topRatio = frameTop / height,
        widthRatio = frameSize / width,
        heightRatio = frameSize / height
    )
}

private fun calculateScanFrameSize(
    width: Float,
    maxFrameSize: Float
): Float = minOf(width * 0.72f, maxFrameSize)

private fun calculateScanFrameLeft(
    width: Float,
    frameSize: Float
): Float = (width - frameSize) / 2f

private fun calculateScanFrameTop(
    height: Float,
    frameSize: Float,
    topOffset: Float
): Float = (height - frameSize) / 2f - topOffset
