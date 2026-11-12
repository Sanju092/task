package com.yearplanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yearplanner.ui.MainViewModel
import com.yearplanner.ui.screens.*

@Composable
fun AppNavHost(viewModel: MainViewModel) {
    val navController = rememberNavController()
    
    NavHost(navController, startDestination = "today") {
        composable("today") {
            TodayScreen(
                onNavigate = { dest -> viewModel.navigateTo(dest) },
                onQuickAdd = { viewModel.showQuickAdd() },
                taskUseCases = viewModel.taskUseCases
            )
        }
        composable("year") {
            YearScreen(
                selectedYear = viewModel.selectedYear,
                onYearChanged = { viewModel.selectedYear = it },
                onNavigate = { dest -> viewModel.navigateTo(dest) },
                taskUseCases = viewModel.taskUseCases
            )
        }
        composable("month?year={year}&month={month}") { backStackEntry ->
            val year = backStackEntry.arguments?.getString("year")?.toIntOrNull() ?: java.time.Year.now().value
            val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: java.time.MonthDay.now().monthValue
            MonthScreen(
                year = year,
                month = month,
                onNavigate = { dest -> viewModel.navigateTo(dest) },
                taskUseCases = viewModel.taskUseCases
            )
        }
        composable("projects") {
            ProjectsScreen(
                onNavigate = { dest -> viewModel.navigateTo(dest) },
                taskUseCases = viewModel.taskUseCases
            )
        }
        composable("settings") {
            SettingsScreen(onNavigate = { dest -> viewModel.navigateTo(dest) })
        }
        composable("task_detail?occurrenceId={occurrenceId}") { backStackEntry ->
            val occurrenceId = backStackEntry.arguments?.getString("occurrenceId") ?: ""
            TaskDetailScreen(
                occurrenceId = occurrenceId,
                onNavigate = { dest -> viewModel.navigateTo(dest) },
                taskUseCases = viewModel.taskUseCases
            )
        }
        composable("quick_add") {
            QuickAddScreen(
                onDismiss = { viewModel.dismissQuickAdd() },
                onNavigate = { dest -> viewModel.navigateTo(dest) }
            )
        }
    }
}
