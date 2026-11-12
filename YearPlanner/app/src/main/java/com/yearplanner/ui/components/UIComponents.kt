package com.yearplanner.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateDpAsState
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearplanner.data.model.OccurrenceStatus
import com.yearplanner.data.model.Project
import com.yearplanner.data.model.TaskOccurrence
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ============================================================================
// Task Card Components
// ============================================================================

@Composable
fun TaskCard(
    task: TaskOccurrence,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onComplete: () -> Unit = {},
    onSnooze: (Int) -> Unit = {},
    onReschedule: (String) -> Unit = {},
    showActions: Boolean = true,
    animateEntrance: Boolean = true
) {
    val isDone = task.status == OccurrenceStatus.COMPLETED
    val isOverdue = task.deadlineDate != null && 
        task.deadlineDate!! < System.currentTimeMillis() && !isDone
    
    val bgColor by animateColorAsState(
        targetValue = when {
            isDone -> MaterialTheme.colorScheme.surfaceContainerLowest
            isOverdue -> Color(0xFF3E1A1A)
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = Spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    val elevation by animateDpAsState(
        targetValue = if (animateEntrance) 4.dp else 0.dp,
        animationSpec = Spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Completion checkbox
            if (showActions) {
                CompletionCheckbox(
                    isDone = isDone,
                    onClick = if (!isDone) onComplete else null,
                    isOverdue = isOverdue
                )
            }
            
            // Task content
            Column(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp)
            ) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium,
                    color = if (isDone) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isDone) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Date info row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Planned date
                    if (task.plannedDate != null) {
                        val planned = Instant.ofEpochMilli(task.plannedDate!!)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        DateChip(
                            text = planned.format(DateTimeFormatter.ofPattern("MMM d")),
                            icon = Icons.Default.CalendarToday,
                            isOverdue = false
                        )
                    }
                    
                    // Deadline
                    if (task.deadlineDate != null) {
                        val deadline = Instant.ofEpochMilli(task.deadlineDate!!)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        val isDeadlineOverdue = task.deadlineDate!! < System.currentTimeMillis() && !isDone
                        DateChip(
                            text = deadline.format(DateTimeFormatter.ofPattern("MMM d")),
                            icon = Icons.Default.Event,
                            isOverdue = isDeadlineOverdue
                        )
                    }
                    
                    // Project chip
                    if (task.taskId.isNotEmpty()) {
                        ProjectChipSmall(projectId = task.taskId)
                    }
                }
            }
            
            // Action buttons
            if (showActions && !isDone) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Snooze dropdown
                    SnoozeMenu(onSnooze = onSnooze)
                    
                    // Reschedule
                    IconButton(
                        onClick = { onReschedule("tomorrow") },
                        content = {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Reschedule",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CompletionCheckbox(
    isDone: Boolean,
    onClick: (() -> Unit)?,
    isOverdue: Boolean = false
) {
    val boxColor by animateColorAsState(
        targetValue = when {
            isDone -> MaterialTheme.colorScheme.primary
            isOverdue -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.surfaceContainerHighest
        }
    )
    
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(boxColor, RoundedCornerShape(14.dp))
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() }),
        contentAlignment = Alignment.Center
    ) {
        if (isDone) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        } else if (isOverdue) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Overdue",
                tint = MaterialTheme.colorScheme.onError
            )
        }
    }
}

@Composable
fun DateChip(
    text: String,
    icon: ImageVector,
    isOverdue: Boolean
) {
    val textColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    val bgColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHighest
    
    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = "", tint = textColor, modifier = Modifier.size(12.dp))
        Text(text = text, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ProjectChipSmall(projectId: String) {
    val colors = listOf(
        Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFF9C27B0),
        Color(0xFFFF9800), Color(0xFFF44336), Color(0xFF009688),
        Color(0xFFE91E63), Color(0xFF3F51B5)
    )
    val color = colors[java.lang.Math.abs(projectId.hashCode()) % colors.size]
    
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(6.dp).background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Project", fontSize = 10.sp, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SnoozeMenu(onSnooze: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { expanded = !expanded },
        contentAlignment = Alignment.Center
    ) {
        if (!expanded) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Snooze, contentDescription = "", modifier = Modifier.size(14.dp))
                Text("Snooze", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Default.ExpandMore, contentDescription = "", modifier = Modifier.size(14.dp))
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(15 to "15m", 60 to "1h", 240 to "4h", 720 to "12h", 1440 to "1d").forEach { (mins, label) ->
                    SuggestionChip(
                        onClick = { onSnooze(mins); expanded = false },
                        modifier = Modifier.height(28.dp),
                        label = { Text(text = label, fontSize = 10.sp) }
                    )
                }
            }
        }
    }
}

// ============================================================================
// Project Components
// ============================================================================

@Composable
fun ProjectChip(
    project: Project,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    showTaskCount: Boolean = true,
    taskCount: Int = 0,
    completedCount: Int = 0
) {
    val color = getProjectColor(project.colorToken)
    
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (showTaskCount && taskCount > 0) {
                    Text(
                        text = "$completedCount / $taskCount tasks",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Progress indicator
            if (showTaskCount && taskCount > 0) {
                CircularProgressIndicator(
                    progress = completedCount.toFloat() / taskCount,
                    modifier = Modifier.size(24.dp),
                    color = color,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

private fun getProjectColor(token: String): Color {
    return when (token) {
        "blue" -> Color(0xFF2196F3)
        "green" -> Color(0xFF4CAF50)
        "orange" -> Color(0xFFFF9800)
        "red" -> Color(0xFFF44336)
        "purple" -> Color(0xFF9C27B0)
        "teal" -> Color(0xFF009688)
        "pink" -> Color(0xFFE91E63)
        "indigo" -> Color(0xFF3F51B5)
        else -> Color(0xFF2196F3)
    }
}

// ============================================================================
// Stat Card Components
// ============================================================================

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    trend: String? = null,
    trendUp: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = "", tint = color)
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Trend
            if (trend != null) {
                Box(
                    modifier = Modifier.padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(
                            if (trendUp) 
                                Icons.Default.TrendingUp 
                            else 
                                Icons.Default.TrendingDown,
                            contentDescription = "",
                            tint = if (trendUp) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                        Text(text = trend, fontSize = 12.sp, fontWeight = FontWeight.Medium, 
                            color = if (trendUp) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// ============================================================================
// Empty State Components
// ============================================================================

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(48.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = "", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
            }
            
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            actionText?.let { text ->
                Button(onClick = onAction ?: {}) {
                    Text(text)
                }
            }
        }
    }
}

// ============================================================================
// Loading/Skeleton Components
// ============================================================================

@Composable
fun TaskSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun ProjectSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp))
        )
    }
}

// ============================================================================
// Filter/Chip Components
// ============================================================================

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        leadingIcon = { icon?.let { Icon(it, contentDescription = "") } },
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
fun SortChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    direction: String = "asc"
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        leadingIcon = { 
            if (selected) {
                Icon(
                    if (direction == "asc")
                        Icons.Default.ArrowUpward
                    else
                        Icons.Default.ArrowDownward,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    )
}
