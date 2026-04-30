package com.example.cxsysys.api

import com.example.cxsysys.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PlantingApiService {

    // ========== 药肥入库相关接口 ==========

    @POST("materialSupplier")
    suspend fun submitSupplier(@Body request: SupplierRequest): BaseResponse<Any>

    @GET("fertilizerSupplierList")
    suspend fun getFertilizerSupplierList(): BaseResponse<List<SupplierSimple>>

    @POST("fertilizer")
    suspend fun submitFertilizerInput(@Body request: FertilizerRequest): BaseResponse<Any>

    @GET("pesticideSupplierList")
    suspend fun getPesticideSupplierList(): BaseResponse<List<SupplierSimple>>

    @POST("pesticide")
    suspend fun submitPesticideInput(@Body request: PesticideRequest): BaseResponse<Any>

    // ========== 通用列表接口 ==========

    @GET("subspeciesList")
    suspend fun getSubspeciesList(): BaseResponse<List<Subspecies>>

    @GET("fieldList")
    suspend fun getFieldList(): BaseResponse<List<Field>>

    @GET("greenhouseList")
    suspend fun getGreenhouseList(): BaseResponse<List<Greenhouse>>

    @GET("fertilizerList")
    suspend fun getFertilizerList(): BaseResponse<List<Fertilizer>>

    @GET("pesticideList")
    suspend fun getPesticideList(): BaseResponse<List<Pesticide>>

    @GET("seedbedListByGhId")
    suspend fun getSeedbedListByGhId(@Query("greenhouse_id") greenhouseId: Int): BaseResponse<List<Seedbed>>

    @GET("plantationList")
    suspend fun getPlantationList(): BaseResponse<List<Plantation>>

    @GET("seedbedListByGh")
    suspend fun getSeedbedListByGh(
        @Query("greenhouse_qrcode") greenhouseQrcode: String? = null,
        @Query("greenhouse_code") greenhouseCode: String? = null
    ): BaseResponse<List<Seedbed>>

    // ========== 通用文件上传接口 ==========
    @retrofit2.http.Multipart
    @POST("upload")
    suspend fun uploadFile(@retrofit2.http.Part file: okhttp3.MultipartBody.Part): UploadResponse

    // ========== 录入提交接口 ==========

    @POST("sapling")
    suspend fun submitSapling(@Body request: SaplingRequest): BaseResponse<Any>

    @POST("plant")
    suspend fun submitPlanting(@Body request: PlantingRequest): BaseResponse<Any>

    // --- 施肥作业 ---
    @POST("fertilizeWork")
    suspend fun submitFertilizeWork(@Body request: FertilizeWorkRequest): BaseResponse<Any>

    // --- 施药作业 ---
    @POST("pesticideWork")
    suspend fun submitPesticideWork(@Body request: PesticideWorkRequest): BaseResponse<Any>

    @POST("disease")
    suspend fun submitDisease(@Body request: DiseaseRequest): BaseResponse<Any>

    @POST("plantGrowth")
    suspend fun submitPlantGrowth(@Body request: PlantGrowthRequest): BaseResponse<Any>

    @POST("irrigation")
    suspend fun submitIrrigation(@Body request: IrrigationRequest): BaseResponse<Any>

    @POST("pruning")
    suspend fun submitPruning(@Body request: PruningRequest): BaseResponse<Any>

    @POST("punch")
    suspend fun submitPunch(@Body request: PunchRequest): BaseResponse<Any>

    @POST("harvest")
    suspend fun submitHarvest(@Body request: HarvestRequest): BaseResponse<Any>
}
