package com.example.cxsysys.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.api.PlantingApiService
import com.example.cxsysys.model.IrrigationRequest
import com.example.cxsysys.model.Plantation
import com.example.cxsysys.model.Seedbed
import com.example.cxsysys.utils.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IrrigationViewModel : ViewModel() {
    private val apiService = RetrofitClient.businessRetrofit.create(PlantingApiService::class.java)

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    private val _plantationList = MutableStateFlow<List<Plantation>>(emptyList())
    val plantationList: StateFlow<List<Plantation>> = _plantationList.asStateFlow()

    private val _seedbedList = MutableStateFlow<List<Seedbed>>(emptyList())
    val seedbedList: StateFlow<List<Seedbed>> = _seedbedList.asStateFlow()

    fun fetchPlantationList() {
        viewModelScope.launch {
            try {
                val response = apiService.getPlantationList()
                if (response.code == 200 && response.data != null) {
                    _plantationList.value = response.data
                }
            } catch (e: Exception) {
                // Ignore or handle error
            }
        }
    }

    fun fetchSeedbedsByGh(greenhouseQrcode: String?, greenhouseCode: String?) {
        viewModelScope.launch {
            try {
                val response = apiService.getSeedbedListByGh(
                    greenhouseQrcode = if (greenhouseQrcode?.isNotEmpty() == true) greenhouseQrcode else null,
                    greenhouseCode = if (greenhouseCode?.isNotEmpty() == true) greenhouseCode else null
                )
                if (response.code == 200 && response.data != null) {
                    _seedbedList.value = response.data
                } else {
                    _seedbedList.value = emptyList()
                }
            } catch (e: Exception) {
                _seedbedList.value = emptyList()
            }
        }
    }

    fun clearSeedbeds() {
        _seedbedList.value = emptyList()
    }

    fun submitIrrigation(
        plantQrcode: String?,
        fieldQrcode: String?,
        fieldCode: String?,
        greenhouseQrcode: String?,
        greenhouseCode: String?,
        seedbedId: String?,
        irriDate: String,
        irriPeriod: String,
        irriMethod: String
    ) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            try {
                val request = IrrigationRequest(
                    plantQrcode = plantQrcode,
                    fieldQrcode = fieldQrcode,
                    fieldCode = fieldCode,
                    greenhouseQrcode = greenhouseQrcode,
                    greenhouseCode = greenhouseCode,
                    seedbedId = seedbedId,
                    irriDate = irriDate,
                    irriPeriod = irriPeriod,
                    irriMethod = irriMethod
                )
                
                val response = apiService.submitIrrigation(request)
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
