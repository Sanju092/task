package com.yearplanner.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yearplanner.ui.MainViewModel
import com.yearplanner.ui.screens.*

@Composable
fun AppNavHost(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "today"
    val showQuickAddDialog by viewModel.showQuickAddDialog
    val projects by viewModel.taskUseCases.getAllProjects().collectAsState(initial = emptyList())

    val showBottomBar = currentRoute in listOf("today", "year", "projects", "settings")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Today, contentDescription = "Today") },
                        label = { Text("Today") },
                        selected = currentRoute == "today",
                        onClick = {
                            navController.navigate("today") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Year") },
                        label = { Text("Year") },
                        selected = currentRoute == "year",
                        onClick = {
                            navController.navigate("year") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Projects") },
                        label = { Text("Projects") },
                        selected = currentRoute == "projects",
                        onClick = {
                            navController.navigate("projects") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == "settings",
                        onClick = {
                            navController.navigate("settings") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = "today") {
                composable("today") {
                    TodayScreen(
                        viewModel = viewModel,
                        onNavigate = { dest -> navController.navigate(dest) }
                    )
                }
                composable("year") {
                    YearScreen(
                        viewModel = viewModel,
                        onNavigate = { dest -> navController.navigate(dest) }
                    )
                }
                composable("month?year={year}&month={month}") { backStackEntry ->
                    val year = backStackEntry.arguments?.getString("year")?.toIntOrNull() ?: java.time.Year.now().value
                    val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: java.time.MonthDay.now().monthValue
                    MonthScreen(
                        year = year,
                        month = month,
                        viewModel = viewModel,
                        onNavigate = { dest -> navController.navigate(dest) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("projects") {
                    ProjectsScreen(
                        viewModel = viewModel,
                        onNavigate = { dest -> navController.navigate(dest) }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        viewModel = viewModel
                    )
                }
                composable("task_detail?occurrenceId={occurrenceId}") { backStackEntry ->
                    val occurrenceId = backStackEntry.arguments?.getString("occurrenceId") ?: ""
                    TaskDetailScreen(
                        occurrenceId = occurrenceId,
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    if (showQuickAddDialog) {
        QuickAddDialog(
            preselectedMonth = viewModel.quickAddMonth.value,
            preselectedYear = viewModel.quickAddYear.value,
            projects = projects,
            onDismiss = { viewModel.dismissQuickAdd() },
            onSave = { title, notes, projectId, priority, dateMillis, month, year, precision ->
                viewModel.createTask(
                    title = title,
                    notes = notes,
                    projectId = projectId,
                    priority = priority,
                    dateMillis = dateMillis,
                    month = month,
                    year = year,
                    precision = precision
                )
                viewModel.dismissQuickAdd()
            }
        )
    }
}
