package com.yearplanner.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yearplanner.data.model.Project
import com.yearplanner.domain.usecase.TaskUseCases
import com.yearplanner.ui.theme.Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class QuickAddActivity : ComponentActivity() {

    @Inject
    lateinit var taskUseCases: TaskUseCases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        
        val template = intent.getStringExtra("template") ?: "today"
        val defaultProject = intent.getStringExtra("default_project")
        val defaultPriority = intent.getIntExtra("default_priority", 0)
        
        setContent {
            val projects by taskUseCases.getAllProjects().collectAsStateWithLifecycle(initialValue = emptyList())
            Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    QuickAddDialogContent(
                        template = template,
                        defaultProject = defaultProject,
                        defaultPriority = defaultPriority,
                        projects = projects,
                        onDismiss = { finish() },
                        onSave = { title, notes, projectId, priority, plannedDate, deadlineDate ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val task = taskUseCases.createTask(
                                    title = title,
                                    notes = notes,
                                    projectId = projectId,
                                    priority = priority
                                )
                                
                                val zone = ZoneId.systemDefault()
                                val plannedMillis = plannedDate?.atZone(zone)?.toInstant()?.toEpochMilli()
                                val deadlineMillis = deadlineDate?.atZone(zone)?.toInstant()?.toEpochMilli()
                                
                                taskUseCases.createOccurrence(
                                    taskId = task.id,
                                    originalOccurrenceKey = "manual_${System.currentTimeMillis()}",
                                    planningPrecision = com.yearplanner.data.model.PlanningPrecision.DAY,
                                    plannedDate = plannedMillis,
                                    deadlineDate = deadlineMillis
                                )
                            }
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickAddDialogContent(
    template: String,
    defaultProject: String?,
    defaultPriority: Int,
    projects: List<Project>,
    onDismiss: () -> Unit,
    onSave: (String, String, String?, Int, LocalDateTime?, LocalDateTime?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedProjectId by remember { mutableStateOf(defaultProject) }
    var priority by remember { mutableIntStateOf(defaultPriority) }
    var plannedDate by remember { mutableStateOf<LocalDateTime?>(getDefaultPlannedDate(template)) }
    var deadlineDate by remember { mutableStateOf<LocalDateTime?>(null) }
    
    val priorityLabels = listOf("None", "Low", "Med", "High")
    val priorityColors = listOf(
        Color(0xFF757575),
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFFF44336)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quick Add Task", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(template.uppercase(), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp).width(320.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Template-specific quick actions
                when (template) {
                    "work" -> QuickActionChips(
                        label = "Work Templates",
                        options = listOf("Meeting", "Code Review", "Deploy", "Docs"),
                        onClick = { title = it }
                    )
                    "personal" -> QuickActionChips(
                        label = "Personal Templates",
                        options = listOf("Shopping", "Call", "Bills", "Errands"),
                        onClick = { title = it }
                    )
                    "health" -> QuickActionChips(
                        label = "Health Templates",
                        options = listOf("Workout", "Walk", "Meds", "Water"),
                        onClick = { title = it }
                    )
                    "learning" -> QuickActionChips(
                        label = "Learning Templates",
                        options = listOf("Read", "Course", "Practice", "Project"),
                        onClick = { title = it }
                    )
                }
                
                // Notes
                TextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                
                // Project selector
                if (projects.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Project", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedProjectId == null,
                                onClick = { selectedProjectId = null },
                                label = { Text("None", fontSize = 12.sp) }
                            )
                            projects.take(3).forEach { proj ->
                                FilterChip(
                                    selected = selectedProjectId == proj.id,
                                    onClick = { selectedProjectId = proj.id },
                                    label = { Text(proj.name, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
                
                // Priority
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Priority:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        priorityLabels.forEachIndexed { index, label ->
                            val isSelected = priority == index
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) priorityColors[index] else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { priority = index }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
                
                // Date shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Planned date
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp))
                            .clickable { plannedDate = (plannedDate ?: LocalDateTime.now()).plusDays(1) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = plannedDate?.format(DateTimeFormatter.ofPattern("MMM d")) ?: "Set date",
                                fontSize = 12.sp,
                                color = if (plannedDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Deadline date
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp))
                            .clickable { deadlineDate = (deadlineDate ?: LocalDateTime.now()).plusDays(2) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = "", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = deadlineDate?.format(DateTimeFormatter.ofPattern("MMM d")) ?: "Deadline",
                                fontSize = 12.sp,
                                color = if (deadlineDate != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (title.isNotBlank()) {
                        onSave(title, notes, selectedProjectId, priority, plannedDate, deadlineDate)
                    }
                },
                enabled = title.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun QuickActionChips(
    label: String,
    options: List<String>,
    onClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { option ->
                SuggestionChip(
                    onClick = { onClick(option) },
                    modifier = Modifier.height(28.dp),
                    label = { Text(text = option, fontSize = 11.sp) }
                )
            }
        }
    }
}

private fun getDefaultPlannedDate(template: String): LocalDateTime? {
    val now = LocalDateTime.now()
    return when (template) {
        "today" -> now.withHour(9).withMinute(0)
        "tomorrow" -> now.plusDays(1).withHour(9).withMinute(0)
        "agenda" -> now.plusDays(1).withHour(9).withMinute(0)
        else -> now.withHour(9).withMinute(0)
    }
}
