package com.yearplanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yearplanner.domain.usecase.TaskUseCases
import com.yearplanner.ui.theme.Theme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TodayScreen(
    onNavigate: (String) -> Unit,
    onQuickAdd: () -> Unit,
    taskUseCases: TaskUseCases = viewModel()
) {
    val today = LocalDate.now()
    val todayMillis = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val tomorrowMillis = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    
    val todayTasks by taskUseCases.getOccurrencesByDateRange(todayMillis, tomorrowMillis - 1).collectAsStateWithLifecycle(initialValue = emptyList())
    val overdueTasks by taskUseCases.getOverdueOccurrences(System.currentTimeMillis()).collectAsStateWithLifecycle(initialValue = emptyList())
    
    val completedToday = todayTasks.count { it.status == com.yearplanner.data.model.OccurrenceStatus.COMPLETED }
    val totalToday = todayTasks.size
    val overdueCount = overdueTasks.size

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Today", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            actions = {
                IconButton(onClick = onQuickAdd) {
                    Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add task")
                }
                IconButton(onClick = { onNavigate("year") }) {
                    Icon(androidx.compose.material.icons.Icons.Default.CalendarMonth, contentDescription = "Year view")
                }
            }
        )
        
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date header
                item {
                    Text(
                        text = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Stats row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard("Today", "$completedToday / $totalToday", MaterialTheme.colorScheme.primary)
                        StatCard("Overdue", overdueCount.toString(), MaterialTheme.colorScheme.error)
                    }
                }
                
                // Overdue section
                if (overdueTasks.isNotEmpty()) {
                    item {
                        SectionHeader("Overdue", overdueCount)
                    }
                    items(overdueTasks) { task ->
                        TaskItem(task = task, onClick = { onNavigate("task_detail?occurrenceId=${task.id}") })
                    }
                }
                
                // Today's tasks
                item {
                    SectionHeader("Today", totalToday)
                }
                
                if (todayTasks.isEmpty()) {
                    item {
                        EmptyState("No tasks for today", "Tap + to add a task")
                    }
                } else {
                    items(todayTasks) { task ->
                        TaskItem(task = task, onClick = { onNavigate("task_detail?occurrenceId=${task.id}") })
                    }
                }
            }
        }
    }
}

@Composable
fun YearScreen(
    selectedYear: Int,
    onYearChanged: (Int) -> Unit,
    onNavigate: (String) -> Unit,
    taskUseCases: TaskUseCases = viewModel()
) {
    val yearSummary by taskUseCases.getYearSummary(selectedYear).collectAsStateWithLifecycle(initialValue = null)
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { 
                Row {
                    Text("$selectedYear", fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { onYearChanged(selectedYear - 1) }) {
                        Icon(androidx.compose.material.icons.Icons.Default.ChevronLeft, contentDescription = "Previous year")
                    }
                    IconButton(onClick = { onYearChanged(selectedYear + 1) }) {
                        Icon(androidx.compose.material.icons.Icons.Default.ChevronRight, contentDescription = "Next year")
                    }
                }
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            navigationIcon = { IconButton(onClick = { onNavigate("today") }) { Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back") } }
        )
        
        yearSummary?.let { summary ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(1..12) { month ->
                    val monthSummary = summary.months[month]
                    MonthSummaryCard(
                        month = month,
                        year = selectedYear,
                        summary = monthSummary,
                        onClick = { onNavigate("month?year=$selectedYear&month=$month") }
                    )
                }
            }
        } ?: {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun MonthScreen(
    year: Int,
    month: Int,
    onNavigate: (String) -> Unit,
    taskUseCases: TaskUseCases = viewModel()
) {
    val tasks by taskUseCases.getOccurrencesByMonth(year, month).collectAsStateWithLifecycle(initialValue = emptyList())
    val monthName = java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("$monthName $year", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            navigationIcon = { IconButton(onClick = { onNavigate("year") }) { Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back") } },
            actions = {
                IconButton(onClick = { /* TODO: Add task for this month */ }) {
                    Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add task")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (tasks.isEmpty()) {
                item {
                    EmptyState("No tasks this month", "Tasks will appear here when scheduled")
                }
            } else {
                items(tasks) { task ->
                    TaskItem(task = task, onClick = { onNavigate("task_detail?occurrenceId=${task.id}") })
                }
            }
        }
    }
}

@Composable
fun ProjectsScreen(
    onNavigate: (String) -> Unit,
    taskUseCases: TaskUseCases = viewModel()
) {
    val projects by taskUseCases.getAllProjects().collectAsStateWithLifecycle(initialValue = emptyList())
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Projects", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            navigationIcon = { IconButton(onClick = { onNavigate("today") }) { Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back") } },
            actions = {
                IconButton(onClick = { /* TODO: Create project */ }) {
                    Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add project")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(projects) { project ->
                ProjectCard(project = project, onClick = { /* TODO: Navigate to project tasks */ })
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            navigationIcon = { IconButton(onClick = { onNavigate("today") }) { Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back") } }
        )
        
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                item {
                    SettingsSection("Appearance") {
                        SettingsItem("Theme", "System default", onClick = { /* TODO */ })
                        SettingsItem("Widget Style", "Configure widget appearance", onClick = { /* TODO */ })
                    }
                }
                item {
                    SettingsSection("Data") {
                        SettingsItem("Export Data", "Backup to JSON or CSV", onClick = { /* TODO */ })
                        SettingsItem("Import Data", "Restore from backup", onClick = { /* TODO */ })
                    }
                }
                item {
                    SettingsSection("Reminders") {
                        SettingsItem("Notification Permission", "Manage reminder notifications", onClick = { /* TODO */ })
                        SettingsItem("Exact Alarms", "Allow precise reminder timing", onClick = { /* TODO */ })
                    }
                }
                item {
                    SettingsSection("About") {
                        SettingsItem("Version", "1.0.0", onClick = { })
                        SettingsItem("Privacy Policy", "View privacy policy", onClick = { /* TODO */ })
                    }
                }
            }
        }
    }
}

@Composable
fun TaskDetailScreen(
    occurrenceId: String,
    onNavigate: (String) -> Unit,
    taskUseCases: TaskUseCases = viewModel()
) {
    val occurrence by taskUseCases.getOccurrence(occurrenceId).collectAsStateWithLifecycle(initialValue = null)
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Task Detail", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            navigationIcon = { IconButton(onClick = { onNavigate("today") }) { Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back") } }
        )
        
        occurrence?.let { occ ->
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(occ.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    
                    // Status toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Status: ${occ.status.name}")
                        Button(
                            onClick = { 
                                if (occ.status == com.yearplanner.data.model.OccurrenceStatus.COMPLETED) {
                                    // TODO: Undo
                                } else {
                                    // TODO: Complete
                                }
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (occ.status == com.yearplanner.data.model.OccurrenceStatus.COMPLETED) 
                                    MaterialTheme.colorScheme.errorContainer 
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(if (occ.status == com.yearplanner.data.model.OccurrenceStatus.COMPLETED) "Undo" else "Complete")
                        }
                    }
                    
                    // Details
                    Text("Details:", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Text(occ.notes.isNotBlank().then(occ.notes) ?: "No notes")
                }
            }
        } ?: {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun QuickAddScreen(
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Quick Add Task", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        
        androidx.compose.material3.TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onDismiss) { Text("Cancel") }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { 
                // TODO: Save task
                onDismiss()
            }) { Text("Save") }
        }
    }
}

// Helper Composables
@Composable
fun StatCard(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 14.sp, color = color)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("$count tasks", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TaskItem(
    task: com.yearplanner.data.model.TaskOccurrence,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxWidth(),
        onClick = onClick
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox
            androidx.compose.material3.Checkbox(
                checked = task.status == com.yearplanner.data.model.OccurrenceStatus.COMPLETED,
                onCheckedChange = { /* TODO */ }
            )
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = if (task.status == com.yearplanner.data.model.OccurrenceStatus.COMPLETED) FontWeight.Normal else FontWeight.Medium,
                    color = if (task.status == com.yearplanner.data.model.OccurrenceStatus.COMPLETED) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.status == com.yearplanner.data.model.OccurrenceStatus.COMPLETED) 
                        androidx.compose.ui.text.style.TextDecoration.LineThrough 
                    else null
                )
                
                if (task.deadlineDate != null) {
                    val deadline = java.time.Instant.ofEpochMilli(task.deadlineDate!!).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    Text(
                        "Due: ${deadline.format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontal) {
            Icon(androidx.compose.material.icons.Icons.Default.Inbox, contentDescription = "", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun MonthSummaryCard(
    month: Int,
    year: Int,
    summary: com.yearplanner.ui.screens.MonthSummary?,
    onClick: () -> Unit
) {
    val monthName = java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
    val total = summary?.totalTasks ?: 0
    val completed = summary?.completedTasks ?: 0
    val progress = if (total > 0) completed.toFloat() / total else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(monthName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("$completed / $total", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            
            androidx.compose.material3.LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProjectCard(
    project: com.yearplanner.data.model.Project,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(12.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.shape.CircleShape
                // TODO: Apply project color
            }
            Text(project.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun SettingsItem(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, fontSize = 16.sp)
                Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(androidx.compose.material.icons.Icons.Default.ChevronRight, contentDescription = "")
        }
    }
}