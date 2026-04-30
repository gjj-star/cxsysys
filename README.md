# CXSYSYS

#### 介绍
沉香溯源系统 App

#### 技术栈
- 开发语言: 100% Kotlin
- UI 框架: Jetpack Compose (完全弃用传统的 XML 布局)
- 架构思想: 单 Activity 架构 (Single Activity Architecture) + 声明式 UI
- 网络请求: Retrofit2 + OkHttp4 + Gson
- 扫码库: CameraX + ZXing
- 版本管理: 基于 Git Tag 自动生成（详见下方「版本号管理」章节）


---

## 📦 版本号管理

### 工作原理

本项目采用 **基于 Git 的自动版本号管理**，无需手动修改任何配置文件。

| 字段 | 来源 | 示例 | 说明 |
|------|------|------|------|
| **versionName** | 最近一个 Git Tag | `1.2.0` 或 `1.2.0+build.3.a1b2c3d` | 给人看的版本名 |
| **versionCode** | Git 总 commit 数 | `127` | 给 Android 系统用的递增整数 |

**核心逻辑：**
- 当代码正好在某个 tag 上 → 版本号为干净的 tag 名，如 **1.2.0**
- 当 tag 之后有新的 commit → 版本号追加构建信息，如 **1.2.0+build.3.a1b2c3d**
  - `3` = 距离 tag 有 3 个新 commit
  - `a1b2c3d` = 当前 commit 的短 hash

---

### 🚀 发版操作步骤（每次发版必做）

> 整个过程在 **Android Studio 图形界面**完成，不需要敲命令行。

#### 第 1 步：确保代码已提交

底部 **Git** 面板 → 点击 **Commit + Push**，把所有改动推送到 Gitee。

#### 第 2 步：打 Tag（标记新版本）

**方式 A — 通过 Log 视图（推荐）：**

1. 底部工具栏点 **Git** 标签页 → 切换到 **Log** 子标签
2. 在提交历史中，**右键点击最新的那个 commit**
3. 选择 **New Tag...**
4. 在弹窗输入标签名，例如：
   - `v1.1.0` ← 正式发布
   - `v1.1.0-beta.1` ← 测试版
5. 点 OK
6. 右键刚打的 tag → 选择 **Push Tag 'v1.x.x' at origin...**

```
示例：从 v1.0.0 发布 v1.1.0

  ● abc1234  feat: 新增天气功能    ← 右键这个 commit → New Tag → 输入 v1.1.0
  ▼ v1.0.0                        ← 上一次的 tag
  ● def5678  init project
```

**方式 B — 通过菜单栏：**

1. 顶部菜单 **VCS → Git → Tags...**
2. 点 **+** 号新建标签
3. 输入标签名 → OK
4. 选中列表中的 tag → 点 **Push Tag** 推送

#### 第 3 步：打包 APK

1. 菜单栏 **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. 等待编译完成
3. 点击弹出的通知 **locate** 找到生成的 APK 文件

#### 第 4 步：验证版本号

安装到手机后：
- **设置 → 应用 → 沉香溯源系统** 页面自动显示版本号
- 或者打开 App → 底部 Tab 切换到**「我的」** → 底部显示 **版本 x.x.x (Build xxx)**

---

### 🏷️ Tag 命名规范

| 格式 | 适用场景 | 示例 |
|------|----------|------|
| `v主版本.次版本.修订` | 正式发布 | `v1.0.0`, `v1.2.0`, `v2.0.0` |
| `v主版本.次版本.修订-beta.N` | 内测/测试版 | `v1.1.0-beta.1`, `v2.0.0-beta.2` |
| `v主版本.次版本.修订-rc.N` | 候选发布版 | `v1.2.0-rc.1` |

> ⚠️ **注意：** 输入的 tag 名称可以带 `v` 前缀也可以不带，最终显示的版本名会自动去掉前缀。推荐统一带 `v` 前缀。

---

### 📋 版本号变化示例

假设当前最新 tag 是 `v1.0.0`：

| 操作 | versionName | versionCode |
|------|-------------|-------------|
| 打完 tag `v1.0.0` 后立即打包 | `1.0.0` | `42` |
| 改了 1 个 commit，还没打新 tag | `1.0.0+build.1.a3f7b2c` | `43` |
| 又改了 2 个 commit | `1.0.0+build.3.d8e9f01` | `45` |
| 打了新 tag `v1.1.0` 后打包 | `1.1.0` | `46` |

---

### ❓ 常见问题

**Q: 忘记打 tag 会怎样？**
A: 版本号会显示为 `1.0.0+build.N.xxxxxxx`，带构建后缀，功能完全正常，只是不够好看。记得下次发版前补上 tag。

**Q: 可以手动指定版本号吗？**
A: 可以。直接改 `app/build.gradle.kts` 里 defaultConfig 中的 versionCode/versionName 就行。但不推荐，因为容易忘记更新。

**Q: versionCode 为什么用 commit 数而不是自己写数字？**
A: 因为 Android 要求 versionCode 只增不减。commit 数天然递增，永远不会冲突或回退。


---

## 📂 项目目录架构及重点文件说明

项目根目录和构建配置（`build.gradle.kts`, `settings.gradle.kts` 等）遵循标准的 Android 项目结构。
下面重点说明应用核心代码路径：**`app/src/main/java/com/example/cxsysys/`**

### `com.example.cxsysys/` 架构概览

遵循现代 Android 推荐架构，按功能职责分层：

```text
com.example.cxsysys/
├── api/           ← 网络接口层 (Retrofit service)
├── model/         ← 数据模型层 (Data class, 请求/响应实体)
├── repository/    ← 数据仓库层 (处理数据来源，当前暂空或按需使用)
├── ui/            ← UI 展示层 (Compose 页面、组件、主题)
├── utils/         ← 工具类层 (单例工具、拓展函数)
├── viewmodel/     ← 逻辑状态层 (Jetpack ViewModel, 桥接 UI 与数据)
└── MainActivity.kt ← 唯一 Activity 宿主
```

### 重点目录与文件说明

#### 1. `MainActivity.kt`
- **作用**：应用的单一 Activity 入口。
- **职责**：设置整个应用的 Compose 根节点，初始化整体主题 (`CXSYSYSTheme`)，并加载根路由 (`MainScreen`)。

#### 2. `ui/` 层
这里包含所有的界面呈现逻辑，完全由 Jetpack Compose 构建。

**`ui/MainScreen.kt`**
- 主入口路由配置，包含 `NavHost` 导航图和 `BottomNavigation` (底部导航栏)。连接四大底部模块（工作台、幼苗、苗木、我的）。

**`ui/theme/`**
- 定义全局的 UI 样式，包括 `Theme.kt` (亮色/暗色模式配置)、`Color.kt` (颜色常量，如 `AgGreenPrimary`) 和 `Type.kt` (排版规范)。

**`ui/components/`**
- 存放可复用的基础 UI 组件，例如 `ScannerScreen` (真实扫码界面组件)、`TopScanCard` (顶部扫码卡片)、`DualModeIdentifierField` 等，用于在各个页面中组合复用。

**`ui/screens/`** (页面目录，按业务模块划分)

*   **`home/` (工作台模块)**
    *   `HomeScreen.kt`: 首页/工作台页面，展示天气以及所有农事作业和资源管理的网格入口。
*   **`children/` (幼苗与苗木模块)**
    *   `ChildrenScreen.kt` & `ChildrenDetailScreen.kt`: 幼苗档案管理列表及详情。
    *   `PlantingScreen.kt` & `PlantDetailScreen.kt`: 苗木档案管理列表及详情。
*   **`mother/` (母树模块)**
    *   `MotherScreen.kt` & `MotherDetailScreen.kt`: 母树资源库列表页及详情。
*   **`mine/` (我的模块)**
    *   `MineScreen.kt`: 个人中心页面。
*   **`plantation/` (农事作业与录入模块 - 重点)**
    该目录下包含所有来自“工作台”的表单录入页面，涉及真实扫码和 API 数据提交：
    *   `AgInputManagerScreen.kt`: 药肥入库及供应商信息录入。根据 `mode` 参数区分 ("supplier", "pesticide", "fertilizer")。
    *   `SaplingEntryScreen.kt`: 幼苗培育录入页。
    *   `PlantingEntryScreen.kt`: 苗木定植录入页。
    *   `IrrigationEntryScreen.kt`: 灌溉记录录入页 (涉及种植园/大棚/苗床 API 级联选择)。
    *   `FertilizerEntryScreen.kt`: 施肥作业录入页。
    *   `PesticideEntryScreen.kt`: 施药信息录入页。
    *   `DiseasePestEntryScreen.kt`: 病虫害信息录入页。
    *   `GrowthEntryScreen.kt`: 生长记录录入页。
    *   `PruningEntryScreen.kt`: 剪枝信息录入页。
    *   `PunchEntryScreen.kt`: 打孔结香录入页。
    *   `HarvestEntryScreen.kt`: 采收香木录入页。

#### 3. `api/` 层
- 存放与后端接口交互的定义。
- `PlantingApiService.kt`: 使用 Retrofit 定义了网络请求的方法，比如 `@GET("/api/v1/misc/weather")`, `@GET("/plantationList")`, 及各种作业提交的 `@POST` 请求。

#### 4. `model/` 层
- `Plantation.kt`, `PlantingModels.kt` 等：存放 Kotlin Data Class。包括 API 请求实体 (Request) 和响应实体 (Response)。

#### 5. `viewmodel/` 层
- **核心逻辑枢纽**：每个主要的 `Screen` (尤其是 `plantation/` 下的表单页) 对应一个 ViewModel (如 `SaplingViewModel`, `IrrigationViewModel` 等)。
- **职责**：使用 Kotlin Coroutines (协程) 调用 `api/` 层获取数据，并将状态通过 `StateFlow` 暴露给 `ui/` 层进行响应式刷新，处理了加载中 (`isLoading`)、成功 (`submitSuccess`) 和失败提示 (`errorMsg`) 等逻辑。

#### 6. `utils/` 层
- `RetrofitClient.kt`: 单例配置，封装了 OkHttpClient 和 Retrofit 实例，统一配置全局的 Base URL (如 `https://dbcx.org.cn`)。
- `QrCodeAnalyzer.kt`: 配合 CameraX 和 ZXing 使用的图像分析器，专门负责从相机帧中解析二维码字符串。


---

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request
