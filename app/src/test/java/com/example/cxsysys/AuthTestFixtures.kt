package com.example.cxsysys

import android.content.SharedPreferences
import com.example.cxsysys.api.AuthApiService
import com.example.cxsysys.model.BaseResponse
import com.example.cxsysys.model.ChangePasswordRequest
import com.example.cxsysys.model.EnterpriseInfo
import com.example.cxsysys.model.EnterpriseMember
import com.example.cxsysys.model.LoginResponse
import com.example.cxsysys.model.PasswordLoginRequest
import com.example.cxsysys.model.SendSmsCodeRequest
import com.example.cxsysys.model.SmsLoginRequest
import com.example.cxsysys.model.UserInfo
import com.example.cxsysys.utils.BusinessSession
import okhttp3.RequestBody

open class StubAuthApiService : AuthApiService {
    override suspend fun sendSmsCode(request: SendSmsCodeRequest): BaseResponse<Any> = error("Unexpected sendSmsCode")
    override suspend fun loginBySms(request: SmsLoginRequest): BaseResponse<LoginResponse> = error("Unexpected loginBySms")
    override suspend fun loginByPassword(request: PasswordLoginRequest): BaseResponse<LoginResponse> = error("Unexpected loginByPassword")
    override suspend fun getCurrentUser(session: BusinessSession?): BaseResponse<UserInfo> = error("Unexpected getCurrentUser")
    override suspend fun updateAvatar(request: RequestBody, session: BusinessSession?): BaseResponse<UserInfo> = error("Unexpected updateAvatar")
    override suspend fun changePassword(request: ChangePasswordRequest, session: BusinessSession?): BaseResponse<Any> = error("Unexpected changePassword")
    override suspend fun getEnterprise(): BaseResponse<EnterpriseInfo> = error("Unexpected getEnterprise")
    override suspend fun getEnterpriseMembers(lastId: Int?, limit: Int?): BaseResponse<List<EnterpriseMember>> = error("Unexpected getEnterpriseMembers")
    override suspend fun logout(session: BusinessSession?): BaseResponse<Any> = error("Unexpected logout")
}

internal class InMemoryPreferences : SharedPreferences {
    private val values = mutableMapOf<String, Any>()

    override fun getAll(): MutableMap<String, *> = values.toMutableMap()
    override fun getString(key: String?, defValue: String?): String? = values[key] as? String ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        (values[key] as? Set<*>)?.filterIsInstance<String>()?.toMutableSet() ?: defValues?.toMutableSet()
    override fun getInt(key: String?, defValue: Int): Int = values[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = values[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = values[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = values[key] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = values.containsKey(key)
    override fun edit(): SharedPreferences.Editor = MemoryEditor()
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

    private inner class MemoryEditor : SharedPreferences.Editor {
        private val updates = mutableMapOf<String, Any?>()
        private var clear = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor = apply { updates[requireNotNull(key)] = value }
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = apply { updates[requireNotNull(key)] = values?.toSet() }
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = apply { updates[requireNotNull(key)] = value }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = apply { updates[requireNotNull(key)] = value }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = apply { updates[requireNotNull(key)] = value }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = apply { updates[requireNotNull(key)] = value }
        override fun remove(key: String?): SharedPreferences.Editor = apply { updates[requireNotNull(key)] = null }
        override fun clear(): SharedPreferences.Editor = apply { clear = true }
        override fun apply() { commit() }
        override fun commit(): Boolean {
            if (clear) values.clear()
            updates.forEach { (key, value) -> if (value == null) values.remove(key) else values[key] = value }
            return true
        }
    }
}
