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

    // ========== 苗木管理接口 ==========

    @GET("plantList")
    suspend fun getPlantList(
        @Query("plant_qrcode") plantQrcode: String? = null,
        @Query("field_id") fieldId: Int? = null,
        @Query("plant_date") plantDate: String? = null,
        @Query("lastid") lastid: Int? = null,
        @Query("limit") limit: Int? = null
    ): BaseResponse<List<Plant>>

    @GET("plants/{plant_id}")
    suspend fun getPlantDetail(
        @retrofit2.http.Path("plant_id") plantId: String
    ): BaseResponse<PlantDetail>

    @GET("plants/{plant_id}/farmingList")
    suspend fun getPlantFarmingList(
        @retrofit2.http.Path("plant_id") plantId: String
    ): BaseResponse<List<FarmingRecord>>

    @GET("plants/{plant_id}/growthList")
    suspend fun getPlantGrowthList(
        @retrofit2.http.Path("plant_id") plantId: String,
        @Query("lastid") lastid: Int? = null,
        @Query("limit") limit: Int? = null
    ): BaseResponse<List<GrowthRecordItem>>

    @GET("plants/{plant_id}/punchList")
    suspend fun getPlantPunchList(
        @retrofit2.http.Path("plant_id") plantId: String
    ): BaseResponse<List<PunchHarvestRecord>>

    @GET("plants/{plant_id}/punch/punching")
    suspend fun getPlantPunchDetail(
        @retrofit2.http.Path("plant_id") plantId: String
    ): BaseResponse<List<PunchDetailWrapper>>

    @GET("plants/{plant_id}/punch/harvest")
    suspend fun getPlantHarvestDetail(
        @retrofit2.http.Path("plant_id") plantId: String
    ): BaseResponse<List<HarvestDetailWrapper>>

    @GET("plants/{plant_id}/farming/fertilize")
    suspend fun getPlantFertDetail(
        @retrofit2.http.Path("plant_id") plantId: String
    ): BaseResponse<List<FertDetailWrapper>>

    @GET("plants/{plant_id}/farming/disease")
    suspend fun getPlantDiseaseDetail(
        @retrofit2.http.Path("plant_id") plantId: String
    ): BaseResponse<List<DiseaseDetailWrapper>>

    @GET("plants/{plant_id}/farming/pesticide")
    suspend fun getPlantPestDetail(
        @retrofit2.http.Path("plant_id") plantId: String
    ): BaseResponse<List<PestDetailWrapper>>

    @GET("plants/{plant_id}/farming/irrigation")
    suspend fun getPlantIrriDetail(
        @retrofit2.http.Path("plant_id") plantId: String
    ): BaseResponse<List<IrriDetailWrapper>>

    @GET("plants/{plant_id}/farming/pruning")
    suspend fun getPlantPrunDetail(
        @retrofit2.http.Path("plant_id") plantId: String
    ): BaseResponse<List<PrunDetailWrapper>>

    @GET("plants/{plant_id}/farming/plant")
    suspend fun getPlantPlantingDetail(
        @retrofit2.http.Path("plant_id") plantId: String
    ): BaseResponse<List<PlantingDetailWrapper>>

    @GET("plantRecordSearch")
    suspend fun getPlantRecordSearch(
        @Query("field_id") fieldId: Int,
        @Query("record_date") recordDate: String,
        @Query("type") type: String,
        @Query("limit") limit: Int? = null,
        @Query("lastid") lastid: Int? = null
    ): BaseResponse<List<PlantRecordSearchItem>>

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
