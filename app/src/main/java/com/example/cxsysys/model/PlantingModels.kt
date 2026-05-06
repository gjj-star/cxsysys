package com.example.cxsysys.model

import com.google.gson.annotations.SerializedName

// ========== 通用文件上传响应模型 ==========
data class UploadResponse(
    val code: Int,
    val message: String,
    val data: UploadData?
)

data class UploadData(
    @SerializedName("file_id") val fileId: String,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("file_size") val fileSize: Long
)

// 通用返回结构
data class BaseResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null,
    val hasMore: Boolean? = null
)

// ========== 苗木管理模型 ==========

data class Plant(
    @SerializedName("plant_id") val plantId: String,
    @SerializedName("plant_qrcode") val plantQrcode: String,
    @SerializedName("subspecies_name") val subspeciesName: String,
    @SerializedName("plant_height") val plantHeight: String?,
    @SerializedName("field_code") val fieldCode: String,
    val status: String
)

data class PlantDetail(
    @SerializedName("subspecies_name") val subspeciesName: String,
    @SerializedName("plant_qrcode") val plantQrcode: String,
    @SerializedName("tree_age") val treeAge: String,
    @SerializedName("field_code") val fieldCode: String,
    val status: String
)

data class FarmingRecord(
    val fert: FertRecord?,
    val disease: DiseaseRecordItem?,
    val pest: PestRecord?,
    val irri: IrriRecord?,
    val prun: PrunRecord?,
    val plant: PlantRecord?
)

data class FertRecord(
    val hasRecord: Boolean,
    val name: String?,
    val date: String?,
    val dosage: String?,
    val method: String?
)

data class DiseaseRecordItem(
    val hasRecord: Boolean,
    val date: String?,
    val type: String?,
    val description: String?
)

data class PestRecord(
    val hasRecord: Boolean,
    val date: String?,
    val name: String?,
    val method: String?
)

data class IrriRecord(
    val hasRecord: Boolean,
    val date: String?,
    val period: String?,
    val method: String?
)

data class PrunRecord(
    val hasRecord: Boolean,
    val date: String?,
    val type: String?
)

data class PlantRecord(
    val hasRecord: Boolean,
    val date: String?,
    val depth: String?,
    @SerializedName("field_code") val fieldCode: String?
)

data class GrowthRecordItem(
    @SerializedName("record_id") val recordId: Int,
    @SerializedName("record_date") val recordDate: String,
    @SerializedName("tree_height") val treeHeight: Double,
    @SerializedName("ground_diameter") val groundDiameter: Double
)

data class PunchHarvestRecord(
    val punch: PunchRecordItem,
    val harvest: HarvestRecordItem
)

data class PunchRecordItem(
    val hasRecord: Boolean,
    @SerializedName("？") val unknownField: String?
)

data class HarvestRecordItem(
    val hasRecord: Boolean,
    val date: String,
    val weight: Double
)

// 打孔结香详情记录（来自 /punch/punching 接口）
data class PunchDetailRecord(
    val hasRecord: Boolean,
    val date: String?,
    val period: String?,
    val depth: Double?,
    val diameter: Double?,
    val pitch: Double?,
    val remark: String?
)

// 打孔结香详情响应包装
data class PunchDetailWrapper(
    val punch: PunchDetailRecord
)

// 采收香木详情记录（来自 /punch/harvest 接口）
data class HarvestDetailRecord(
    val hasRecord: Boolean,
    val date: String?,
    val weight: Double?
)

// 采收香木详情响应包装
data class HarvestDetailWrapper(
    val harvest: HarvestDetailRecord
)

// ========== 农事详情模型（来自各/farming/xxx接口） ==========

// 施肥详情（来自 /farming/fertilize）
data class FertDetailRecord(
    val hasRecord: Boolean,
    val name: String?,
    val date: String?,
    val dosage: String?,
    val method: String?,
    val water: String?,
    val remark: String?
)
data class FertDetailWrapper(val fert: FertDetailRecord)

// 病虫害详情（来自 /farming/disease）
data class DiseaseDetailRecord(
    val hasRecord: Boolean,
    val date: String?,
    val type: String?,
    val description: String?,
    @SerializedName("photo_url") val photoUrl: List<String>?
)
data class DiseaseDetailWrapper(val disease: DiseaseDetailRecord)

// 施药详情（来自 /farming/pesticide）
data class PestDetailRecord(
    val hasRecord: Boolean,
    val date: String?,
    val period: String?,
    val name: String?,
    val ppm: String?,
    val dosage: String?,
    val method: String?
)
data class PestDetailWrapper(val pest: PestDetailRecord)

// 灌溉详情（来自 /farming/irrigation）
data class IrriDetailRecord(
    val hasRecord: Boolean,
    val date: String?,
    val period: String?,
    val method: String?
)
data class IrriDetailWrapper(val irri: IrriDetailRecord)

// 剪枝详情（来自 /farming/pruning）
data class PrunDetailRecord(
    val hasRecord: Boolean,
    val date: String?,
    val period: String?,
    val type: String?,
    val tool: String?,
    val disinfect: String?,
    val remark: String?
)
data class PrunDetailWrapper(val prun: PrunDetailRecord)

// 苗木定植详情（来自 /farming/plant）
data class PlantingDetailRecord(
    val hasRecord: Boolean,
    val date: String?,
    @SerializedName("sapling_qrcode") val saplingQrcode: String?,
    val method: String?,
    val subspecies: String?,
    val generation: String?,
    val depth: String?,
    val width: String?,
    val distance: String?,
    @SerializedName("field_code") val fieldCode: String?
)
data class PlantingDetailWrapper(val plant: PlantingDetailRecord)

data class PlantRecordSearchItem(
    @SerializedName("plant_qrcode") val plantQrcode: String,
    val description: String
)

// ========== 大棚数据模型 ==========
data class Greenhouse(
    @SerializedName("greenhouse_id") val greenhouseId: Int,
    @SerializedName("greenhouse_code") val greenhouseCode: String
)

// 苗床数据模型
data class Seedbed(
    @SerializedName("seedbed_id") val seedbedId: Int,
    @SerializedName("seedbed_code") val seedbedCode: String,
    val status: Int? = null
)

// 品种细分数据模型
data class Subspecies(
    @SerializedName("enterprise_subspecies_id") val enterpriseSubspeciesId: Int,
    @SerializedName("subspecies_name") val subspeciesName: String,
    @SerializedName("subspecies_code") val subspeciesCode: String,
    @SerializedName("species_name") val speciesName: String,
    @SerializedName("subspecies_remark") val subspeciesRemark: String? = null
)

// 地块数据模型
data class Field(
    @SerializedName("field_id") val fieldId: Int,
    @SerializedName("field_code") val fieldCode: String,
    @SerializedName("field_qrcode") val fieldQrcode: String,
    @SerializedName("soil_type") val soilType: String,
    @SerializedName("field_area") val fieldArea: String,
    @SerializedName("field_status") val fieldStatus: String
)

// 肥料数据模型
data class Fertilizer(
    @SerializedName("fert_id") val fertId: Int,
    @SerializedName("fert_name") val fertName: String,
    @SerializedName("fert_supplier") val fertSupplier: String,
    @SerializedName("fert_type") val fertType: String,
    @SerializedName("nutrient_n") val nutrientN: Double,
    @SerializedName("nutrient_p") val nutrientP: Double,
    @SerializedName("nutrient_k") val nutrientK: Double,
    @SerializedName("photo_front_url") val photoFrontUrl: String? = null,
    @SerializedName("photo_back_url") val photoBackUrl: String? = null,
    val remark: String? = null
)

// 农药数据模型
data class Pesticide(
    @SerializedName("pest_id") val pestId: Int,
    @SerializedName("pest_name") val pestName: String,
    @SerializedName("pest_supplier") val pestSupplier: String,
    @SerializedName("pest_ingredient") val pestIngredient: String,
    @SerializedName("manufacture_date") val manufactureDate: String,
    @SerializedName("photo_front") val photoFront: String? = null,
    @SerializedName("photo_back") val photoBack: String? = null,
    val remark: String? = null
)

// 幼苗培育请求体
data class SaplingRequest(
    @SerializedName("seedbed_id") val seedbedId: Int,
    @SerializedName("mothertree_qrcode") val mothertreeQrcode: String? = null,
    @SerializedName("enterprise_subspecies_id") val enterpriseSubspeciesId: Int,
    val generation: String,
    @SerializedName("generation_way") val generationWay: String,
    @SerializedName("sapling_date") val saplingDate: String,
    @SerializedName("entry_date") val entryDate: String,
    @SerializedName("initial_quantity") val initialQuantity: Int
)

// 苗木定植请求体
data class PlantingRequest(
    @SerializedName("field_qrcode") val fieldQrcode: String? = null,
    @SerializedName("field_code") val fieldCode: String? = null,
    @SerializedName("mothertree_qrcode") val mothertreeQrcode: String? = null,
    @SerializedName("enterprise_subspecies_id") val enterpriseSubspeciesId: Int,
    @SerializedName("generation_way") val generationWay: String,
    val generation: String,
    @SerializedName("sapling_date") val saplingDate: String,
    @SerializedName("hole_depth") val holeDepth: Double,
    @SerializedName("hole_width") val holeWidth: Double,
    @SerializedName("plant_spacing") val plantSpacing: Double,
    val quantity: Int
)

// 施肥作业请求体
data class FertilizeWorkRequest(
    @SerializedName("field_qrcode") val fieldQrcode: String? = null,
    @SerializedName("field_code") val fieldCode: String? = null,
    @SerializedName("ferti_date") val fertiDate: String,
    @SerializedName("ferti_period") val fertiPeriod: String,
    @SerializedName("ferti_id") val fertiId: String,
    @SerializedName("ferti_dosage") val fertiDosage: Double,
    @SerializedName("ferti_method") val fertiMethod: String,
    @SerializedName("ferti_water") val fertiWater: String? = null,
    val remark: String? = null
)

// 施药作业请求体
data class PesticideWorkRequest(
    @SerializedName("plant_qrcode") val plantQrcode: String? = null,
    @SerializedName("field_qrcode") val fieldQrcode: String? = null,
    @SerializedName("field_code") val fieldCode: String? = null,
    val date: String,
    val period: String,
    @SerializedName("pest_id") val pestId: String,
    @SerializedName("pest_dosage") val pestDosage: Double,
    @SerializedName("pest_method") val pestMethod: String,
    @SerializedName("pest_water") val pestWater: Double,
    val record: String? = null
)

// 病虫害信息请求体
data class DiseaseRequest(
    @SerializedName("plant_qrcode") val plantQrcode: String? = null,
    @SerializedName("field_qrcode") val fieldQrcode: String? = null,
    @SerializedName("field_code") val fieldCode: String? = null,
    @SerializedName("record_date") val recordDate: String,
    @SerializedName("disease_type") val diseaseType: String,
    @SerializedName("disease_description") val diseaseDescription: String? = null,
    @SerializedName("disease_photo_url") val diseasePhotoUrl: List<String>? = null
)

// 生长记录请求体
data class PlantGrowthRequest(
    @SerializedName("plant_qrcode") val plantQrcode: String? = null,
    @SerializedName("field_qrcode") val fieldQrcode: String? = null,
    @SerializedName("field_code") val fieldCode: String? = null,
    @SerializedName("record_date") val recordDate: String,
    val height: Double,
    @SerializedName("crown_width") val crownWidth: Double,
    val diameter: Double,
    @SerializedName("chest_diameter") val chestDiameter: Double,
    val straightness: String,
    @SerializedName("plant_quantity") val plantQuantity: Int,
    @SerializedName("photo_url") val photoUrl: List<String>
)

// 灌溉记录请求体
data class IrrigationRequest(
    @SerializedName("plant_qrcode") val plantQrcode: String? = null,
    @SerializedName("field_qrcode") val fieldQrcode: String? = null,
    @SerializedName("field_code") val fieldCode: String? = null,
    @SerializedName("greenhouse_qrcode") val greenhouseQrcode: String? = null,
    @SerializedName("greenhouse_code") val greenhouseCode: String? = null,
    @SerializedName("seedbed_id") val seedbedId: String? = null,
    @SerializedName("irri_date") val irriDate: String,
    @SerializedName("irri_period") val irriPeriod: String,
    @SerializedName("irri_method") val irriMethod: String
)

// 剪枝记录请求体
data class PruningRequest(
    @SerializedName("plant_qrcode") val plantQrcode: String? = null,
    @SerializedName("field_qrcode") val fieldQrcode: String? = null,
    @SerializedName("field_code") val fieldCode: String? = null,
    @SerializedName("prun_date") val prunDate: String,
    @SerializedName("prun_period") val prunPeriod: String,
    @SerializedName("prun_type") val prunType: String,
    @SerializedName("prun_tool") val prunTool: String,
    @SerializedName("prun_disinfection") val prunDisinfection: String,
    val remark: String? = null
)

// 打孔结香请求体
data class PunchRequest(
    @SerializedName("plant_qrcode") val plantQrcode: String? = null,
    @SerializedName("field_qrcode") val fieldQrcode: String? = null,
    @SerializedName("field_code") val fieldCode: String? = null,
    @SerializedName("punch_date") val punchDate: String,
    @SerializedName("punch_period") val punchPeriod: String,
    @SerializedName("punch_depth") val punchDepth: Double,
    @SerializedName("punch_diameter") val punchDiameter: Double,
    @SerializedName("punch_pitch") val punchPitch: Double,
    val remark: String? = null
)

// 供应商请求体
data class SupplierRequest(
    @SerializedName("supplier_name") val supplierName: String,
    @SerializedName("supplier_address") val supplierAddress: String,
    @SerializedName("supplier_tel") val supplierTel: String,
    @SerializedName("supplier_class") val supplierClass: Int
)

// 肥料入库请求体
data class FertilizerRequest(
    @SerializedName("supplier_id") val supplierId: Int,
    @SerializedName("fert_name") val fertName: String,
    @SerializedName("fert_type") val fertType: String,
    @SerializedName("nutrient_n") val nutrientN: Double,
    @SerializedName("nutrient_p") val nutrientP: Double,
    @SerializedName("nutrient_k") val nutrientK: Double,
    val remark: String? = null,
    @SerializedName("front_photo_url") val frontPhotoUrl: List<String>,
    @SerializedName("back_photo_url") val backPhotoUrl: List<String>
)

// 农药入库请求体
data class PesticideRequest(
    @SerializedName("supplier_id") val supplierId: Int,
    @SerializedName("pest_name") val pestName: String,
    @SerializedName("pest_ingredient") val pestIngredient: String,
    @SerializedName("manufacture_date") val manufactureDate: String,
    val remark: String? = null,
    @SerializedName("front_photo_url") val frontPhotoUrl: List<String>,
    @SerializedName("back_photo_url") val backPhotoUrl: List<String>
)

// 供应商简要信息模型
data class SupplierSimple(
    @SerializedName("supplier_id") val supplierId: Int,
    @SerializedName("supplier_name") val supplierName: String,
    @SerializedName("supplier_tel") val supplierTel: String
)

// 采收香木请求体
data class HarvestRequest(
    @SerializedName("field_code") val fieldCode: String? = null,
    @SerializedName("field_qrcode") val fieldQrcode: String? = null,
    @SerializedName("plant_qrcode") val plantQrcode: String? = null,
    @SerializedName("harvest_date") val harvestDate: String,
    @SerializedName("harvest_weight") val harvestWeight: Double
)
