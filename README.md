# CXSYSYS

#### 介绍
沉香溯源系统 App

#### 技术栈
- 开发语言: 100% Kotlin
- UI 框架: Jetpack Compose (完全弃用传统的 XML 布局)
- 架构思想: 单 Activity 架构 (Single Activity Architecture) + 声明式 UI
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


#### 项目目录结构：
\---com
    \---example
        \---cxsysys
            |   MainActivity.kt
            |   
            \---ui
                |   MainScreen.kt
                |   
                +---components
                +---screens
                |   +---children
                |   |       ChildrenDetailScreen.kt
                |   |       ChildrenScreen.kt
                |   |       PlantDetailScreen.kt
                |   |       PlantingScreen.kt
                |   |       
                |   +---home
                |   |       HomeScreen.kt
                |   |       
                |   +---mine
                |   |       MineScreen.kt
                |   |       
                |   +---mother
                |   |       MotherDetailScreen.kt
                |   |       MotherScreen.kt
                |   |       
                |   \---plantation
                |           DiseasePestEntryScreen.kt
                |           FertilizerEntryScreen.kt
                |           GrowthEntryScreen.kt
                |           HarvestEntryScreen.kt
                |           IrrigationEntryScreen.kt
                |           PesticideEntryScreen.kt
                |           PlantingEntryScreen.kt
                |           PruningEntryScreen.kt
                |           PunchEntryScreen.kt
                |           SaplingEntryScreen.kt
                |           AgInputManagerScreen.kt
                \---theme
                        Color.kt
                        Theme.kt
                        Type.kt

文件解释：
1.  children文件夹：底部Tab“幼苗”“苗木”模块

- ChildrenDetailScreen.kt 幼苗详情页
- ChildrenScreen.kt 幼苗档案管理页
- PlantDetailScreen.kt 苗木详情页
- PlantingScreen.kt 苗木档案管理页


2.  home文件夹：底部Tab“工作台”模块
- HomeScreen.kt 主页（工作台）页面

3.  mine文件夹：底部Tab“我的”模块
- MineScreen.kt 个人主页（我的）页面

4.  mother文件夹：底部Tab“母树”模块

- MotherDetailScreen.kt 母树详情页
- MotherScreen.kt 母树资源库页
- 

5.  plantation 文件夹：管理工作台页面里的模块

- DiseasePestEntryScreen.kt 病虫害信息录入页
- FertilizerEntryScreen.kt 施肥作业录入页
- GrowthEntryScreen.kt 生长记录录入页
- HarvestEntryScreen.kt 采收香木录入页
- IrrigationEntryScreen.kt 灌溉记录录入页
- PesticideEntryScreen.kt 施药信息录入页
- PlantingEntryScreen.kt 苗木定植录入页
- PruningEntryScreen.kt 剪枝信息录入页
- PunchEntryScreen.kt 打孔结香录入页
- SaplingEntryScreen.kt 幼苗培育录入页
- AgInputManagerScreen.kt 药肥（包括供应商）信息录入页 
- * 包含：供应商录入、农药信息入库、肥料信息入库
- * @param mode 模式: "supplier", "pesticide", "fertilizer"


6.  MainScreen.kt 主入口路由配置

#### 安装教程

1.  xxxx
2.  xxxx
3.  xxxx

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
