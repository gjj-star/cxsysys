package com.example.cxsysys.model

import com.google.gson.annotations.SerializedName

data class EnterpriseInfo(
    @SerializedName("enterprise_id") val enterpriseId: Int? = null,
    @SerializedName("enterprise_name") val enterpriseName: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("address") val address: String? = null
)

data class EnterpriseMember(
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("user_name") val userName: String? = null,
    @SerializedName("real_name") val realName: String? = null,
    @SerializedName("roles") val roles: List<UserRole> = emptyList(),
    @SerializedName("super_admin") val superAdmin: Boolean = false
)
