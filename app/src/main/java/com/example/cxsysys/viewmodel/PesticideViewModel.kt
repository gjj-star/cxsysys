package com.example.cxsysys.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.model.Pesticide
import com.example.cxsysys.model.PesticideWorkRequest
import com.example.cxsysys.utils.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PesticideViewModel : ViewModel() {
    private val api = RetrofitClient.businessRetrofit.create(com.example.cxsysys.api.PlantingApiService::class.java)

    private val _pesticides = MutableStateFlow<List<Pesticide>>(emptyList())
    val pesticides: StateFlow<List<Pesticide>> = _pesticides

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess

    init {
        fetchPesticides()
    }

    fun fetchPesticides() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = api.getPesticideList()
                if (response.code == 0 || response.code == 200) {
                    _pesticides.value = response.data ?: emptyList()
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "网络请求失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitPesticideWork(
        plantQrcode: String?,
        fieldQrcode: String?,
        fieldCode: String?,
        date: String,
        period: String,
        pestIds: List<Int>,
        pestDosage: Double,
        pestMethod: String,
        pestWater: Double,
        record: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _submitSuccess.value = false
            try {
                // 将多个农药ID拼接成字符串，以逗号分隔
                val pestIdStr = pestIds.joinToString(",")
                
                val request = PesticideWorkRequest(
                    plantQrcode = plantQrcode?.takeIf { it.isNotBlank() },
                    fieldQrcode = fieldQrcode?.takeIf { it.isNotBlank() },
                    fieldCode = fieldCode?.takeIf { it.isNotBlank() },
                    date = date,
                    period = period,
                    pestId = pestIdStr,
                    pestDosage = pestDosage,
                    pestMethod = pestMethod,
                    pestWater = pestWater,
                    record = record?.takeIf { it.isNotBlank() }
                )
                val response = api.submitPesticideWork(request)
                if (response.code == 0 || response.code == 200) {
                    _submitSuccess.value = true
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "提交失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSubmitState() {
        _submitSuccess.value = false
        _error.value = null
    }
}
