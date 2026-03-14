package com.example.cxsysys.ui

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
// 引入主题色
import com.example.cxsysys.ui.theme.AgGreenPrimary
// 引入各个页面组件
import com.example.cxsysys.ui.screens.children.ChildrenScreen
import com.example.cxsysys.ui.screens.children.ChildrenDetailScreen
import com.example.cxsysys.ui.screens.children.PlantingScreen
import com.example.cxsysys.ui.screens.children.PlantDetailScreen
import com.example.cxsysys.ui.screens.home.HomeScreen
import com.example.cxsysys.ui.screens.mine.MineScreen
import com.example.cxsysys.ui.screens.mother.MotherScreen
import com.example.cxsysys.ui.screens.mother.MotherDetailScreen
import com.example.cxsysys.ui.screens.plantation.*

// 路由常量定义
const val ROUTE_HOME = "home"
const val ROUTE_MOTHER = "mother"
const val ROUTE_CHILDREN = "children" // 幼苗列表
const val ROUTE_PLANTING = "planting" // 苗木列表
const val ROUTE_MINE = "mine"

// 详情页路由
const val ROUTE_PLANT_DETAIL = "plant_detail/{plantId}"
// [只修改这里] 将参数名改为 seedbedId
const val ROUTE_SAPLING_DETAIL = "sapling_detail/{seedbedId}"
const val ROUTE_MOTHER_DETAIL = "mother_detail/{motherId}"

// 作业录入路由
const val ROUTE_SAPLING_ENTRY = "sapling_entry"       // 幼苗培育
const val ROUTE_PLANTING_ENTRY = "planting_entry"     // 苗木定植
const val ROUTE_GROWTH_ENTRY = "growth_entry"         // 生长记录
const val ROUTE_FERTILIZER_ENTRY = "fertilizer_entry" // 施肥作业
const val ROUTE_DISEASE_PEST_ENTRY = "disease_pest_entry" // 病虫害信息
const val ROUTE_PESTICIDE_ENTRY = "pesticide_entry"   // 施药信息
const val ROUTE_IRRIGATION_ENTRY = "irrigation_entry" // 灌溉记录
const val ROUTE_PRUNING_ENTRY = "pruning_entry"       // 剪枝记录
const val ROUTE_PUNCH_ADD = "punch_entry"             // 打孔结香
const val ROUTE_HARVEST_ADD = "harvest_entry"         // 采收香木

// 药肥基础信息录入路由
const val ROUTE_AG_INPUT_SUPPLIER = "ag_input_supplier"     // 供应商录入
const val ROUTE_AG_INPUT_PESTICIDE = "ag_input_pesticide"   // 农药录入
const val ROUTE_AG_INPUT_FERTILIZER = "ag_input_fertilizer" // 肥料录入

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    data object Home : BottomNavItem(ROUTE_HOME, "工作台", Icons.Default.Home)
    data object Mother : BottomNavItem(ROUTE_MOTHER, "母树", Icons.Default.Park)
    data object Children : BottomNavItem(ROUTE_CHILDREN, "幼苗", Icons.Default.Grass)
    data object Planting : BottomNavItem(ROUTE_PLANTING, "苗木", Icons.Default.Forest)
    data object Mine : BottomNavItem(ROUTE_MINE, "我的", Icons.Default.Person)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            val mainRoutes = listOf(ROUTE_HOME, ROUTE_MOTHER, ROUTE_CHILDREN, ROUTE_PLANTING, ROUTE_MINE)
            if (currentRoute in mainRoutes) {
                NavigationBar {
                    val items = listOf(
                        BottomNavItem.Home,
                        BottomNavItem.Mother,
                        BottomNavItem.Children,
                        BottomNavItem.Planting,
                        BottomNavItem.Mine
                    )
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            // [修改] 底部导航栏样式：选中为绿色，指示器为浅绿色
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AgGreenPrimary,
                                selectedTextColor = AgGreenPrimary,
                                indicatorColor = AgGreenPrimary.copy(alpha = 0.15f),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- 底部Tab页面 ---
            composable(ROUTE_HOME) {
                HomeScreen(
                    onNavigateToModule = { routeId ->
                        try {
                            navController.navigate(routeId)
                        } catch (e: Exception) {
                            Toast.makeText(context, "页面 $routeId 开发中", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            composable(ROUTE_MOTHER) {
                MotherScreen(
                    onNavigateToDetail = { motherId -> navController.navigate("mother_detail/$motherId") }
                )
            }
            composable(ROUTE_CHILDREN) {
                ChildrenScreen(
                    // [只修改这里] 传入 seedbedId
                    onNavigateToDetail = { seedbedId -> navController.navigate("sapling_detail/$seedbedId") }
                )
            }
            composable(ROUTE_PLANTING) {
                PlantingScreen(
                    onNavigateToDetail = { plantId -> navController.navigate("plant_detail/$plantId") }
                )
            }
            composable(ROUTE_MINE) { MineScreen() }

            // --- 农事作业录入页面 ---
            composable(ROUTE_SAPLING_ENTRY) { SaplingEntryScreen(onBackClick = { navController.popBackStack() }) }
            composable(ROUTE_PLANTING_ENTRY) { PlantingEntryScreen(onBackClick = { navController.popBackStack() }) }
            composable(ROUTE_GROWTH_ENTRY) { GrowthEntryScreen(onBackClick = { navController.popBackStack() }) }
            composable(ROUTE_FERTILIZER_ENTRY) { FertilizerEntryScreen(onBackClick = { navController.popBackStack() }) }
            composable(ROUTE_DISEASE_PEST_ENTRY) { DiseasePestEntryScreen(onBackClick = { navController.popBackStack() }) }
            composable(ROUTE_PESTICIDE_ENTRY) { PesticideEntryScreen(onBackClick = { navController.popBackStack() }) }
            composable(ROUTE_IRRIGATION_ENTRY) { IrrigationEntryScreen(onBackClick = { navController.popBackStack() }) }
            composable(ROUTE_PRUNING_ENTRY) { PruningEntryScreen(onBackClick = { navController.popBackStack() }) }
            composable(ROUTE_PUNCH_ADD) { PunchEntryScreen(onBackClick = { navController.popBackStack() }) }
            composable(ROUTE_HARVEST_ADD) { HarvestEntryScreen(onBackClick = { navController.popBackStack() }) }

            // 药肥基础信息录入页面
            composable(ROUTE_AG_INPUT_SUPPLIER) {
                AgInputManagerScreen(mode = "supplier", onBackClick = { navController.popBackStack() })
            }
            composable(ROUTE_AG_INPUT_PESTICIDE) {
                AgInputManagerScreen(mode = "pesticide", onBackClick = { navController.popBackStack() })
            }
            composable(ROUTE_AG_INPUT_FERTILIZER) {
                AgInputManagerScreen(mode = "fertilizer", onBackClick = { navController.popBackStack() })
            }

            // --- 详情页面 ---
            composable(
                route = ROUTE_PLANT_DETAIL,
                arguments = listOf(navArgument("plantId") { type = NavType.StringType })
            ) { backStackEntry ->
                val plantId = backStackEntry.arguments?.getString("plantId") ?: ""
                PlantDetailScreen(plantId = plantId, onBackClick = { navController.popBackStack() })
            }

            composable(
                route = ROUTE_SAPLING_DETAIL,
                // [只修改这里] 接收参数名改为 seedbedId
                arguments = listOf(navArgument("seedbedId") { type = NavType.StringType })
            ) { backStackEntry ->
                val seedbedId = backStackEntry.arguments?.getString("seedbedId") ?: ""
                ChildrenDetailScreen(seedbedId = seedbedId, onBackClick = { navController.popBackStack() })
            }

            composable(
                route = ROUTE_MOTHER_DETAIL,
                arguments = listOf(navArgument("motherId") { type = NavType.StringType })
            ) { backStackEntry ->
                val motherId = backStackEntry.arguments?.getString("motherId") ?: ""
                MotherDetailScreen(motherTreeId = motherId, onBackClick = { navController.popBackStack() })
            }
            // ... existing code ...
            composable("pesticide_entry") { // 你的实际路由名称可能略有不同
                PesticideEntryScreen(
                    onBackClick = { navController.popBackStack() },
                    // [新增] 传入具体的导航行为，跳转至农药入库页
                    onNavigateToPesticideAdd = {
                        navController.navigate("ag_input_pesticide") // 替换为你配置的“农药信息”对应路由名
                    }
                )
            }
            composable("fertilizer_entry") {
                FertilizerEntryScreen(
                    onBackClick = { navController.popBackStack() },
                    // [新增] 传入具体的导航行为，跳转至肥料入库页
                    onNavigateToFertilizerAdd = {
                        navController.navigate("ag_input_fertilizer") // 跳转到农资管理的 fertilizer 模式
                    }
                )
            }
        }
    }
}