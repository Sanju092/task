@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yearplanner.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearplanner.data.repository.MonthSummary
import com.yearplanner.data.repository.YearSummary
import com.yearplanner.data.model.OccurrenceStatus
import com.yearplanner.data.model.PlanningPrecision
import com.yearplanner.data.model.Project
import com.yearplanner.data.model.TaskOccurrence
import com.yearplanner.ui.MainViewModel
import com.yearplanner.ui.theme.ThemeMode
import com.yearplanner.ui.components.EmptyState
import com.yearplanner.ui.components.ProjectChip
import com.yearplanner.ui.components.StatCard
import com.yearplanner.ui.components.TaskCard
import com.yearplanner.ui.components.TaskSkeleton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ============================================================================
// Today Screen - Enhanced with animations and better UX
// ============================================================================

@Composable
fun TodayScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit
) {
    val today = LocalDate.now()
    val todayMillis = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val tomorrowMillis = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    val todayTasks by viewModel.taskUseCases.getOccurrencesByDateRange(todayMillis, tomorrowMillis - 1).collectAsStateWithLifecycle(initialValue = emptyList())
    val overdueTasks by viewModel.taskUseCases.getOverdueOccurrences(System.currentTimeMillis()).collectAsStateWithLifecycle(initialValue = emptyList())

    val completedToday = todayTasks.count { it.status == OccurrenceStatus.COMPLETED }
    val totalToday = todayTasks.size
    val overdueCount = overdueTasks.size

    // Filter out completed from today if not showing
    val pendingTodayTasks = todayTasks.filter { it.status != OccurrenceStatus.COMPLETED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Today", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                actions = {
                    IconButton(onClick = { viewModel.showQuickAdd() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add task")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showQuickAdd() },
                icon = { Icon(Icons.Default.Add, contentDescription = "Quick Add") },
                text = { Text("Add Task") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
            // Date header with animation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Day progress indicator
                    if (totalToday > 0) {
                        CircularProgressIndicator(
                            progress = completedToday.toFloat() / totalToday,
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Completed",
                        value = "$completedToday / $totalToday",
                        icon = Icons.Default.CheckCircle,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    if (overdueCount > 0) {
                        StatCard(
                            title = "Overdue",
                            value = overdueCount.toString(),
                            icon = Icons.Default.Warning,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                            trend = "+$overdueCount",
                            trendUp = false
                        )
                    }
                }
            }

            // Overdue section with red accent
            if (overdueTasks.isNotEmpty()) {
                item {
                    SectionHeader("[OVERDUE] Overdue", overdueCount, MaterialTheme.colorScheme.error)
                }
                items(overdueTasks) { task ->
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

            // Today's tasks
            item {
                SectionHeader("[TODAY] Scheduled for Today", pendingTodayTasks.size)
            }

            if (pendingTodayTasks.isEmpty() && overdueTasks.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.EventAvailable,
                        title = "No tasks for today",
                        subtitle = "Tap + to add a task or load sample plan from Settings",
                        onAction = { viewModel.showQuickAdd() },
                        actionText = "+ Add Task"
                    )
                }
            } else {
                items(pendingTodayTasks) { task ->
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

// ============================================================================
// Year Screen - Enhanced with better visuals
// ============================================================================

@Composable
fun YearScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit
) {
    var selectedYear by remember { mutableStateOf(viewModel.selectedYear) }
    val yearSummary by viewModel.taskUseCases.getYearSummary(selectedYear).collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$selectedYear", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            selectedYear -= 1
                            viewModel.selectedYear = selectedYear
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous year")
                        }
                        IconButton(onClick = {
                            selectedYear += 1
                            viewModel.selectedYear = selectedYear
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next year")
                        }
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                actions = {
                    IconButton(onClick = { viewModel.showQuickAdd() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add task")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showQuickAdd() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        }
    ) { innerPadding ->
        val summary = yearSummary
        if (summary != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Annual progress card
                item {
                    val totalYearTasks = summary.months.values.sumOf { it.totalTasks }
                    val completedYearTasks = summary.months.values.sumOf { it.completedTasks }
                    val percent = if (totalYearTasks > 0) (completedYearTasks * 100 / totalYearTasks) else 0
                    
                    // Streak calculation would come from viewModel
                    val currentStreak = viewModel.currentStreak
                    val longestStreak = viewModel.longestStreak
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Annual Progress", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$completedYearTasks of $totalYearTasks tasks completed ($percent%)", fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocalFireDepartment, contentDescription = "", tint = MaterialTheme.colorScheme.tertiary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("[FIRE] $currentStreak days", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.EmojiEvents, contentDescription = "", tint = Color(0xFFFFD600))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("[TROPHY] $longestStreak days", fontSize = 12.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = if (totalYearTasks > 0) completedYearTasks.toFloat() / totalYearTasks else 0f,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    }
                }

                // Month grid
                items((1..12).toList()) { month ->
                    val monthSummary = summary.months[month]
                    MonthSummaryCard(
                        month = month,
                        year = selectedYear,
                        summary = monthSummary,
                        onClick = { onNavigate("month?year=$selectedYear&month=$month") }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

// ============================================================================
// Month Screen - Enhanced
// ============================================================================

@Composable
fun MonthScreen(
    year: Int,
    month: Int,
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val tasks by viewModel.taskUseCases.getOccurrencesByMonth(year, month).collectAsStateWithLifecycle(initialValue = emptyList())
    val monthName = java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())

    val (completedCount, totalCount) = remember(tasks) {
        tasks.fold(Pair(0, 0)) { acc, t ->
            val c = if (t.status == OccurrenceStatus.COMPLETED) 1 else 0
            acc.copy(first = acc.first + c, second = acc.second + 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$monthName $year", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        if (totalCount > 0) {
                            CircularProgressIndicator(
                                progress = completedCount.toFloat() / totalCount,
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showQuickAdd(month = month, year = year) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add task for $monthName")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showQuickAdd(month = month, year = year) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add task") },
                text = { Text("Add Task") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (tasks.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.CalendarMonth,
                        title = "No tasks in $monthName $year",
                        subtitle = "Plan ahead by scheduling milestones or goals for this month",
                        onAction = { viewModel.showQuickAdd(month = month, year = year) },
                        actionText = "+ Add Task to $monthName"
                    )
                }
            } else {
                // Month stats
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard("Done", "$completedCount", Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                        StatCard("Total", "$totalCount", Icons.Default.List, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                        StatCard("Rate", "${if (totalCount > 0) (completedCount * 100 / totalCount) else 0}%", Icons.Default.TrendingUp, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                    }
                }
                
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

// ============================================================================
// Projects Screen - Enhanced with progress and goals
// ============================================================================

@Composable
fun ProjectsScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit
) {
    val projects by viewModel.taskUseCases.getAllProjects().collectAsStateWithLifecycle(initialValue = emptyList())
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projects & Goals", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                actions = {
                    IconButton(onClick = { showNewProjectDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add project")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewProjectDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add project") },
                text = { Text("New Project") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Organize your yearly roadmap by project, domain, or life pillar.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (projects.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.FolderOpen,
                        title = "No projects yet",
                        subtitle = "Create your first project (e.g. Work, Health, Personal, Learning)",
                        onAction = { showNewProjectDialog = true },
                        actionText = "+ Create Project"
                    )
                }
            } else {
                items(projects) { project ->
                    // Would need to pass task counts from viewModel
                    ProjectChip(
                        project = project,
                        onClick = { /* navigate to project tasks */ },
                        showTaskCount = true,
                        taskCount = 0, // Would come from viewModel
                        completedCount = 0
                    )
                }
            }
        }
    }

    if (showNewProjectDialog) {
        CreateProjectDialog(
            onDismiss = { showNewProjectDialog = false },
            onSave = { name, color ->
                viewModel.createProject(name, color)
                showNewProjectDialog = false
            }
        )
    }
}

// ============================================================================
// Settings Screen - Enhanced with more options
// ============================================================================

@Composable
fun SettingsScreen(
    viewModel: MainViewModel
) {
    val currentThemeMode by viewModel.themeMode
    var showClearConfirm by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Preferences", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Theme Section
            item {
                SettingsSection("Theme & Appearance") {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Color Theme", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            RadioOption("System Default", ThemeMode.SYSTEM, currentThemeMode, viewModel::setThemeMode)
                            RadioOption("Light Mode", ThemeMode.LIGHT, currentThemeMode, viewModel::setThemeMode)
                            RadioOption("Dark Mode (OLED)", ThemeMode.DARK, currentThemeMode, viewModel::setThemeMode)
                        }
                    }
                }
            }

            // Widget Settings
            item {
                SettingsSection("Home Screen Widgets") {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Widget Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Long-press home screen -> Widgets -> Year Planner to add Today, Year Overview, Analytics, or Year Agenda widgets.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { /* open widget settings */ }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Settings, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Configure Widget Instances")
                            }
                        }
                    }
                }
            }

            // Reminders Section
            item {
                SettingsSection("Reminders & Notifications") {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Notification Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Configure when and how you receive task reminders.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { /* open notification settings */ }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Notifications, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Manage Notifications")
                            }
                        }
                    }
                }
            }

            // Data Management
            item {
                SettingsSection("Data Management") {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Load Sample Data
                            Text("Populate Year Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Load pre-configured sample projects (Work, Health, Personal, Learning) and tasks across all 12 months.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    viewModel.loadSampleData()
                                    snackbarMessage = "Sample 12-month plan loaded successfully!"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Load 12-Month Sample Plan")
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Export/Import
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(onClick = { showExportDialog = true }, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Export (JSON/CSV)")
                                }
                                OutlinedButton(onClick = { showImportDialog = true }, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Upload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Import Data")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Reset Data
                            Text("Reset Data", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Clear all tasks, occurrences, and projects from local database.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { showClearConfirm = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear All Local Data")
                            }
                        }
                    }
                }
            }

            // About Section
            item {
                SettingsSection("About Year Planner") {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Year Planner", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Version 1.0.0", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                """A high-performance yearly planner and goal tracking app built with Jetpack Compose, Room Database, and Glance Home Widgets.

Features: Year-at-a-glance heatmap, offline-first, no account required, CSV/JSON backup, lock screen widgets.""",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Data?") },
            text = { Text("Are you sure you want to delete all tasks, occurrences, and projects? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirm = false
                        snackbarMessage = "All data cleared."
                    }
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data") },
            text = { Text("""Choose export format:

* JSON - Full fidelity backup
* CSV - Spreadsheet compatible""") },
            confirmButton = {
                Button(onClick = { viewModel.exportData("json"); showExportDialog = false }) {
                    Text("Export JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = true; showExportDialog = false }) {
                    Text("Import Instead")
                }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Data") },
            text = { Text("""Select a JSON or CSV file to import.

Note: Import will merge with existing data.""") },
            confirmButton = {
                Button(onClick = { /* open file picker */ showImportDialog = false }) {
                    Text("Choose File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (snackbarMessage != null) {
        AlertDialog(
            onDismissRequest = { snackbarMessage = null },
            title = { Text("Notice") },
            text = { Text(snackbarMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { snackbarMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

// ============================================================================
// Task Detail Screen - Enhanced
// ============================================================================

@Composable
fun TaskDetailScreen(
    occurrenceId: String,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val occurrence by viewModel.taskUseCases.getOccurrence(occurrenceId).collectAsStateWithLifecycle(initialValue = null)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        val occ = occurrence
        if (occ != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header card with status and key info
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = occ.title.ifBlank { "Untitled Task" },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            // Status badge
                            val isCompleted = occ.status == OccurrenceStatus.COMPLETED
                            SuggestionChip(
                                onClick = { },
                                label = { Text(if (isCompleted) "Completed" else "Pending") },
                                icon = {
                                    Icon(
                                        if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Planning info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InfoChip(
                                icon = Icons.Default.CalendarToday,
                                label = "Planning",
                                value = occ.planningPrecision.name
                            )
                            if (occ.plannedDate != null) {
                                val dateStr = java.time.Instant.ofEpochMilli(occ.plannedDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                InfoChip(
                                    icon = Icons.Default.Event,
                                    label = "Scheduled",
                                    value = dateStr.format(DateTimeFormatter.ofPattern("MMM d"))
                                )
                            }
                            if (occ.deadlineDate != null) {
                                val deadlineStr = java.time.Instant.ofEpochMilli(occ.deadlineDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                InfoChip(
                                    icon = Icons.Default.Flag,
                                    label = "Deadline",
                                    value = deadlineStr.format(DateTimeFormatter.ofPattern("MMM d")),
                                    isOverdue = occ.deadlineDate < System.currentTimeMillis() && !isCompleted
                                )
                            }
                        }
                    }
                }

                // Notes section
                if (occ.notes.isNotBlank()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Notes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(occ.notes, fontSize = 14.sp)
                        }
                    }
                }

                // Recurrence info
                if (occ.seriesId != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Recurring Task", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("This task is part of a recurring series. Changes to the series will affect future occurrences.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Main action button
                Button(
                    onClick = { viewModel.toggleOccurrence(occ.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (occ.status == OccurrenceStatus.COMPLETED)
                            MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    val isCompleted = occ.status == OccurrenceStatus.COMPLETED
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.Undo else Icons.Default.Check,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isCompleted) "Revert to Pending" else "Mark as Completed", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                
                // Quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { viewModel.snoozeOccurrence(occ.id, 60) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Snooze, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Snooze 1h")
                    }
                    OutlinedButton(onClick = { viewModel.rescheduleOccurrence(occ.id, "tomorrow") }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tomorrow")
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Task?") },
            text = { Text("Are you sure you want to remove this task?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteOccurrence(occurrenceId) {
                            showDeleteConfirm = false
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit dialog
    if (showEditDialog) {
        // Would open edit screen
        showEditDialog = false
    }
}

// ============================================================================
// Helper Composables
// ============================================================================

@Composable
fun RadioOption(label: String, mode: ThemeMode, current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(mode) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = current == mode,
            onClick = { onSelect(mode) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isOverdue: Boolean = false
) {
    val textColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val bgColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHighest
    
    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = "", tint = textColor, modifier = Modifier.size(16.dp))
        Column {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int, titleColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = titleColor)
        Text("$count items", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, colorToken: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("blue") }
    val colors = listOf("blue" to Color(0xFF2196F3), "green" to Color(0xFF4CAF50), "red" to Color(0xFFF44336), "purple" to Color(0xFF9C27B0), "orange" to Color(0xFFFF9800))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Project", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Color Tag:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { (colorKey, colorValue) ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(colorValue, CircleShape)
                                .clickable { selectedColor = colorKey }
                                .padding(2.dp)
                        ) {
                            if (selectedColor == colorKey) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, selectedColor) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MonthSummaryCard(
    month: Int,
    year: Int,
    summary: MonthSummary?,
    onClick: () -> Unit
) {
    val monthName = java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())
    val total = summary?.totalTasks ?: 0
    val completed = summary?.completedTasks ?: 0
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(monthName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "$completed / $total tasks",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

@Composable
fun QuickAddDialog(
    preselectedMonth: Int?,
    preselectedYear: Int?,
    projects: List<Project>,
    onDismiss: () -> Unit,
    onSave: (title: String, notes: String, projectId: String?, priority: Int, dateMillis: Long?, month: Int?, year: Int?, precision: PlanningPrecision) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedProjectId by remember { mutableStateOf(projects.firstOrNull()?.id) }
    var selectedPriority by remember { mutableIntStateOf(1) }
    var dateMode by remember { mutableStateOf(if (preselectedMonth != null) "MONTH" else "TODAY") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                if (projects.isNotEmpty()) {
                    Text("Project:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        projects.take(4).forEach { project ->
                            FilterChip(
                                selected = selectedProjectId == project.id,
                                onClick = { selectedProjectId = project.id },
                                label = { Text(project.name, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                Text("Priority:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1 to "Low", 2 to "Medium", 3 to "High").forEach { (pri, label) ->
                        FilterChip(
                            selected = selectedPriority == pri,
                            onClick = { selectedPriority = pri },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Text("Schedule For:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = dateMode == "TODAY",
                        onClick = { dateMode = "TODAY" },
                        label = { Text("Today") }
                    )
                    FilterChip(
                        selected = dateMode == "TOMORROW",
                        onClick = { dateMode = "TOMORROW" },
                        label = { Text("Tomorrow") }
                    )
                    if (preselectedMonth != null) {
                        FilterChip(
                            selected = dateMode == "MONTH",
                            onClick = { dateMode = "MONTH" },
                            label = { Text("Month $preselectedMonth") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val zone = java.time.ZoneId.systemDefault()
                        val targetDate = when (dateMode) {
                            "TOMORROW" -> LocalDate.now().plusDays(1)
                            else -> LocalDate.now()
                        }
                        val millis = if (dateMode == "MONTH") null else targetDate.atStartOfDay(zone).toInstant().toEpochMilli()
                        val precision = if (dateMode == "MONTH") PlanningPrecision.MONTH else PlanningPrecision.DAY

                        onSave(
                            title,
                            notes,
                            selectedProjectId,
                            selectedPriority,
                            millis,
                            preselectedMonth ?: targetDate.monthValue,
                            preselectedYear ?: targetDate.year,
                            precision
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}