package com.example.cxsysys.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.api.PlantingApiService
import com.example.cxsysys.model.*
import com.example.cxsysys.utils.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AgInputViewModel : ViewModel() {
    private val apiService = RetrofitClient.businessRetrofit.create(PlantingApiService::class.java)

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    private val _supplierList = MutableStateFlow<List<SupplierSimple>>(emptyList())
    val supplierList: StateFlow<List<SupplierSimple>> = _supplierList.asStateFlow()

    fun resetState() {
        _submitState.value = SubmitState.Idle
    }

    // 1. 提交供应商信息
    fun submitSupplier(name: String, address: String, tel: String, typeCode: Int) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            try {
                val request = SupplierRequest(
                    supplierName = name,
                    supplierAddress = address,
                    supplierTel = tel,
                    supplierClass = typeCode
                )
                val response = apiService.submitSupplier(request)
                if (response.code == 0 || response.code == 200) {
                    _submitState.value = SubmitState.Success
                } else {
                    _submitState.value = SubmitState.Error(response.message ?: "提交失败")
                }
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e.message ?: "网络错误")
            }
        }
    }

    // 2. 获取肥料厂家列表
    fun fetchFertilizerSuppliers() {
        viewModelScope.launch {
            try {
                val response = apiService.getFertilizerSupplierList()
                if (response.code == 0 || response.code == 200) {
                    _supplierList.value = response.data ?: emptyList()
                }
            } catch (e: Exception) {
                // 忽略或打印日志
            }
        }
    }

    // 3. 获取农药厂家列表
    fun fetchPesticideSuppliers() {
        viewModelScope.launch {
            try {
                val response = apiService.getPesticideSupplierList()
                if (response.code == 0 || response.code == 200) {
                    _supplierList.value = response.data ?: emptyList()
                }
            } catch (e: Exception) {
                // 忽略或打印日志
            }
        }
    }

    // 4. 提交农药入库信息
    fun submitPesticide(
        supplierId: Int,
        pestName: String,
        ingredient: String,
        manufactureDate: String,
        remark: String?,
        frontPhotoFile: File?,
        backPhotoFile: File?
    ) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            try {
                // 先上传图片
                val frontUrl = uploadSingleFile(frontPhotoFile)
                val backUrl = uploadSingleFile(backPhotoFile)

                if (frontPhotoFile != null && frontUrl == null) {
                    _submitState.value = SubmitState.Error("正面图上传失败")
                    return@launch
                }
                if (backPhotoFile != null && backUrl == null) {
                    _submitState.value = SubmitState.Error("背面图上传失败")
                    return@launch
                }

                val request = PesticideRequest(
                    supplierId = supplierId,
                    pestName = pestName,
                    pestIngredient = ingredient,
                    manufactureDate = manufactureDate,
                    remark = remark?.ifEmpty { null },
                    frontPhotoUrl = if (frontUrl != null) listOf(frontUrl) else emptyList(),
                    backPhotoUrl = if (backUrl != null) listOf(backUrl) else emptyList()
                )

                val response = apiService.submitPesticideInput(request)
                if (response.code == 0 || response.code == 200) {
                    _submitState.value = SubmitState.Success
                } else {
                    _submitState.value = SubmitState.Error(response.message ?: "提交失败")
                }
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e.message ?: "网络错误")
            }
        }
    }

    // 5. 提交肥料入库信息
    fun submitFertilizer(
        supplierId: Int,
        fertName: String,
        fertType: String,
        nutrientN: Double,
        nutrientP: Double,
        nutrientK: Double,
        remark: String?,
        frontPhotoFile: File?,
        backPhotoFile: File?
    ) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            try {
                // 先上传图片
                val frontUrl = uploadSingleFile(frontPhotoFile)
                val backUrl = uploadSingleFile(backPhotoFile)

                if (frontPhotoFile != null && frontUrl == null) {
                    _submitState.value = SubmitState.Error("正面图上传失败")
                    return@launch
                }
                if (backPhotoFile != null && backUrl == null) {
                    _submitState.value = SubmitState.Error("背面图上传失败")
                    return@launch
                }

                val request = FertilizerRequest(
                    supplierId = supplierId,
                    fertName = fertName,
                    fertType = fertType,
                    nutrientN = nutrientN,
                    nutrientP = nutrientP,
                    nutrientK = nutrientK,
                    remark = remark?.ifEmpty { null },
                    frontPhotoUrl = if (frontUrl != null) listOf(frontUrl) else emptyList(),
                    backPhotoUrl = if (backUrl != null) listOf(backUrl) else emptyList()
                )

                val response = apiService.submitFertilizerInput(request)
                if (response.code == 0 || response.code == 200) {
                    _submitState.value = SubmitState.Success
                } else {
                    _submitState.value = SubmitState.Error(response.message ?: "提交失败")
                }
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e.message ?: "网络错误")
            }
        }
    }

    private suspend fun uploadSingleFile(file: File?): String? {
        if (file == null || !file.exists()) return null
        return try {
            val requestFile = file.asRequestBody(MultipartBody.FORM)
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val uploadResponse = apiService.uploadFile(body)
            if ((uploadResponse.code == 0 || uploadResponse.code == 200) && uploadResponse.data != null) {
                uploadResponse.data.fileUrl
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}