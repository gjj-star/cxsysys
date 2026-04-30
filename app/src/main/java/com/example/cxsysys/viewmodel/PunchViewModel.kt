package com.example.cxsysys.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.api.PlantingApiService
import com.example.cxsysys.model.PunchRequest
import com.example.cxsysys.utils.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PunchViewModel : ViewModel() {
    private val apiService = RetrofitClient.businessRetrofit.create(PlantingApiService::class.java)

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    fun submitPunch(
        plantQrcode: String?,
        fieldQrcode: String?,
        fieldCode: String?,
        punchDate: String,
        punchPeriod: String,
        punchDepth: Double,
        punchDiameter: Double,
        punchPitch: Double,
        remark: String?
    ) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            try {
                val request = PunchRequest(
                    plantQrcode = plantQrcode,
                    fieldQrcode = fieldQrcode,
                    fieldCode = fieldCode,
                    punchDate = punchDate,
                    punchPeriod = punchPeriod,
                    punchDepth = punchDepth,
                    punchDiameter = punchDiameter,
                    punchPitch = punchPitch,
                    remark = remark
                )
                
                val response = apiService.submitPunch(request)
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
