package com.example.cxsysys.utils

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

/**
 * 基于 ZXing 和 CameraX 的二维码图像分析器
 */
class QrCodeAnalyzer(
    private val scanFrameAreaProvider: () -> ScanFrameArea? = { null },
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val supportedImageFormats = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )

    override fun analyze(image: ImageProxy) {
        if (image.format in supportedImageFormats) {
            val bytes = image.planes[0].buffer.toByteArray()
            val scanFrameArea = scanFrameAreaProvider()
            val cropRect = scanFrameArea?.toImageCropRect(
                imageWidth = image.width,
                imageHeight = image.height,
                rotationDegrees = image.imageInfo.rotationDegrees
            )
            val source = PlanarYUVLuminanceSource(
                bytes,
                image.width,
                image.height,
                cropRect?.left ?: 0,
                cropRect?.top ?: 0,
                cropRect?.width ?: image.width,
                cropRect?.height ?: image.height,
                false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            try {
                // 优化解析参数：支持多种格式，尝试更硬的解码模式
                val hints = mapOf(
                    DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.QR_CODE),
                    DecodeHintType.TRY_HARDER to true
                )
                val result = MultiFormatReader().decode(binaryBitmap, hints)
                onQrCodeScanned(result.text)
            } catch (e: Exception) {
                // 解析失败时不处理，让 CameraX 继续推下一帧
            } finally {
                image.close()
            }
        } else {
            image.close()
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
}

data class ScanFrameArea(
    val leftRatio: Float,
    val topRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float
)

private data class ImageCropRect(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int
)

private fun ScanFrameArea.toImageCropRect(
    imageWidth: Int,
    imageHeight: Int,
    rotationDegrees: Int
): ImageCropRect {
    val displayWidth = if (rotationDegrees == 90 || rotationDegrees == 270) imageHeight else imageWidth
    val displayHeight = if (rotationDegrees == 90 || rotationDegrees == 270) imageWidth else imageHeight
    val displayLeft = leftRatio.coerceIn(0f, 1f) * displayWidth
    val displayTop = topRatio.coerceIn(0f, 1f) * displayHeight
    val displayRight = (displayLeft + widthRatio.coerceIn(0f, 1f) * displayWidth).coerceIn(0f, displayWidth.toFloat())
    val displayBottom = (displayTop + heightRatio.coerceIn(0f, 1f) * displayHeight).coerceIn(0f, displayHeight.toFloat())

    val rect = when (rotationDegrees) {
        90 -> ImageCropRect(
            left = displayTop.toInt(),
            top = (imageHeight - displayRight).toInt(),
            width = (displayBottom - displayTop).toInt(),
            height = (displayRight - displayLeft).toInt()
        )
        270 -> ImageCropRect(
            left = (imageWidth - displayBottom).toInt(),
            top = displayLeft.toInt(),
            width = (displayBottom - displayTop).toInt(),
            height = (displayRight - displayLeft).toInt()
        )
        180 -> ImageCropRect(
            left = (imageWidth - displayRight).toInt(),
            top = (imageHeight - displayBottom).toInt(),
            width = (displayRight - displayLeft).toInt(),
            height = (displayBottom - displayTop).toInt()
        )
        else -> ImageCropRect(
            left = displayLeft.toInt(),
            top = displayTop.toInt(),
            width = (displayRight - displayLeft).toInt(),
            height = (displayBottom - displayTop).toInt()
        )
    }

    val safeLeft = rect.left.coerceIn(0, imageWidth - 1)
    val safeTop = rect.top.coerceIn(0, imageHeight - 1)
    val safeWidth = rect.width.coerceIn(1, imageWidth - safeLeft)
    val safeHeight = rect.height.coerceIn(1, imageHeight - safeTop)
    return ImageCropRect(safeLeft, safeTop, safeWidth, safeHeight)
}
