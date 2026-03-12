# CXSYSYS

#### 介绍
沉香溯源系统app（测试版本控制）

#### 技术栈
- 开发语言: 100% Kotlin
- UI 框架: Jetpack Compose (完全弃用传统的 XML 布局)
- 架构思想: 单 Activity 架构 (Single Activity Architecture) + 声明式 UI


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
