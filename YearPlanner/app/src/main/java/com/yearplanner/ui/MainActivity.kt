package com.yearplanner.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yearplanner.ui.navigation.AppNavHost
import com.yearplanner.ui.theme.Theme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            Theme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            "com.yearplanner.action.VIEW_OCCURRENCE" -> {
                val occurrenceId = intent.getStringExtra("occurrence_id")
                occurrenceId?.let { viewModel.navigateToOccurrence(it) }
            }
            "com.yearplanner.action.QUICK_ADD" -> {
                viewModel.showQuickAdd()
            }
        }
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    val taskUseCases: com.yearplanner.domain.usecase.TaskUseCases
) : ViewModel() {

    var currentDestination: String = "today"
    var selectedYear: Int = java.time.Year.now().value
    var selectedMonth: Int = java.time.MonthDay.now().monthValue
    var selectedOccurrenceId: String? = null
    var showQuickAddDialog: Boolean = false

    fun navigateTo(destination: String) {
        currentDestination = destination
    }

    fun navigateToOccurrence(occurrenceId: String) {
        selectedOccurrenceId = occurrenceId
        currentDestination = "task_detail"
    }

    fun showQuickAdd() {
        showQuickAddDialog = true
    }

    fun dismissQuickAdd() {
        showQuickAddDialog = false
    }

    fun setSelectedYear(year: Int) {
        selectedYear = year
    }

    fun setSelectedMonth(month: Int) {
        selectedMonth = month
    }
}