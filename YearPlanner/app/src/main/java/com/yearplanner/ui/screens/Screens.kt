@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yearplanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearplanner.data.model.*
import com.yearplanner.data.repository.MonthSummary
import com.yearplanner.ui.MainViewModel
import com.yearplanner.ui.theme.ThemeMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TodayScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit
) {
    val today = LocalDate.now()
    val todayMillis = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val tomorrowMillis = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    val todayTasks by viewModel.taskUseCases.getOccurrencesByDateRange(todayMillis, tomorrowMillis - 1).collectAsState(initial = emptyList())
    val overdueTasks by viewModel.taskUseCases.getOverdueOccurrences(System.currentTimeMillis()).collectAsState(initial = emptyList())

    val completedToday = todayTasks.count { it.status == OccurrenceStatus.COMPLETED }
    val totalToday = todayTasks.size
    val overdueCount = overdueTasks.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today", fontWeight = FontWeight.Bold) },
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
                Icon(Icons.Default.Add, contentDescription = "Quick Add")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date header
            item {
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard("Completed Today", "$completedToday / $totalToday", MaterialTheme.colorScheme.primary)
                    }
                    if (overdueCount > 0) {
                        Box(modifier = Modifier.weight(1f)) {
                            StatCard("Overdue", overdueCount.toString(), MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Overdue section
            if (overdueTasks.isNotEmpty()) {
                item {
                    SectionHeader("Overdue", overdueCount, MaterialTheme.colorScheme.error)
                }
                items(overdueTasks) { task ->
                    TaskItem(
                        task = task,
                        onClick = { onNavigate("task_detail?occurrenceId=${task.id}") },
                        onToggle = { viewModel.toggleOccurrence(task.id) }
                    )
                }
            }

            // Today's tasks
            item {
                SectionHeader("Scheduled for Today", totalToday)
            }

            if (todayTasks.isEmpty()) {
                item {
                    EmptyState(
                        title = "No tasks for today",
                        subtitle = "Tap + to add a task or load sample plan from Settings",
                        onAction = { viewModel.showQuickAdd() },
                        actionText = "+ Add Task"
                    )
                }
            } else {
                items(todayTasks) { task ->
                    TaskItem(
                        task = task,
                        onClick = { onNavigate("task_detail?occurrenceId=${task.id}") },
                        onToggle = { viewModel.toggleOccurrence(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun YearScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit
) {
    var selectedYear by remember { mutableStateOf(viewModel.selectedYear) }
    val yearSummary by viewModel.taskUseCases.getYearSummary(selectedYear).collectAsState(initial = null)

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
                item {
                    val totalYearTasks = summary.months.values.sumOf { it.totalTasks }
                    val completedYearTasks = summary.months.values.sumOf { it.completedTasks }
                    val percent = if (totalYearTasks > 0) (completedYearTasks * 100 / totalYearTasks) else 0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Annual Progress", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$completedYearTasks of $totalYearTasks tasks completed ($percent%)", fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = if (totalYearTasks > 0) completedYearTasks.toFloat() / totalYearTasks else 0f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

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

@Composable
fun MonthScreen(
    year: Int,
    month: Int,
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val tasks by viewModel.taskUseCases.getOccurrencesByMonth(year, month).collectAsState(initial = emptyList())
    val monthName = java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$monthName $year", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(
                onClick = { viewModel.showQuickAdd(month = month, year = year) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
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
                        title = "No tasks in $monthName $year",
                        subtitle = "Plan ahead by scheduling milestones or goals for this month",
                        onAction = { viewModel.showQuickAdd(month = month, year = year) },
                        actionText = "+ Add Task to $monthName"
                    )
                }
            } else {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onClick = { onNavigate("task_detail?occurrenceId=${task.id}") },
                        onToggle = { viewModel.toggleOccurrence(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectsScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit
) {
    val projects by viewModel.taskUseCases.getAllProjects().collectAsState(initial = emptyList())
    var showNewProjectDialog by remember { mutableStateOf(false) }

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
            FloatingActionButton(
                onClick = { showNewProjectDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add project")
            }
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
                        title = "No projects yet",
                        subtitle = "Create your first project (e.g. Work, Health, Personal, Learning)",
                        onAction = { showNewProjectDialog = true },
                        actionText = "+ Create Project"
                    )
                }
            } else {
                items(projects) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { }
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

@Composable
fun SettingsScreen(
    viewModel: MainViewModel
) {
    val currentThemeMode by viewModel.themeMode
    var showClearConfirm by remember { mutableStateOf(false) }
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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentThemeMode == ThemeMode.SYSTEM,
                                    onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("System Default")
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setThemeMode(ThemeMode.LIGHT) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentThemeMode == ThemeMode.LIGHT,
                                    onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Light Mode")
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setThemeMode(ThemeMode.DARK) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentThemeMode == ThemeMode.DARK,
                                    onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Dark Mode (OLED)")
                            }
                        }
                    }
                }
            }

            // Demo Data Section
            item {
                SettingsSection("Sample & Demo Data") {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                        }
                    }
                }
            }

            // Reset Data Section
            item {
                SettingsSection("Data Management") {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                            Text("Year Planner", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Version 1.0.0", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "A high-performance yearly planner and goal tracking app built with Jetpack Compose, Room Database, and Glance Home Widgets.",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

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

@Composable
fun TaskDetailScreen(
    occurrenceId: String,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val occurrence by viewModel.taskUseCases.getOccurrence(occurrenceId).collectAsState(initial = null)
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = occ.title.ifBlank { "Untitled Task" },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isCompleted = occ.status == OccurrenceStatus.COMPLETED
                            AssistChip(
                                onClick = { },
                                label = { Text(if (isCompleted) "Completed" else "Pending") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AssistChip(
                                onClick = { },
                                label = { Text("Precision: ${occ.planningPrecision.name}") }
                            )
                        }

                        if (occ.plannedDate != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val dateStr = java.time.Instant.ofEpochMilli(occ.plannedDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            Text("Scheduled: ${dateStr.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))}", fontSize = 14.sp)
                        }
                    }
                }

                // Action button to complete / undo
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
                    Text(if (isCompleted) "Revert to Pending" else "Mark as Completed")
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
}

// Dialog for adding tasks
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
    var selectedPriority by remember { mutableIntStateOf(1) } // 0=None, 1=Low, 2=Med, 3=High
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

                // Project Selector
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

                // Priority Selector
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

                // Schedule Selector
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
                Text("Save Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog for creating a new project
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

// Helper Composables
@Composable
fun StatCard(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 13.sp, color = color, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
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
fun TaskItem(
    task: TaskOccurrence,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    val isCompleted = task.status == OccurrenceStatus.COMPLETED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggle() }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title.ifBlank { "Untitled Task" },
                    fontSize = 16.sp,
                    fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium,
                    color = if (isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                )

                if (task.deadlineDate != null) {
                    val deadline = java.time.Instant.ofEpochMilli(task.deadlineDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    Text(
                        "Due: ${deadline.format(DateTimeFormatter.ofPattern("MMM d"))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    onAction: (() -> Unit)? = null,
    actionText: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (onAction != null && actionText != null) {
                Spacer(modifier = Modifier.height(16.dp))
                FilledTonalButton(onClick = onAction) {
                    Text(actionText)
                }
            }
        }
    }
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    val projectColor = when (project.colorToken.lowercase()) {
        "green" -> Color(0xFF4CAF50)
        "red" -> Color(0xFFF44336)
        "purple" -> Color(0xFF9C27B0)
        "orange" -> Color(0xFFFF9800)
        else -> Color(0xFF2196F3)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(projectColor, CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(project.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
