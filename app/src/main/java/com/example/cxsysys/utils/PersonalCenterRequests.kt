package com.example.cxsysys.utils

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun avatarUpdateBody(url: String?): RequestBody = JsonObject().apply {
    add("avatar_url", if (url == null) JsonNull.INSTANCE else JsonPrimitive(url))
}.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

fun passwordChangeError(oldPassword: String, newPassword: String, confirmPassword: String): String? = when {
    oldPassword.isEmpty() -> "请输入旧密码"
    newPassword.length < 6 -> "新密码至少6位"
    newPassword.toByteArray(Charsets.UTF_8).size > 72 -> "新密码不能超过72字节"
    newPassword != confirmPassword -> "两次新密码不一致"
    newPassword == oldPassword -> "新密码不能与旧密码相同"
    else -> null
}
