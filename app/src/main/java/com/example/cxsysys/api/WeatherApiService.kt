package com.example.cxsysys.api

import com.example.cxsysys.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("api/v1/misc/weather")
    suspend fun getWeather(
        @Query("adcode") adcode: String? = null
    ): WeatherResponse
}
