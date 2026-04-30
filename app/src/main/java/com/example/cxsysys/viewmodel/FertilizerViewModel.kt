package com.example.cxsysys.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.model.Fertilizer
import com.example.cxsysys.model.FertilizeWorkRequest
import com.example.cxsysys.utils.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FertilizerViewModel : ViewModel() {
    private val api = RetrofitClient.businessRetrofit.create(com.example.cxsysys.api.PlantingApiService::class.java)

    private val _fertilizers = MutableStateFlow<List<Fertilizer>>(emptyList())
    val fertilizers: StateFlow<List<Fertilizer>> = _fertilizers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess

    init {
        fetchFertilizers()
    }

    fun fetchFertilizers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = api.getFertilizerList()
                if (response.code == 0 || response.code == 200) {
                    _fertilizers.value = response.data ?: emptyList()
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

    fun submitFertilizeWork(
        fieldQrcode: String?,
        fieldCode: String?,
        fertiDate: String,
        fertiPeriod: String,
        fertiIds: List<Int>,
        fertiDosage: Double,
        fertiMethod: String,
        fertiWater: String?,
        remark: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _submitSuccess.value = false
            try {
                // 将多个肥料ID拼接成字符串，以逗号分隔
                val fertiIdStr = fertiIds.joinToString(",")
                
                val request = FertilizeWorkRequest(
                    fieldQrcode = fieldQrcode?.takeIf { it.isNotBlank() },
                    fieldCode = fieldCode?.takeIf { it.isNotBlank() },
                    fertiDate = fertiDate,
                    fertiPeriod = fertiPeriod,
                    fertiId = fertiIdStr,
                    fertiDosage = fertiDosage,
                    fertiMethod = fertiMethod,
                    fertiWater = fertiWater?.takeIf { it.isNotBlank() },
                    remark = remark?.takeIf { it.isNotBlank() }
                )
                val response = api.submitFertilizeWork(request)
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
