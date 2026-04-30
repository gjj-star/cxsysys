package com.example.cxsysys.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.api.PlantingApiService
import com.example.cxsysys.model.HarvestRequest
import com.example.cxsysys.utils.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HarvestViewModel : ViewModel() {
    private val apiService = RetrofitClient.businessRetrofit.create(PlantingApiService::class.java)

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    fun submitHarvest(
        fieldCode: String?,
        fieldQrcode: String?,
        plantQrcode: String?,
        harvestDate: String,
        harvestWeight: Double
    ) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            try {
                val request = HarvestRequest(
                    fieldCode = fieldCode,
                    fieldQrcode = fieldQrcode,
                    plantQrcode = plantQrcode,
                    harvestDate = harvestDate,
                    harvestWeight = harvestWeight
                )
                
                val response = apiService.submitHarvest(request)
                if (response.code == 200) {
                    _submitState.value = SubmitState.Success
                } else {
                    _submitState.value = SubmitState.Error(response.message ?: "提交失败")
                }
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e.message ?: "网络错误")
            }
        }
    }

    fun resetState() {
        _submitState.value = SubmitState.Idle
    }
}
