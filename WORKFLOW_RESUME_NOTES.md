# 沉香溯源系统 (CXSYSYS) - 项目经验与工作流记录

这份文档用于记录我们开发和重构“沉香溯源系统”过程中的核心工作流、技术难点及解决方案。你可以直接把这里的素材提取到你的简历中。

## 1. 项目概况与技术选型
- **项目背景**：沉香种植、生产、溯源的全流程管理移动端应用。
- **技术栈**：100% Kotlin，Jetpack Compose，Single Activity 架构。
- **核心依赖**：
  - 网络层：Retrofit2 + OkHttp4 + Gson + 协程 (Coroutines) + StateFlow
  - UI 框架：Jetpack Compose (Material 3)
  - 扫码库：CameraX + ZXing (适配国内无 GMS 环境)
  - 图片加载：Coil

## 2. 核心工作流与重构经验 (持续更新)

### 2.1 农事作业表单的 API 整合与级联选择
- **背景**：初期项目使用本地硬编码的模拟数据展示“种植园 - 大棚 - 苗床”的联动关系。
- **工作流/解决方案**：
  - 接入真实的业务 API (`/plantationList` 等)，通过 Retrofit 获取真实数据模型 (`Plantation`)。
  - 在 `ViewModel` 中使用 `StateFlow` 管理异步加载状态 (`isLoading`、`errorMsg` 等)，并在 Compose UI 层通过 `collectAsState()` 实现数据驱动的界面刷新。
  - **UX 细节优化**：重构了下拉组件 (`SaplingDropdownField`)。当父级（大棚）选择后若无子级（苗床）数据，下拉框依然保持可交互，并在内部明确提示“无空闲苗床”，而不是粗暴地禁用组件，提升了用户的探索体验。

### 2.2 真实环境下的二维码扫码方案设计
- **背景**：初期使用按钮点击模拟扫码结果。国内部分 Android 设备由于缺乏 GMS，Google ML Kit 扫码方案存在兼容性问题。
- **工作流/解决方案**：
  - 舍弃了依赖 GMS 的方案，选用了兼容性更强的 **ZXing** 结合 **CameraX**。
  - 封装了自定义的图像分析器 (`QrCodeAnalyzer`) 逐帧处理相机预览画面，并抽离了 `ScannerScreen` 与 `TopScanCard` 组件。
  - 实现了扫码结果与表单输入的无缝对接，支持条码数据自动转录入文本框，提高了现场农事作业的录入效率。

### 2.3 网络请求架构与多环境 API 管理
- **工作流/解决方案**：
  - 构建了单例的 `RetrofitClient`。
  - 将第三方 API（如气象接口 `https://uapis.cn/`）与自有业务 API（`https://dbcx.org.cn/plantingApi/`）通过不同的 OkHttpClient 实例进行分离。
  - 利用 OkHttp 拦截器 (Interceptor) 为业务请求统一注入鉴权 Header (`user-enterprise-id`)，降低了接口调用的代码冗余。

### 2.4 基于 Git 的版本号自动管理流水线
- **工作流/解决方案**：
  - 在 `build.gradle.kts` 中通过读取 Git 的 commit 数量与 Tag 信息，动态生成 `versionCode` 和 `versionName`。
  - 确立了无需手动修改配置文件的发版工作流（打 Tag -> 触发自动构建打包），降低了多分支开发时的版本号冲突风险。

---
*注：本文档会在后续的开发交流中，根据新增的功能和优化的难点随时更新。*
