package com.example.cxsysys.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.model.PlantingRequest
import com.example.cxsysys.model.Subspecies
import com.example.cxsysys.utils.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlantingViewModel : ViewModel() {

    private val api = RetrofitClient.businessRetrofit.create(com.example.cxsysys.api.PlantingApiService::class.java)

    private val _subspeciesList = MutableStateFlow<List<Subspecies>>(emptyList())
    val subspeciesList: StateFlow<List<Subspecies>> = _subspeciesList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess.asStateFlow()

    init {
        // 移除 init 中的自动调用，改为由 UI 层的 LaunchedEffect(Unit) 触发
    }

    fun fetchInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val subResponse = api.getSubspeciesList()
                if (subResponse.code == 0 || subResponse.code == 200) {
                    _subspeciesList.value = subResponse.data ?: emptyList()
                }
            } catch (e: Exception) {
                _errorMsg.value = "加载初始数据失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitPlanting(request: PlantingRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.submitPlanting(request)
                if (response.code == 0 || response.code == 200) {
                    _submitSuccess.value = true
                } else {
                    _errorMsg.value = response.message
                }
            } catch (e: Exception) {
                _errorMsg.value = "提交失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMsg.value = null
    }

    fun clearSubmitSuccess() {
        _submitSuccess.value = false
    }
}
