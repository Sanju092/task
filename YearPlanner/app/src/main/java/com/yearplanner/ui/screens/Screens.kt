@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yearplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearplanner.data.model.OccurrenceStatus
import com.yearplanner.data.model.Project
import com.yearplanner.data.model.TaskOccurrence
import com.yearplanner.data.repository.MonthSummary
import com.yearplanner.domain.usecase.TaskUseCases
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TodayScreen(
    onNavigate: (String) -> Unit,
    onQuickAdd: () -> Unit,
    taskUseCases: TaskUseCases
) {
    val today = LocalDate.now()
    val todayMillis = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val tomorrowMillis = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    val todayTasks by taskUseCases.getOccurrencesByDateRange(todayMillis, tomorrowMillis - 1).collectAsState(initial = emptyList())
    val overdueTasks by taskUseCases.getOverdueOccurrences(System.currentTimeMillis()).collectAsState(initial = emptyList())

    val completedToday = todayTasks.count { it.status == OccurrenceStatus.COMPLETED }
    val totalToday = todayTasks.size
    val overdueCount = overdueTasks.size

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Today", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            actions = {
                IconButton(onClick = onQuickAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Add task")
                }
                IconButton(onClick = { onNavigate("year") }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Year view")
                }
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
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
    taskUseCases: TaskUseCases
) {
    val yearSummary by taskUseCases.getYearSummary(selectedYear).collectAsState(initial = null)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$selectedYear", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { onYearChanged(selectedYear - 1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous year")
                    }
                    IconButton(onClick = { onYearChanged(selectedYear + 1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next year")
                    }
                }
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            navigationIcon = {
                IconButton(onClick = { onNavigate("today") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        val summary = yearSummary
        if (summary != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
    taskUseCases: TaskUseCases
) {
    val tasks by taskUseCases.getOccurrencesByMonth(year, month).collectAsState(initial = emptyList())
    val monthName = java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("$monthName $year", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            navigationIcon = {
                IconButton(onClick = { onNavigate("year") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Add task")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
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
    taskUseCases: TaskUseCases
) {
    val projects by taskUseCases.getAllProjects().collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Projects", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            navigationIcon = {
                IconButton(onClick = { onNavigate("today") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* TODO: Create project */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Add project")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(projects) { project ->
                ProjectCard(project = project, onClick = { /* TODO */ })
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
            navigationIcon = {
                IconButton(onClick = { onNavigate("today") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                item {
                    SettingsSection("Appearance") {
                        SettingsItem("Theme", "System default", onClick = { })
                        SettingsItem("Widget Style", "Configure widget appearance", onClick = { })
                    }
                }
                item {
                    SettingsSection("Data") {
                        SettingsItem("Export Data", "Backup to JSON or CSV", onClick = { })
                        SettingsItem("Import Data", "Restore from backup", onClick = { })
                    }
                }
                item {
                    SettingsSection("Reminders") {
                        SettingsItem("Notification Permission", "Manage reminder notifications", onClick = { })
                        SettingsItem("Exact Alarms", "Allow precise reminder timing", onClick = { })
                    }
                }
                item {
                    SettingsSection("About") {
                        SettingsItem("Version", "1.0.0", onClick = { })
                        SettingsItem("Privacy Policy", "View privacy policy", onClick = { })
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
    taskUseCases: TaskUseCases
) {
    val occurrence by taskUseCases.getOccurrence(occurrenceId).collectAsState(initial = null)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Task Detail", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            navigationIcon = {
                IconButton(onClick = { onNavigate("today") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        val occ = occurrence
        if (occ != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(occ.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status: ${occ.status.name}")
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (occ.status == OccurrenceStatus.COMPLETED)
                                    MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(if (occ.status == OccurrenceStatus.COMPLETED) "Undo" else "Complete")
                        }
                    }

                    Text("Details:", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Text(if (occ.title.isNotBlank()) occ.title else "No notes")
                }
            }
        } else {
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

        TextField(
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
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onDismiss() }) { Text("Save") }
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
    task: TaskOccurrence,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = task.status == OccurrenceStatus.COMPLETED,
                onCheckedChange = { }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = if (task.status == OccurrenceStatus.COMPLETED) FontWeight.Normal else FontWeight.Medium,
                    color = if (task.status == OccurrenceStatus.COMPLETED)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.status == OccurrenceStatus.COMPLETED)
                        TextDecoration.LineThrough
                    else null
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
            Icon(Icons.Default.Inbox, contentDescription = "", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
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
    val monthName = java.time.Month.of(month).getDisplayName(TextStyle.SHORT, Locale.getDefault())
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
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(12.dp),
                contentAlignment = Alignment.Center
            )
            Text(project.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
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

@Composable
fun SettingsItem(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontSize = 16.sp)
                Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "")
        }
    }
}
