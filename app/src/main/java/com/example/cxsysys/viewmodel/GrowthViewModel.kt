package com.example.cxsysys.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.api.PlantingApiService
import com.example.cxsysys.model.PlantGrowthRequest
import com.example.cxsysys.utils.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class GrowthViewModel : ViewModel() {
    private val apiService = RetrofitClient.businessRetrofit.create(PlantingApiService::class.java)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _submitSuccess = MutableStateFlow<Boolean?>(null)
    val submitSuccess: StateFlow<Boolean?> = _submitSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun submitGrowth(
        context: Context,
        plantQrcode: String?,
        fieldQrcode: String?,
        fieldCode: String?,
        recordDate: String,
        height: Double,
        crownWidth: Double,
        diameter: Double,
        chestDiameter: Double,
        straightness: String,
        plantQuantity: Int,
        imageUris: List<Uri>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _submitSuccess.value = null
            _errorMessage.value = null

            try {
                // 1. 先上传图片
                val uploadedUrls = mutableListOf<String>()
                for (uri in imageUris) {
                    val file = getFileFromUri(context, uri)
                    if (file != null) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                        val uploadResponse = apiService.uploadFile(body)
                        if ((uploadResponse.code == 0 || uploadResponse.code == 200) && uploadResponse.data != null) {
                            uploadedUrls.add(uploadResponse.data.fileUrl)
                        } else {
                            throw Exception("图片上传失败: ${uploadResponse.message}")
                        }
                    }
                }

                // 2. 提交表单
                val request = PlantGrowthRequest(
                    plantQrcode = plantQrcode?.takeIf { it.isNotBlank() },
                    fieldQrcode = fieldQrcode?.takeIf { it.isNotBlank() },
                    fieldCode = fieldCode?.takeIf { it.isNotBlank() },
                    recordDate = recordDate,
                    height = height,
                    crownWidth = crownWidth,
                    diameter = diameter,
                    chestDiameter = chestDiameter,
                    straightness = straightness,
                    plantQuantity = plantQuantity,
                    photoUrl = uploadedUrls
                )

                val response = apiService.submitPlantGrowth(request)
                if (response.code == 0 || response.code == 200) {
                    _submitSuccess.value = true
                } else {
                    _errorMessage.value = response.message ?: "提交失败"
                    _submitSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "网络错误: ${e.message}"
                _submitSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _submitSuccess.value = null
        _errorMessage.value = null
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileName(context, uri) ?: "temp_image.jpg"
            val tempFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
}
