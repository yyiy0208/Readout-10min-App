package com.readout10min.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.readout10min.ui.screens.HomeScreen
import com.readout10min.ui.screens.ContentLibraryScreen
import com.readout10min.ui.screens.ReadingPracticeScreen
import com.readout10min.ui.screens.ProgressRecordScreen
import com.readout10min.ui.theme.Purple80
import com.readout10min.ui.theme.SurfaceVariant
import com.readout10min.ui.theme.OnSurfaceVariant
import com.readout10min.ui.theme.OnSurface

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object ContentLibrary : Screen("content_library", "内容库", Icons.Default.Book)
    object ReadingPractice : Screen("reading_practice", "练习", Icons.Default.Refresh)
    object ProgressRecord : Screen("progress_record", "记录", Icons.Default.Settings)
    
    // 带参数的路由
    companion object {
        fun ReadingPracticeWithId(contentId: String) = "${ReadingPractice.route}/$contentId"
        fun ReadingPracticeWithIdAndParagraph(contentId: String, paragraphNumber: Int) = "${ReadingPractice.route}/$contentId?paragraph=$paragraphNumber"
    }
}

val screens = listOf(
    Screen.Home,
    Screen.ContentLibrary,
    Screen.ReadingPractice,
    Screen.ProgressRecord
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val isNavBarVisible = remember { mutableStateOf(true) }
    
    Scaffold(
        bottomBar = {
            if (isNavBarVisible.value) {
                BottomAppBar(
                    containerColor = SurfaceVariant,
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    screens.forEach {
                        val isSelected = when (it) {
                            Screen.ReadingPractice -> {
                                // 对于朗读页，检查当前路由是否以 reading_practice 开头
                                currentDestination?.route?.startsWith(Screen.ReadingPractice.route) == true
                            }
                            else -> {
                                currentDestination?.hierarchy?.any { destination -> destination.route == it.route } == true
                            }
                        }
                        
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (it == Screen.ContentLibrary && currentDestination?.route?.startsWith(Screen.ReadingPractice.route) == true) {
                                    // 从朗读页返回内容库时，使用 navigateUp 保持状态
                                    navController.navigateUp()
                                } else {
                                    navController.navigate(it.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(imageVector = it.icon, contentDescription = it.label) },
                            label = { Text(it.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Purple80,
                                selectedTextColor = Purple80,
                                unselectedIconColor = OnSurfaceVariant,
                                unselectedTextColor = OnSurfaceVariant,
                                indicatorColor = SurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) {innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                   HomeScreen(navController)
               }
            composable(Screen.ContentLibrary.route) {
                ContentLibraryScreen(navController)
            }
            composable(Screen.ReadingPractice.route) {
                ReadingPracticeScreen(navController, null, null, null, isNavBarVisible)
            }
            composable("${Screen.ReadingPractice.route}/{contentId}?paragraph={paragraph}&paragraphId={paragraphId}") {backStackEntry ->
                val contentId = backStackEntry.arguments?.getString("contentId")
                val paragraphNumber = backStackEntry.arguments?.getString("paragraph")?.toIntOrNull()
                val paragraphIdStr = backStackEntry.arguments?.getString("paragraphId")
                val paragraphId = try {
                    java.util.UUID.fromString(paragraphIdStr)
                } catch (e: Exception) {
                    null
                }
                val uuid = try {
                    java.util.UUID.fromString(contentId)
                } catch (e: Exception) {
                    null
                }
                ReadingPracticeScreen(navController, uuid, paragraphNumber, paragraphId, isNavBarVisible)
            }
            composable(Screen.ProgressRecord.route) {
                ProgressRecordScreen(navController)
            }
        }
    }
}
