package com.example.cxsysys.model

import com.google.gson.annotations.SerializedName

/**
 * 天气接口返回数据模型
 *
 * 接口地址: GET https://uapis.cn/api/v1/misc/weather?adcode=440900
 * 返回结构为平铺 JSON 对象（无嵌套 data 字段）
 */
data class WeatherResponse(
    @SerializedName("province")
    val province: String = "",

    @SerializedName("city")
    val city: String = "",

    @SerializedName("adcode")
    val adcode: String = "",

    @SerializedName("weather")
    val weather: String = "",

    @SerializedName("weather_icon")
    val weatherIcon: String = "",

    @SerializedName("temperature")
    val temperature: Int = 0,

    @SerializedName("wind_direction")
    val windDirection: String = "",

    @SerializedName("wind_power")
    val windPower: String = "",

    @SerializedName("humidity")
    val humidity: Int = 0,

    @SerializedName("report_time")
    val reportTime: String = ""
)
