package com.example.cxsysys.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cxsysys.api.PlantingApiService
import com.example.cxsysys.model.DictData
import com.example.cxsysys.model.DictType
import com.example.cxsysys.utils.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 数据字典全局管理 ViewModel
 *
 * 使用方式：
 *   val dictViewModel: DictViewModel = viewModel()  // 或在顶层共享
 *   val label = dictViewModel.getLabel("mothertree_status", "0")  // → "正常"
 *   val options = dictViewModel.getOptions("fertilizer_method")   // → List<String> 用于下拉
 *
 * 字典类型约定（dict_type）：
 *   - mothertree_status   母树状态
 *   - plant_status        苗木状态
 *   - fertilizer_method   施肥方式
 *   - fertilizer_type     肥料类型
 *   - pesticide_method    施药方式
 *   - pesticide_type      农药类型
 *   - irrigation_method   灌溉方式
 *   - irrigation_region   灌溉区域类型
 *   - pruning_type        剪枝类型
 *   - pruning_tool        剪枝工具
 *   - punch_type          打孔类型
 *   - growth_straightness 通直度
 *   - disease_pest_type   病虫害类型
 *   - time_slot           时间段
 *   - generation_way      繁育方式
 *   - supplier_type       供应商类型
 */
class DictViewModel : ViewModel() {

    private val api = RetrofitClient.businessRetrofit.create(PlantingApiService::class.java)

    // 字典缓存：dict_type -> DictType
    private val _dictCache = MutableStateFlow<Map<String, DictType>>(emptyMap())
    val dictCache: StateFlow<Map<String, DictType>> = _dictCache.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误消息
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    // 已请求过的 dict_type 集合，避免重复请求
    private val requestedTypes = mutableSetOf<String>()

    /**
     * 批量预加载多个字典类型
     * 在页面初始化时调用，一次网络请求获取多种字典数据
     */
    fun preloadDicts(vararg dictTypes: String) {
        val typesToLoad = dictTypes.filter { it !in requestedTypes }
        if (typesToLoad.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getDictBatch(typesToLoad.joinToString(","))
                if (response.code == 0 || response.code == 200) {
                    val data = response.data ?: emptyList()
                    val newMap = _dictCache.value.toMutableMap()
                    data.forEach { dict ->
                        newMap[dict.type] = dict
                        requestedTypes.add(dict.type)
                    }
                    _dictCache.value = newMap
                } else {
                    _errorMsg.value = response.message
                }
            } catch (e: Exception) {
                _errorMsg.value = "加载字典数据失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 获取单个字典类型（如果缓存没有则请求）
     */
    fun fetchDict(dictType: String) {
        if (dictType in requestedTypes) return
        viewModelScope.launch {
            try {
                val response = api.getDict(dictType)
                if (response.code == 0 || response.code == 200) {
                    val data = response.data ?: return@launch
                    val newMap = _dictCache.value.toMutableMap()
                    newMap[data.type] = data
                    requestedTypes.add(data.type)
                    _dictCache.value = newMap
                }
            } catch (e: Exception) {
                _errorMsg.value = "加载字典 $dictType 失败: ${e.message}"
            }
        }
    }

    /**
     * 根据 dict_type 和 data_value 获取中文标签
     * 例: getLabel("mothertree_status", "0") → "正常"
     */
    fun getLabel(dictType: String, value: String?): String {
        if (value == null) return "-"
        val dict = _dictCache.value[dictType] ?: return value
        return dict.dictData.find { it.dataValue == value }?.dataLabel ?: value
    }

    /**
     * 根据 dict_type 和 data_value (Int) 获取中文标签
     * 例: getLabel("mothertree_status", 0) → "正常"
     */
    fun getLabel(dictType: String, value: Int): String {
        return getLabel(dictType, value.toString())
    }

    /**
     * 获取某个字典类型的所有选项标签列表（用于下拉选择）
     * 例: getOptions("fertilizer_method") → ["穴施", "沟施", "撒施", ...]
     */
    fun getOptions(dictType: String): List<String> {
        val dict = _dictCache.value[dictType] ?: return emptyList()
        return dict.dictData.sortedBy { it.dataSort }.map { it.dataLabel }
    }

    /**
     * 获取某个字典类型的所有选项（value + label 对），用于提交时需要 value
     * 例: getOptionEntries("mothertree_status") → [("0","正常"), ("1","冻结"), ...]
     */
    fun getOptionEntries(dictType: String): List<Pair<String, String>> {
        val dict = _dictCache.value[dictType] ?: return emptyList()
        return dict.dictData.sortedBy { it.dataSort }.map { it.dataValue to it.dataLabel }
    }

    /**
     * 根据标签获取值（用于从下拉选项反向查找提交值）
     * 例: getValue("fertilizer_method", "穴施") → "1"
     */
    fun getValue(dictType: String, label: String): String? {
        val dict = _dictCache.value[dictType] ?: return null
        return dict.dictData.find { it.dataLabel == label }?.dataValue
    }

    /**
     * 构建状态映射 Map<Int, String>（兼容现有 STATUS_MAP 用法）
     * 例: getStatusMap("mothertree_status") → {0="正常", 1="冻结", 2="注销/死亡"}
     */
    fun getStatusMap(dictType: String): Map<Int, String> {
        val dict = _dictCache.value[dictType] ?: return emptyMap()
        return dict.dictData.associate { (it.dataValue.toIntOrNull() ?: 0) to it.dataLabel }
    }

    fun clearError() {
        _errorMsg.value = null
    }
}
