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

class MotherTreeViewModel : ViewModel() {

    private val api = RetrofitClient.businessRetrofit.create(PlantingApiService::class.java)

    // 母树列表
    private val _treeList = MutableStateFlow<List<MotherTreeItem>>(emptyList())
    val treeList: StateFlow<List<MotherTreeItem>> = _treeList.asStateFlow()

    // 是否还有更多数据
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    // 母树详情
    private val _treeDetail = MutableStateFlow<MotherTreeDetail?>(null)
    val treeDetail: StateFlow<MotherTreeDetail?> = _treeDetail.asStateFlow()

    // 品种列表（新增母树时选择品种用）
    private val _subspeciesList = MutableStateFlow<List<Subspecies>>(emptyList())
    val subspeciesList: StateFlow<List<Subspecies>> = _subspeciesList.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 是否正在加载更多（分页）
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // 错误消息
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    // 提交成功标志（新增/修改）
    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess.asStateFlow()

    // 搜索关键字
    private var currentKeyword: String? = null

    /**
     * 获取母树列表（首次加载或搜索）
     */
    fun fetchTreeList(keyword: String? = null) {
        currentKeyword = keyword
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getMotherTreeList(keyword = keyword, limit = 20)
                if (response.code == 0 || response.code == 200) {
                    val data = response.data
                    _treeList.value = data?.list ?: emptyList()
                    _hasMore.value = data?.hasMore ?: false
                } else {
                    _errorMsg.value = response.message
                }
            } catch (e: Exception) {
                _errorMsg.value = "获取母树列表失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载更多（分页）
     */
    fun loadMore() {
        if (_isLoadingMore.value || !_hasMore.value) return
        val lastId = _treeList.value.lastOrNull()?.mothertreeId ?: return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val response = api.getMotherTreeList(
                    keyword = currentKeyword,
                    lastid = lastId,
                    limit = 20
                )
                if (response.code == 0 || response.code == 200) {
                    val data = response.data
                    val newList = data?.list ?: emptyList()
                    _treeList.value = _treeList.value + newList
                    _hasMore.value = data?.hasMore ?: false
                } else {
                    _errorMsg.value = response.message
                }
            } catch (e: Exception) {
                _errorMsg.value = "加载更多失败: ${e.message}"
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * 获取母树详情
     */
    fun fetchTreeDetail(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getMotherTreeDetail(id)
                if (response.code == 0 || response.code == 200) {
                    _treeDetail.value = response.data?.firstOrNull()
                } else {
                    _errorMsg.value = response.message
                }
            } catch (e: Exception) {
                _errorMsg.value = "获取母树详情失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 获取品种列表（用于新增母树时的品种选择）
     */
    fun fetchSubspeciesList() {
        if (_subspeciesList.value.isNotEmpty()) return
        viewModelScope.launch {
            try {
                val response = api.getSubspeciesList()
                if (response.code == 0 || response.code == 200) {
                    _subspeciesList.value = response.data ?: emptyList()
                }
            } catch (e: Exception) {
                _errorMsg.value = "获取品种列表失败: ${e.message}"
            }
        }
    }

    /**
     * 新增母树
     */
    fun createMotherTree(request: MotherTreeCreateRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.createMotherTree(request)
                if (response.code == 0 || response.code == 200) {
                    _submitSuccess.value = true
                    // 刷新列表
                    fetchTreeList(keyword = currentKeyword)
                } else {
                    _errorMsg.value = response.message
                }
            } catch (e: Exception) {
                _errorMsg.value = "新增母树失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 修改母树信息
     */
    fun updateMotherTree(id: Int, request: MotherTreeUpdateRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.updateMotherTree(id, request)
                if (response.code == 0 || response.code == 200) {
                    _submitSuccess.value = true
                    // 刷新详情
                    fetchTreeDetail(id)
                } else {
                    _errorMsg.value = response.message
                }
            } catch (e: Exception) {
                _errorMsg.value = "修改母树失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 变更母树状态
     */
    fun updateMotherTreeStatus(id: Int, statusCode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.updateMotherTree(id, MotherTreeUpdateRequest(status = statusCode))
                if (response.code == 0 || response.code == 200) {
                    _submitSuccess.value = true
                    fetchTreeDetail(id)
                } else {
                    _errorMsg.value = response.message
                }
            } catch (e: Exception) {
                _errorMsg.value = "状态变更失败: ${e.message}"
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
