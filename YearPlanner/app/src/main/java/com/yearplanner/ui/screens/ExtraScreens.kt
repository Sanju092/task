@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yearplanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yearplanner.data.model.WidgetKind
import com.yearplanner.ui.MainViewModel
import com.yearplanner.ui.components.*
import com.yearplanner.ui.theme.ThemeMode
import kotlin.math.roundToInt

@Composable
fun WidgetConfigScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedWidgetType by remember { mutableStateOf(WidgetKind.TODAY) }
    var selectedProject by remember { mutableStateOf<String?>(null) }
    var maxItems by remember { mutableStateOf(10) }
    var showCompleted by remember { mutableStateOf(false) }
    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    var sortByDate by remember { mutableStateOf(true) }
    var viewMode by remember { mutableStateOf(WidgetViewMode.AGENDA) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget Configuration", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Configure your home screen widget", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Widget Type Selector
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Widget Type", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WidgetTypeButton(
                                kind = WidgetKind.TODAY,
                                label = "Today",
                                icon = Icons.Default.Today,
                                selected = selectedWidgetType == WidgetKind.TODAY,
                                onClick = { selectedWidgetType = WidgetKind.TODAY }
                            )
                            WidgetTypeButton(
                                kind = WidgetKind.YEAR_OVERVIEW,
                                label = "Year Overview",
                                icon = Icons.Default.CalendarMonth,
                                selected = selectedWidgetType == WidgetKind.YEAR_OVERVIEW,
                                onClick = { selectedWidgetType = WidgetKind.YEAR_OVERVIEW }
                            )
                            WidgetTypeButton(
                                kind = WidgetKind.ANALYTICS,
                                label = "Analytics",
                                icon = Icons.Default.TrendingUp,
                                selected = selectedWidgetType == WidgetKind.ANALYTICS,
                                onClick = { selectedWidgetType = WidgetKind.ANALYTICS }
                            )
                            WidgetTypeButton(
                                kind = WidgetKind.YEAR_AGENDA,
                                label = "Year Agenda",
                                icon = Icons.Default.List,
                                selected = selectedWidgetType == WidgetKind.YEAR_AGENDA,
                                onClick = { selectedWidgetType = WidgetKind.YEAR_AGENDA }
                            )
                        }
                    }
                }
            }

            // Theme Selector
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Widget Theme", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeButton(
                                mode = ThemeMode.SYSTEM,
                                label = "System",
                                icon = Icons.Default.BrightnessAuto,
                                selected = themeMode == ThemeMode.SYSTEM,
                                onClick = { themeMode = ThemeMode.SYSTEM }
                            )
                            ThemeButton(
                                mode = ThemeMode.LIGHT,
                                label = "Light",
                                icon = Icons.Default.LightMode,
                                selected = themeMode == ThemeMode.LIGHT,
                                onClick = { themeMode = ThemeMode.LIGHT }
                            )
                            ThemeButton(
                                mode = ThemeMode.DARK,
                                label = "Dark",
                                icon = Icons.Default.DarkMode,
                                selected = themeMode == ThemeMode.DARK,
                                onClick = { themeMode = ThemeMode.DARK }
                            )
                        }
                    }
                }
            }

            // Project Filter (for Today & Agenda)
            if (selectedWidgetType in listOf(WidgetKind.TODAY, WidgetKind.YEAR_AGENDA)) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Project Filter", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Show tasks from specific project only", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            // Would populate from viewModel.getAllProjects()
                            OutlinedTextField(
                                value = selectedProject ?: "",
                                onValueChange = { selectedProject = if (it.isBlank()) null else it },
                                label = { Text("Project ID (optional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Max Items Slider
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Max Items", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("$maxItems", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = maxItems.toFloat(),
                            onValueChange = { maxItems = it.roundToInt() },
                            valueRange = 4f..20f,
                            steps = 16
                        )
                    }
                }
            }

            // Show Completed Toggle
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Show Completed Tasks", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Display finished tasks in widget", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = showCompleted,
                            onCheckedChange = { showCompleted = it }
                        )
                    }
                }
            }

            // View Mode (for Year Overview)
            if (selectedWidgetType == WidgetKind.YEAR_OVERVIEW) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("View Mode", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ViewModeButton(
                                    mode = WidgetViewMode.HEATMAP,
                                    label = "Heatmap",
                                    icon = Icons.Default.GridOn,
                                    selected = viewMode == WidgetViewMode.HEATMAP,
                                    onClick = { viewMode = WidgetViewMode.HEATMAP }
                                )
                                ViewModeButton(
                                    mode = WidgetViewMode.AGENDA,
                                    label = "Agenda",
                                    icon = Icons.Default.ListAlt,
                                    selected = viewMode == WidgetViewMode.AGENDA,
                                    onClick = { viewMode = WidgetViewMode.AGENDA }
                                )
                                ViewModeButton(
                                    mode = WidgetViewMode.DAY_GRID,
                                    label = "Day Grid",
                                    icon = Icons.Default.CalendarViewMonth,
                                    selected = viewMode == WidgetViewMode.DAY_GRID,
                                    onClick = { viewMode = WidgetViewMode.DAY_GRID }
                                )
                            }
                        }
                    }
                }
            }

            // Live Preview
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Live Preview", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        WidgetPreviewCard(
                            widgetType = selectedWidgetType,
                            projectFilter = selectedProject,
                            maxItems = maxItems,
                            showCompleted = showCompleted,
                            themeMode = themeMode,
                            viewMode = viewMode
                        )
                    }
                }
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        // Save to DataStore via WidgetPreferences
                        // This would use the widget ID from intent
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Configuration", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun RowScope.WidgetTypeButton(
    kind: WidgetKind,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
    
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RowScope.ThemeButton(
    mode: ThemeMode,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
    
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RowScope.ViewModeButton(
    mode: WidgetViewMode,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
    
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun WidgetPreviewCard(
    widgetType: WidgetKind,
    projectFilter: String?,
    maxItems: Int,
    showCompleted: Boolean,
    themeMode: ThemeMode,
    viewMode: WidgetViewMode
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(
                when (themeMode) {
                    ThemeMode.LIGHT -> MaterialTheme.colorScheme.surfaceContainerHighest
                    ThemeMode.DARK -> MaterialTheme.colorScheme.surfaceContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                },
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                when (widgetType) {
                    WidgetKind.TODAY -> Icons.Default.Today
                    WidgetKind.YEAR_OVERVIEW -> Icons.Default.CalendarMonth
                    WidgetKind.ANALYTICS -> Icons.Default.TrendingUp
                    WidgetKind.YEAR_AGENDA -> Icons.Default.List
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(widgetType.displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Preview with $maxItems items", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (projectFilter != null) {
                Text("Filtered: $projectFilter", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}



enum class WidgetViewMode {
    HEATMAP,
    AGENDA,
    DAY_GRID
}

@Composable
fun DayDetailScreen(
    year: Int,
    month: Int,
    day: Int,
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val date = java.time.LocalDate.of(year, month, day)
    val dateMillis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val tasks by viewModel.taskUseCases.getOccurrencesByDate(dateMillis).collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")), fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showQuickAdd(month = month, year = year) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add task")
                    }
                }
            )
        }
    ) { innerPadding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            if (tasks.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.EventAvailable,
                        title = "No tasks on this day",
                        subtitle = "Tap + to add a task for ${date.format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))}",
                        onAction = { viewModel.showQuickAdd(month = month, year = year) },
                        actionText = "+ Add Task"
                    )
                }
            } else {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onNavigate("task_detail?occurrenceId=${task.id}") },
                        onComplete = { viewModel.toggleOccurrence(task.id) },
                        onSnooze = { mins -> viewModel.snoozeOccurrence(task.id, mins) },
                        onReschedule = { target -> viewModel.rescheduleOccurrence(task.id, target) },
                        showActions = true
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val year = java.time.Year.now().value
    val yearSummary by viewModel.taskUseCases.getYearSummary(year).collectAsStateWithLifecycle(initialValue = null)
    val currentStreak = viewModel.currentStreak
    val longestStreak = viewModel.longestStreak

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            // Streak Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = "", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Streak", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$currentStreak days", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Longest", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$longestStreak days", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // Weekly sparkline
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (0..6).forEach { i ->
                                val dayCompleted = (i + currentStreak) % 7 < currentStreak
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .background(
                                            if (dayCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                            RoundedCornerShape(8.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Year Progress
            yearSummary?.let { summary ->
                item {
                    val totalYearTasks = summary.months.values.sumOf { it.totalTasks }
                    val completedYearTasks = summary.months.values.sumOf { it.completedTasks }
                    val percent = if (totalYearTasks > 0) (completedYearTasks * 100 / totalYearTasks) else 0

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Year $year Progress", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("$completedYearTasks of $totalYearTasks tasks completed ($percent%)", fontSize = 14.sp)
                                }
                                CircularProgressIndicator(
                                    progress = if (totalYearTasks > 0) completedYearTasks.toFloat() / totalYearTasks else 0f,
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 8.dp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = if (totalYearTasks > 0) completedYearTasks.toFloat() / totalYearTasks else 0f,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                }

                // Monthly breakdown
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly Breakdown", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                (1..12).forEach { month ->
                                    val monthSummary = summary.months[month]
                                    val total = monthSummary?.totalTasks ?: 0
                                    val completed = monthSummary?.completedTasks ?: 0
                                    val pct = if (total > 0) (completed * 100 / total) else 0
                                    val monthName = java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault())
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(monthName, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(48.dp))
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            LinearProgressIndicator(
                                                progress = if (total > 0) completed.toFloat() / total else 0f,
                                                modifier = Modifier.weight(1f),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                            )
                                            Text("$completed/$total ($pct%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(60.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: item {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}