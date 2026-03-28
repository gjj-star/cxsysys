java
├─ folder_structure.md
└─ com
└─ example
└─ cxsysys
├─ MainActivity.kt
├─ viewmodel
│  └─ viewmodel.md
├─ utils
│  └─ utils.md
├─ ui
│  ├─ MainScreen.kt
│  ├─ theme
│  │  ├─ Color.kt
│  │  ├─ Theme.kt
│  │  └─ Type.kt
│  ├─ screens
│  │  ├─ plantation
│  │  │  ├─ AgInputManagerScreen.kt
│  │  │  ├─ DiseasePestEntryScreen.kt
│  │  │  ├─ FertilizerEntryScreen.kt
│  │  │  ├─ GrowthEntryScreen.kt
│  │  │  ├─ HarvestEntryScreen.kt
│  │  │  ├─ IrrigationEntryScreen.kt
│  │  │  ├─ PesticideEntryScreen.kt
│  │  │  ├─ PlantingEntryScreen.kt
│  │  │  ├─ PruningEntryScreen.kt
│  │  │  ├─ PunchEntryScreen.kt
│  │  │  └─ SaplingEntryScreen.kt
│  │  ├─ mother
│  │  │  ├─ MotherDetailScreen.kt
│  │  │  └─ MotherScreen.kt
│  │  ├─ mine
│  │  │  └─ MineScreen.kt
│  │  ├─ home
│  │  │  └─ HomeScreen.kt
│  │  └─ children
│  │     ├─ ChildrenDetailScreen.kt
│  │     ├─ ChildrenScreen.kt
│  │     ├─ PlantDetailScreen.kt
│  │     └─ PlantingScreen.kt
│  └─ components
│     ├─ DualModeIdentifierField.kt
│     └─ TopScanCard.kt
├─ repository
│  └─ repository.md
├─ model
│  ├─ model.md
│  └─ request
│     └─ request.md
└─ api
└─ api.md

MainActivity.kt: 整个 Android 应用的唯一入口（Single Activity 架构），通常在这里设置 Compose 的内容（setContent）。
model/ (数据模型层): 存放实体类（Data Class）。包含从服务端接收的 JSON 对应的数据模型，或者请求参数（request/）。
api/ (网络接口层): 存放 Retrofit 的接口定义文件（Interface），用于声明 HTTP 请求（GET/POST 等）及对应的 URL 路径。
repository/ (数据仓库层): 作为数据中枢。它负责调用 api 获取网络数据，或者调用本地数据库获取本地数据，然后将数据处理后返回给 ViewModel。
viewmodel/ (视图模型层): 连接 UI 和 Repository 的桥梁。负责处理页面逻辑、发起网络请求，并将状态（State）暴露给 UI 层（Compose）监听。
utils/ (工具层): 存放全局通用的工具类，例如网络请求的单例配置（RetrofitClient）、时间格式化工具、常量等。
ui/ (用户界面层): 所有的 UI 代码（Jetpack Compose）。
MainScreen.kt: 包含导航栏（BottomNavigation）或抽屉的整体脚手架（Scaffold）。
theme/: 存放颜色、字体、黑暗/白天模式等主题配置。
components/: 存放跨页面复用的 UI 小组件（如顶部的扫码卡片、下拉框等）。
screens/: 按业务模块划分的具体页面（如 plantation、home 等）。