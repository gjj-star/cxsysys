plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// 调试开关：是否启用打印模块。
// - true:  引入 :printerfeature（适合真机/ARM 调试打印功能）
// - false: 不引入 :printerfeature（适合 x86_64 模拟器或无打印 SDK 环境）
val enablePrinterModule = providers
    .gradleProperty("enablePrinterModule")
    .map { it.toBoolean() }
    .orElse(true)
    .get()

android {
    namespace = "com.example.cxsysys"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.cxsysys"
        minSdk = 24
        targetSdk = 36

        // 从 Git 自动读取版本号（由根目录 version.gradle.kts 生成）
        val version = rootProject.extra["versionInfo"] as Map<String, String>
        versionCode = version["versionCode"]!!.toInt()
        versionName = version["versionName"]!!

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // 1. 导航组件 (解决 MainScreen 里的 NavHost, rememberNavController 报错)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    // 2. 图标扩展包 (解决 Forest, Park, Spa, BugReport 等图标报错)
    // 注意：默认项目只包含基础图标，咱们用了好看的图标，所以需要加这个扩展包
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation(libs.androidx.activity.compose)
    // 网络请求
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.media3.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // 打印模块依赖按开关加载，便于不同平台切换调试
    if (enablePrinterModule) {
        implementation(project(":printerfeature"))
    }
}
