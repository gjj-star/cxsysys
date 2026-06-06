# 沉香溯源系统 (CXSYSYS)

#### 介绍
沉香种植全流程溯源管理 App，涵盖母树管理、幼苗培育、苗木定植、农事作业、采收香木等环节。

---

## 技术栈

| 类别 | 技术选型 |
|------|----------|
| 开发语言 | 100% Kotlin |
| UI 框架 | Jetpack Compose + Material3 (BOM 2024.09.00) |
| 架构模式 | 单 Activity + MVVM (ViewModel + StateFlow) |
| 网络请求 | Retrofit2 + OkHttp4 + Gson |
| 认证方式 | JWT (自定义 `token` header) |
| 图片加载 | Coil |
| 扫码 | CameraX + ZXing |
| 版本管理 | 基于 Git Tag 自动生成 |

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

## 📂 项目目录架构

项目根目录和构建配置（`build.gradle.kts`, `settings.gradle.kts` 等）遵循标准的 Android 项目结构。
应用核心代码路径：**`app/src/main/java/com/example/cxsysys/`**

```text
com.example.cxsysys/
├── MainActivity.kt         ← 唯一 Activity 宿主
├── api/                    ← 网络接口层
│   ├── PlantingApiService.kt   种植/母树/作业相关接口
│   └── AuthApiService.kt        认证登录接口
├── model/                  ← 数据模型层
│   ├── PlantingModels.kt       种植+母树数据模型
│   ├── Plantation.kt           种植园/大棚/苗床模型
│   └── AuthModels.kt           认证请求/响应模型
├── ui/                     ← UI 展示层 (Compose)
│   ├── MainScreen.kt           导航图 + 底部Tab
│   ├── theme/                  主题/颜色/字体
│   ├── components/             可复用组件 (扫码、表单验证等)
│   └── screens/                各业务页面
│       ├── home/                 工作台
│       ├── mother/               母树管理
│       ├── children/            幼苗管理
│       ├── plantation/          苗木管理 + 农事作业录入
│       └── mine/                个人中心
├── utils/                  ← 工具类层
│   ├── RetrofitClient.kt       网络客户端 + JWT拦截器
│   ├── TokenManager.kt         Token持久化管理
│   ├── QrCodeAnalyzer.kt       扫码图像分析器
│   └── QrCodeGenerator.kt      二维码生成器
└── viewmodel/              ← 逻辑状态层
    ├── PlantingViewModel.kt     苗木管理
    ├── MotherTreeViewModel.kt   母树管理
    ├── SaplingViewModel.kt      幼苗管理
    ├── AuthViewModel.kt         认证状态
    └── ...                      各作业录入ViewModel
```

---

## 🧩 功能模块

### 底部 Tab 页面

| Tab | 模块 | 功能 |
|-----|------|------|
| 工作台 | HomeScreen | 天气展示 + 所有农事作业/资源管理入口网格 |
| 母树 | MotherScreen → MotherDetailScreen | 母树列表（搜索+分页）→ 详情（照片+编辑+状态变更） |
| 幼苗 | ChildrenScreen → ChildrenDetailScreen | 幼苗档案列表 → 详情 |
| 苗木 | PlantingScreen → PlantDetailScreen | 苗木档案列表 → 详情 + 农事记录 |
| 我的 | MineScreen | 个人中心 → 6个次级页面 |

### 农事作业录入（从工作台进入）

| 页面 | 功能 |
|------|------|
| SaplingEntryScreen | 幼苗培育录入 |
| PlantingEntryScreen | 苗木定植录入 |
| GrowthEntryScreen | 生长记录录入 |
| FertilizerEntryScreen | 施肥作业录入 |
| PesticideEntryScreen | 施药信息录入 |
| DiseasePestEntryScreen | 病虫害信息录入 |
| IrrigationEntryScreen | 灌溉记录录入 |
| PruningEntryScreen | 剪枝信息录入 |
| PunchEntryScreen | 打孔结香录入 |
| HarvestEntryScreen | 采收香木录入 |
| AgInputManagerScreen | 供应商/农药/肥料信息录入（按 mode 切换） |

### 个人中心次级页面

| 页面 | 功能 | 状态 |
|------|------|------|
| ProfileInfoScreen | 个人信息查看/编辑 | 占位，接口待对接 |
| EnterpriseScreen | 企业管理 + 成员管理 | 占位，接口待对接 |
| ChangePasswordScreen | 修改密码 | 占位，接口待对接 |
| SettingsScreen | 通知/同步/位置开关 + 缓存/备份/语言/主题 | 占位，接口待对接 |
| HelpFeedbackScreen | 帮助FAQ + 意见反馈 | 占位，接口待对接 |
| AboutScreen | 版本信息 + 开发者/版权 | 完整功能 |

---

## 🔗 API 接口

### 认证

| 接口 | 方法 | 说明 |
|------|------|------|
| `/smsCode` | POST | 获取短信验证码 |
| `/login/sms` | POST | 手机验证码登录 |
| `/login/password` | POST | 账号密码登录 |
| `/user` | GET | 获取用户信息 |

> 认证方式：JWT Token，通过自定义 `token` header 传递（非标准 `Authorization: Bearer`）

### 母树管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/mothertreeList` | GET | 母树列表（支持 keyword 搜索 + lastid 分页） |
| `/mothertrees/{id}` | GET | 母树详情 |
| `/mothertree` | POST | 新增母树 |
| `/mothertrees/{id}` | PUT | 修改信息 / 变更状态 |

### 苗木管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/plantationList` | GET | 种植园列表 |
| `/plant/{plantId}` | GET | 苗木详情 |
| `/plant/{plantId}/records` | GET | 苗木农事记录 |

---

## ⚙️ 开发配置

### Java 版本
AGP 8.13+ 需要 Java 17+，项目配置使用 Java 21（Zulu 发行版）。

### 超级管理员 Token
`gradle.properties` 中 `enableSuperToken=true` 时自动注入超级管理员 Token，免去开发阶段反复登录。生产构建应设为 `false`。

### 后端仓库
- Gitee: https://gitee.com/mrbanana16/agarwood-plantingApi.git

---

## 🏗️ 编译与运行

```bash
# 编译 Debug APK
./gradlew :app:assembleDebug

# 仅编译 Kotlin（快速验证代码）
./gradlew :app:compileDebugKotlin

# 编译 Release APK（需签名配置）
./gradlew :app:assembleRelease
```

---

## 参与贡献

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request
