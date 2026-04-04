# 项目长期记忆 - 沉香溯源系统 (CXSYSYS)

## 项目概况
- **类型**: Android 原生 App（100% Jetpack Compose）
- **包名**: `com.example.cxsysys`
- **compileSdk**: 36, **minSdk**: 24
- **仓库**: https://gitee.com/kuo-jinjia/cxsysys.git
- **IDE**: Android Studio（Gradle Kotlin DSL）

## 技术栈
- UI: Jetpack Compose + Material3
- 导航: Navigation Compose 2.7.7
- 图标: material-icons-extended
- 网络层: Retrofit2 + OkHttp4 + Gson（2026-04-04 新增）

## 包结构
```
com.example.cxsysys/
├── api/           ← 网络接口定义
├── model/         ← 数据模型
├── utils/         ← 工具类（RetrofitClient 等）
├── viewmodel/     ← ViewModel
├── ui/
│   ├── components/    ← 公共组件
│   ├── screens/       ← 页面
│   │   ├── home/      ← 首页（HomeScreen.kt）
│   │   ├── children/  ← 子模块页面
│   │   ├── mother/    ← 母树管理
│   │   ├── mine/      ← 我的
│   │   └── plantation/← 农事作业
│   └── theme/         ← 主题色、字体
└── MainActivity.kt
```

## 关键文件说明
- **HomeScreen.kt**: 首页，包含 WeatherCard + 农事作业网格入口
- **MainScreen.kt**: 导航宿主，NavHost + BottomNavigation

## 天气功能（2026-04-04 完成）
- 接口: `GET https://uapis.cn/api/v1/misc/weather?adcode=440900`
- 返回结构: 平铺 JSON（无嵌套 data 字段），含 province/city/weather/temperature/humidity/wind_direction/wind_power/report_time
- 默认城市: 茂名市 (adcode=440900)
- 不需要 token，免费接口
- 涉及文件: WeatherResponse.kt, WeatherApiService.kt, RetrofitClient.kt, HomeViewModel.kt, HomeScreen.kt

## 打印模块
- 独立 module: `printerfeature`
- 开关: `enablePrinterModule` gradle 属性控制是否引入
