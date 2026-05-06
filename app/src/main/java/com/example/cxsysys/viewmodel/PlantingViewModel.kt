package com.example.cxsysys.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.model.*
import com.example.cxsysys.utils.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlantingViewModel : ViewModel() {

    private val api = RetrofitClient.businessRetrofit.create(com.example.cxsysys.api.PlantingApiService::class.java)

    // 苗木列表状态
    private val _plantList = MutableStateFlow<List<Plant>>(emptyList())
    val plantList: StateFlow<List<Plant>> = _plantList.asStateFlow()

    // 苗木详情基础信息
    private val _plantDetail = MutableStateFlow<PlantDetail?>(null)
    val plantDetail: StateFlow<PlantDetail?> = _plantDetail.asStateFlow()

    // 农事记录列表
    private val _farmingList = MutableStateFlow<List<FarmingRecord>>(emptyList())
    val farmingList: StateFlow<List<FarmingRecord>> = _farmingList.asStateFlow()

    // 生长记录列表
    private val _growthList = MutableStateFlow<List<GrowthRecordItem>>(emptyList())
    val growthList: StateFlow<List<GrowthRecordItem>> = _growthList.asStateFlow()

    // 结香采收记录列表
    private val _punchList = MutableStateFlow<List<PunchHarvestRecord>>(emptyList())
    val punchList: StateFlow<List<PunchHarvestRecord>> = _punchList.asStateFlow()

    // 打孔结香详情
    private val _punchDetail = MutableStateFlow<PunchDetailRecord?>(null)
    val punchDetail: StateFlow<PunchDetailRecord?> = _punchDetail.asStateFlow()

    // 采收香木详情
    private val _harvestDetail = MutableStateFlow<HarvestDetailRecord?>(null)
    val harvestDetail: StateFlow<HarvestDetailRecord?> = _harvestDetail.asStateFlow()

    // 农事详情（6个）
    private val _fertDetail = MutableStateFlow<FertDetailRecord?>(null)
    val fertDetail: StateFlow<FertDetailRecord?> = _fertDetail.asStateFlow()

    private val _diseaseDetail = MutableStateFlow<DiseaseDetailRecord?>(null)
    val diseaseDetail: StateFlow<DiseaseDetailRecord?> = _diseaseDetail.asStateFlow()

    private val _pestDetail = MutableStateFlow<PestDetailRecord?>(null)
    val pestDetail: StateFlow<PestDetailRecord?> = _pestDetail.asStateFlow()

    private val _irriDetail = MutableStateFlow<IrriDetailRecord?>(null)
    val irriDetail: StateFlow<IrriDetailRecord?> = _irriDetail.asStateFlow()

    private val _prunDetail = MutableStateFlow<PrunDetailRecord?>(null)
    val prunDetail: StateFlow<PrunDetailRecord?> = _prunDetail.asStateFlow()

    private val _plantingDetail = MutableStateFlow<PlantingDetailRecord?>(null)
    val plantingDetail: StateFlow<PlantingDetailRecord?> = _plantingDetail.asStateFlow()

    // 阶段记录搜索列表
    private val _stageRecordList = MutableStateFlow<List<PlantRecordSearchItem>>(emptyList())
    val stageRecordList: StateFlow<List<PlantRecordSearchItem>> = _stageRecordList.asStateFlow()

    private val _subspeciesList = MutableStateFlow<List<Subspecies>>(emptyList())
    val subspeciesList: StateFlow<List<Subspecies>> = _subspeciesList.asStateFlow()

    private val _fieldList = MutableStateFlow<List<Field>>(emptyList())
    val fieldList: StateFlow<List<Field>> = _fieldList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess.asStateFlow()

    // 获取苗木管理页初始所需选项数据和列表数据
    fun fetchInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 并发获取品种和地块供筛选使用，以及首屏苗木列表
                val subResponse = api.getSubspeciesList()
                if (subResponse.code == 0 || subResponse.code == 200) {
                    _subspeciesList.value = subResponse.data ?: emptyList()
                }
                
                val fieldResponse = api.getFieldList()
                if (fieldResponse.code == 0 || fieldResponse.code == 200) {
                    _fieldList.value = fieldResponse.data ?: emptyList()
                }

                fetchPlantList()
            } catch (e: Exception) {
                _errorMsg.value = "加载初始数据失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 获取苗木列表 (带筛选)
    fun fetchPlantList(
        qrcode: String? = null,
        fieldId: Int? = null,
        plantDate: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getPlantList(
                    plantQrcode = qrcode,
                    fieldId = fieldId,
                    plantDate = plantDate
                )
                if (response.code == 0 || response.code == 200) {
                    _plantList.value = response.data ?: emptyList()
                } else {
                    _errorMsg.value = response.message
                }
            } catch (e: Exception) {
                _errorMsg.value = "获取苗木列表失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 搜索指定阶段的苗木记录
    fun fetchPlantRecordSearch(fieldId: Int, recordDate: String, type: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getPlantRecordSearch(
                    fieldId = fieldId,
                    recordDate = recordDate,
                    type = type
                )
                if (response.code == 0 || response.code == 200) {
                    _stageRecordList.value = response.data ?: emptyList()
                } else {
                    _errorMsg.value = response.message
                }
            } catch (e: Exception) {
                _errorMsg.value = "阶段记录搜索失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 获取苗木详情页的所有数据
    fun fetchPlantDetailAll(plantId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val detailResponse = api.getPlantDetail(plantId)
                if (detailResponse.code == 0 || detailResponse.code == 200) {
                    _plantDetail.value = detailResponse.data
                }

                val farmingResponse = api.getPlantFarmingList(plantId)
                if (farmingResponse.code == 0 || farmingResponse.code == 200) {
                    _farmingList.value = farmingResponse.data ?: emptyList()
                }

                val growthResponse = api.getPlantGrowthList(plantId)
                if (growthResponse.code == 0 || growthResponse.code == 200) {
                    _growthList.value = growthResponse.data ?: emptyList()
                }

                val punchResponse = api.getPlantPunchList(plantId)
                if (punchResponse.code == 0 || punchResponse.code == 200) {
                    _punchList.value = punchResponse.data ?: emptyList()
                }

                // 获取打孔结香详情
                try {
                    val punchDetailResp = api.getPlantPunchDetail(plantId)
                    if (punchDetailResp.code == 0 || punchDetailResp.code == 200) {
                        _punchDetail.value = punchDetailResp.data?.firstOrNull()?.punch
                    }
                } catch (e: Exception) { /* 打孔详情可选 */ }

                // 获取采收香木详情
                try {
                    val harvestDetailResp = api.getPlantHarvestDetail(plantId)
                    if (harvestDetailResp.code == 0 || harvestDetailResp.code == 200) {
                        _harvestDetail.value = harvestDetailResp.data?.firstOrNull()?.harvest
                    }
                } catch (e: Exception) { /* 采收详情可选 */ }

                // 获取6个农事详情
                try {
                    val r1 = api.getPlantFertDetail(plantId)
                    if (r1.code == 0 || r1.code == 200) _fertDetail.value = r1.data?.firstOrNull()?.fert
                } catch (e: Exception) { }

                try {
                    val r2 = api.getPlantDiseaseDetail(plantId)
                    if (r2.code == 0 || r2.code == 200) _diseaseDetail.value = r2.data?.firstOrNull()?.disease
                } catch (e: Exception) { }

                try {
                    val r3 = api.getPlantPestDetail(plantId)
                    if (r3.code == 0 || r3.code == 200) _pestDetail.value = r3.data?.firstOrNull()?.pest
                } catch (e: Exception) { }

                try {
                    val r4 = api.getPlantIrriDetail(plantId)
                    if (r4.code == 0 || r4.code == 200) _irriDetail.value = r4.data?.firstOrNull()?.irri
                } catch (e: Exception) { }

                try {
                    val r5 = api.getPlantPrunDetail(plantId)
                    if (r5.code == 0 || r5.code == 200) _prunDetail.value = r5.data?.firstOrNull()?.prun
                } catch (e: Exception) { }

                try {
                    val r6 = api.getPlantPlantingDetail(plantId)
                    if (r6.code == 0 || r6.code == 200) _plantingDetail.value = r6.data?.firstOrNull()?.plant
                } catch (e: Exception) { }
            } catch (e: Exception) {
                _errorMsg.value = "获取详情数据失败: ${e.message}"
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
