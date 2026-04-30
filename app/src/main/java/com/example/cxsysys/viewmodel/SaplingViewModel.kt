package com.example.cxsysys.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.model.*
import com.example.cxsysys.utils.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SaplingViewModel : ViewModel() {

    private val api = RetrofitClient.businessRetrofit.create(com.example.cxsysys.api.PlantingApiService::class.java)

    // 数据源
    private val _greenhouseList = MutableStateFlow<List<Greenhouse>>(emptyList())
    val greenhouseList: StateFlow<List<Greenhouse>> = _greenhouseList.asStateFlow()

    private val _seedbedList = MutableStateFlow<List<Seedbed>>(emptyList())
    val seedbedList: StateFlow<List<Seedbed>> = _seedbedList.asStateFlow()

    private val _subspeciesList = MutableStateFlow<List<Subspecies>>(emptyList())
    val subspeciesList: StateFlow<List<Subspecies>> = _subspeciesList.asStateFlow()

    // 状态
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
                val ghResponse = api.getGreenhouseList()
                if (ghResponse.code == 0 || ghResponse.code == 200) {
                    _greenhouseList.value = ghResponse.data ?: emptyList()
                }

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

    fun fetchSeedbedsByGreenhouse(greenhouseId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getSeedbedListByGhId(greenhouseId)
                if (response.code == 0 || response.code == 200) {
                    // 过滤出空闲状态的苗床 status = 0
                    _seedbedList.value = response.data?.filter { it.status == 0 } ?: emptyList()
                } else {
                    _errorMsg.value = response.message
                }
            } catch (e: Exception) {
                _errorMsg.value = "加载苗床失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitSapling(request: SaplingRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.submitSapling(request)
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
