// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

val businessBaseUrl = providers.gradleProperty("BUSINESS_BASE_URL").get().trim()
val businessBaseUri = java.net.URI(businessBaseUrl)
require(businessBaseUri.scheme in setOf("http", "https") && !businessBaseUri.host.isNullOrBlank()
    && businessBaseUri.rawQuery == null && businessBaseUri.rawFragment == null) {
    "BUSINESS_BASE_URL must be an absolute HTTP(S) URL without a query or fragment"
}
extra["BUSINESS_BASE_URL"] = businessBaseUrl.trimEnd('/') + "/"

// 引入版本管理脚本（基于 Git Tag 自动生成 versionName / versionCode）
apply(from = "version.gradle.kts")
