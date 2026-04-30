package com.example.cxsysys.model

import com.google.gson.annotations.SerializedName

data class Plantation(
    @SerializedName("plantation_id") val plantationId: Int,
    @SerializedName("plantation_name") val plantationName: String,
    @SerializedName("plantation_code") val plantationCode: String
)
