package com.example.printerfeature.data;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PrinterApiService {
    @GET("plantationList")
    Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantationOption>>> getPlantations();

    @GET("fieldList")
    Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldOption>>> getFields();

    @GET("greenhouseList")
    Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhouseOption>>> getGreenhouses();

    @GET("print/field")
    Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldPrintData>>> getFieldPrintData(
            @Query("plantation_id") int plantationId
    );

    @GET("print/field")
    Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.FieldPrintData>>> getFieldPrintData(
            @Query("field_code") String fieldCode
    );

    @GET("print/greenhouse")
    Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhousePrintData>>> getGreenhousePrintData(
            @Query("plantation_id") int plantationId
    );

    @GET("print/greenhouse")
    Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.GreenhousePrintData>>> getGreenhousePrintData(
            @Query("greenhouse_code") String greenhouseCode
    );

    @GET("print/seedbed")
    Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.SeedbedPrintData>>> getSeedbedPrintData(
            @Query("greenhouse_id") int greenhouseId
    );

    @GET("print/seedbed")
    Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.SeedbedPrintData>>> getSeedbedPrintData(
            @Query("seedbed_code") String seedbedCode
    );

    @GET("print/plant")
    Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>> getPlantPrintData(
            @Query("field_id") int fieldId,
            @Query("plant_date") String plantDate
    );

    @GET("print/plant")
    Call<PrinterApiModels.ApiResponse<List<PrinterApiModels.PlantPrintData>>> getPlantPrintData(
            @Query("plant_qrcode") String plantQrcode
    );
}
