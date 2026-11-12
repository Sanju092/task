package com.yearplanner.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yearplanner.data.model.PlanningPrecision
import com.yearplanner.data.model.TaskOccurrence
import com.yearplanner.ui.navigation.AppNavHost
import com.yearplanner.ui.theme.Theme
import com.yearplanner.ui.theme.ThemeMode
import com.yearplanner.widget.WidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            val themeMode by viewModel.themeMode
            Theme(themeMode = themeMode) {
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
    val taskUseCases: com.yearplanner.domain.usecase.TaskUseCases,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var selectedYear: Int = java.time.Year.now().value
    var selectedMonth: Int = java.time.MonthDay.now().monthValue
    var selectedOccurrenceId: String? = null
    
    val themeMode = mutableStateOf(ThemeMode.SYSTEM)
    val showQuickAddDialog = mutableStateOf(false)
    val quickAddMonth = mutableStateOf<Int?>(null)
    val quickAddYear = mutableStateOf<Int?>(null)

    // Streak tracking
    var currentStreak: Int = 0
    var longestStreak: Int = 0

    init {
        viewModelScope.launch {
            taskUseCases.seedIfNeeded()
            calculateStreaks()
            WidgetUpdater.updateAllWidgets(context)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        themeMode.value = mode
    }

    fun navigateToOccurrence(occurrenceId: String) {
        selectedOccurrenceId = occurrenceId
    }

    fun showQuickAdd(month: Int? = null, year: Int? = null) {
        quickAddMonth.value = month
        quickAddYear.value = year
        showQuickAddDialog.value = true
    }

    fun dismissQuickAdd() {
        showQuickAddDialog.value = false
        quickAddMonth.value = null
        quickAddYear.value = null
    }

    fun toggleOccurrence(occurrenceId: String) {
        viewModelScope.launch {
            taskUseCases.toggleOccurrenceCompletion(occurrenceId)
            calculateStreaks()
            WidgetUpdater.updateAllWidgets(context)
        }
    }

    fun deleteOccurrence(occurrenceId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            taskUseCases.deleteOccurrence(occurrenceId)
            calculateStreaks()
            WidgetUpdater.updateAllWidgets(context)
            onDone()
        }
    }

    fun snoozeOccurrence(occurrenceId: String, minutes: Int) {
        viewModelScope.launch {
            taskUseCases.snoozeOccurrence(occurrenceId, minutes)
            WidgetUpdater.updateAllWidgets(context)
        }
    }

    fun rescheduleOccurrence(occurrenceId: String, target: String) {
        viewModelScope.launch {
            taskUseCases.rescheduleOccurrence(occurrenceId, target)
            WidgetUpdater.updateAllWidgets(context)
        }
    }

    fun createTask(
        title: String,
        notes: String = "",
        projectId: String? = null,
        priority: Int = 0,
        dateMillis: Long? = null,
        month: Int? = null,
        year: Int? = null,
        precision: PlanningPrecision = PlanningPrecision.DAY
    ) {
        viewModelScope.launch {
            val task = taskUseCases.createTask(
                title = title,
                notes = notes,
                projectId = projectId,
                priority = priority
            )
            val zone = ZoneId.systemDefault()
            val finalDateMillis = dateMillis ?: if (precision == PlanningPrecision.DAY) {
                LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
            } else null

            taskUseCases.createOccurrence(
                taskId = task.id,
                originalOccurrenceKey = "task_${task.id}_${System.currentTimeMillis()}",
                planningPrecision = precision,
                plannedDate = finalDateMillis,
                plannedMonth = month,
                plannedYear = year,
                deadlineDate = finalDateMillis,
                zoneId = zone.id
            )
            calculateStreaks()
            WidgetUpdater.updateAllWidgets(context)
        }
    }

    fun createProject(name: String, colorToken: String = "blue") {
        viewModelScope.launch {
            taskUseCases.createProject(name = name, colorToken = colorToken)
            WidgetUpdater.updateAllWidgets(context)
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            taskUseCases.seedSampleData()
            calculateStreaks()
            WidgetUpdater.updateAllWidgets(context)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            taskUseCases.clearAllData()
            currentStreak = 0
            longestStreak = 0
            WidgetUpdater.updateAllWidgets(context)
        }
    }

    fun exportData(format: String) {
        viewModelScope.launch {
            taskUseCases.exportData(format)
        }
    }

    private fun calculateStreaks() {
        viewModelScope.launch {
            try {
                val (current, longest) = taskUseCases.calculateStreaks()
                currentStreak = current
                longestStreak = longest
            } catch (_: Exception) {}
        }
    }
}