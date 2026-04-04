// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// 引入版本管理脚本（基于 Git Tag 自动生成 versionName / versionCode）
apply(from = "version.gradle.kts")