package com.yearplanner.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.GlanceAppWidget
import androidx.glance.GlanceId
import androidx.glance.action.Action
import androidx.glance.action.RunCallbackAction
import androidx.glance.action.StartActivityAction
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.preferences
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.unit.dp
import androidx.glance.unit.sp
import com.yearplanner.data.model.WidgetConfig
import com.yearplanner.data.model.WidgetKind
import com.yearplanner.ui.QuickAddActivity

class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, glanceId: GlanceId) {
        val config = getWidgetConfig(context, glanceId)
        val today = java.time.LocalDate.now()
        val tasks = getTodayTasks(context)
        
        val completedCount = tasks.count { it.status == com.yearplanner.data.model.OccurrenceStatus.COMPLETED }
        val totalCount = tasks.size
        val overdueCount = getOverdueCount(context)
        
        provideContent {
            Column(modifier = androidx.glance.layout.Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = androidx.glance.layout.Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.glance.layout.Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Today",
                            fontSize = 20.sp,
                            fontWeight = androidx.glance.text.FontWeight.Bold,
                            color = androidx.glance.Color.White
                        )
                        Text(
                            text = today.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                            fontSize = 14.sp,
                            color = androidx.glance.Color.White.copy(alpha = 0.8f)
                        )
                    }
                    
                    // Stats
                    Row(horizontalArrangement = androidx.glance.layout.Arrangement.spacedBy(8.dp)) {
                        StatBadge("$completedCount/$totalCount", androidx.glance.Color.Green)
                        if (overdueCount > 0) {
                            StatBadge("$overdueCount overdue", androidx.glance.Color.Red)
                        }
                    }
                }
                
                androidx.glance.layout.Spacer(modifier = androidx.glance.layout.Modifier.height(12.dp))
                
                // Task list
                if (tasks.isEmpty()) {
                    Text(
                        text = "No tasks for today\nTap + to add one",
                        fontSize = 14.sp,
                        color = androidx.glance.Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = androidx.glance.layout.Modifier.fillMaxWidth().padding(16.dp)
                    )
                } else {
                    Column(modifier = androidx.glance.layout.Modifier.fillMaxWidth()) {
                        tasks.take(5).forEach { task ->
                            TaskRow(
                                task = task,
                                onComplete = ActionRunCallback(
                                    "complete_${task.id}",
                                    { context -> completeTask(context, task.id) }
                                )
                            )
                        }
                    }
                }
                
                // Footer actions
                androidx.glance.layout.Spacer(modifier = androidx.glance.layout.Modifier.height(12.dp))
                Row(
                    modifier = androidx.glance.layout.Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.glance.layout.Arrangement.SpaceEvenly
                ) {
                    ActionButton(
                        text = "+ Add",
                        onClick = StartActivityAction(
                            Intent(context, QuickAddActivity::class.java)
                        )
                    )
                    ActionButton(
                        text = "View All",
                        onClick = StartActivityAction(
                            Intent(context, com.yearplanner.ui.MainActivity::class.java)
                                .apply { putExtra("navigate", "today") }
                        )
                    )
                }
            }
        }
    }
    
    private fun getTodayTasks(context: Context): List<TaskOccurrence> {
        // TODO: Query from database
        return emptyList()
    }
    
    private fun getOverdueCount(context: Context): Int {
        // TODO: Query from database
        return 0
    }
    
    private fun completeTask(context: Context, occurrenceId: String) {
        // TODO: Call completion use case
    }
    
    private fun getWidgetConfig(context: Context, glanceId: GlanceId): WidgetConfig {
        // TODO: Get from DataStore
        return WidgetConfig(appWidgetId = glanceId.id, widgetKind = WidgetKind.TODAY)
    }
}

class YearOverviewWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, glanceId: GlanceId) {
        val config = getWidgetConfig(context, glanceId)
        val year = config.selectedYear
        val monthData = getMonthSummaries(context, year)
        
        provideContent {
            Column(modifier = androidx.glance.layout.Modifier.fillMaxSize().padding(12.dp)) {
                // Year header with navigation
                Row(
                    modifier = androidx.glance.layout.Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.glance.layout.Arrangement.SpaceBetween
                ) {
                    ActionButton(
                        text = "<",
                        onClick = ActionRunCallback("prev_year", { ctx -> updateYear(ctx, glanceId, year - 1) })
                    )
                    Text(
                        text = year.toString(),
                        fontSize = 20.sp,
                        fontWeight = androidx.glance.text.FontWeight.Bold,
                        color = androidx.glance.Color.White
                    )
                    ActionButton(
                        text = ">",
                        onClick = ActionRunCallback("next_year", { ctx -> updateYear(ctx, glanceId, year + 1) })
                    )
                }
                
                androidx.glance.layout.Spacer(modifier = androidx.glance.layout.Modifier.height(8.dp))
                
                // 3x4 grid of months
                androidx.glance.layout.Box(modifier = androidx.glance.layout.Modifier.fillMaxSize()) {
                    androidx.glance.layout.Column(
                        modifier = androidx.glance.layout.Modifier.fillMaxSize(),
                        verticalArrangement = androidx.glance.layout.Arrangement.spacedBy(4.dp)
                    ) {
                        (0..3).forEach { row ->
                            Row(
                                modifier = androidx.glance.layout.Modifier.fillMaxWidth(),
                                horizontalArrangement = androidx.glance.layout.Arrangement.spacedBy(4.dp)
                            ) {
                                (1..3).forEach { col ->
                                    val month = row * 3 + col
                                    if (month <= 12) {
                                        val data = monthData[month] ?: MonthSummary(month, year, 0, 0)
                                        MonthCell(
                                            month = month,
                                            data = data,
                                            onClick = ActionRunCallback(
                                                "month_$month",
                                                { ctx -> openMonth(ctx, year, month) }
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun getMonthSummaries(context: Context, year: Int): Map<Int, MonthSummary> {
        // TODO: Query from database
        return emptyMap()
    }
    
    private fun updateYear(context: Context, glanceId: GlanceId, newYear: Int) {
        // TODO: Update widget config
    }
    
    private fun openMonth(context: Context, year: Int, month: Int) {
        // TODO: Open month in app
    }
    
    private fun getWidgetConfig(context: Context, glanceId: GlanceId): WidgetConfig {
        return WidgetConfig(appWidgetId = glanceId.id, widgetKind = WidgetKind.YEAR_OVERVIEW)
    }
}

class YearAgendaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, glanceId: GlanceId) {
        val config = getWidgetConfig(context, glanceId)
        val year = config.selectedYear
        val tasks = getYearTasks(context, year)
        
        provideContent {
            Column(modifier = androidx.glance.layout.Modifier.fillMaxSize().padding(12.dp)) {
                // Header
                Row(
                    modifier = androidx.glance.layout.Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.glance.layout.Arrangement.SpaceBetween
                ) {
                    ActionButton(
                        text = "<",
                        onClick = ActionRunCallback("prev_year", { ctx -> updateYear(ctx, glanceId, year - 1) })
                    )
                    Text(
                        text = "Agenda $year",
                        fontSize = 20.sp,
                        fontWeight = androidx.glance.text.FontWeight.Bold,
                        color = androidx.glance.Color.White
                    )
                    ActionButton(
                        text = ">",
                        onClick = ActionRunCallback("next_year", { ctx -> updateYear(ctx, glanceId, year + 1) })
                    )
                }
                
                androidx.glance.layout.Spacer(modifier = androidx.glance.layout.Modifier.height(8.dp))
                
                // Task list
                if (tasks.isEmpty()) {
                    Text(
                        text = "No tasks this year",
                        fontSize = 14.sp,
                        color = androidx.glance.Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = androidx.glance.layout.Modifier.fillMaxWidth().padding(16.dp)
                    )
                } else {
                    Column(
                        modifier = androidx.glance.layout.Modifier.fillMaxSize(),
                        verticalArrangement = androidx.glance.layout.Arrangement.spacedBy(4.dp)
                    ) {
                        tasks.take(config.visibleBatchSize).forEach { task ->
                            TaskRow(
                                task = task,
                                onComplete = ActionRunCallback(
                                    "complete_${task.id}",
                                    { context -> completeTask(context, task.id) }
                                )
                            )
                        }
                    }
                }
                
                // Load more
                if (tasks.size > config.visibleBatchSize) {
                    ActionButton(
                        text = "Load More",
                        onClick = ActionRunCallback("load_more", { ctx -> loadMore(ctx, glanceId) })
                    )
                }
            }
        }
    }
    
    private fun getYearTasks(context: Context, year: Int): List<TaskOccurrence> {
        // TODO: Query from database
        return emptyList()
    }
    
    private fun updateYear(context: Context, glanceId: GlanceId, newYear: Int) {
        // TODO: Update widget config
    }
    
    private fun loadMore(context: Context, glanceId: GlanceId) {
        // TODO: Load more tasks
    }
    
    private fun completeTask(context: Context, occurrenceId: String) {
        // TODO: Call completion use case
    }
    
    private fun getWidgetConfig(context: Context, glanceId: GlanceId): WidgetConfig {
        return WidgetConfig(appWidgetId = glanceId.id, widgetKind = WidgetKind.YEAR_AGENDA)
    }
}

// Helper Composables
@Composable
fun StatBadge(text: String, color: androidx.glance.Color) {
    Box(
        modifier = androidx.glance.layout.Modifier.padding(8.dp, 4.dp),
        contentAlignment = androidx.glance.layout.Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = androidx.glance.text.FontWeight.Bold,
            color = androidx.glance.Color.White
        )
    }
}

@Composable
fun ActionButton(text: String, onClick: androidx.glance.action.Action) {
    Box(
        modifier = androidx.glance.layout.Modifier
            .fillMaxWidth()
            .padding(12.dp, 8.dp)
            .background(color = androidx.glance.Color.White.copy(alpha = 0.2f))
            .clickable(onClick),
        contentAlignment = androidx.glance.layout.Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = androidx.glance.text.FontWeight.Medium,
            color = androidx.glance.Color.White
        )
    }
}

@Composable
fun MonthCell(
    month: Int,
    data: MonthSummary,
    onClick: androidx.glance.action.Action
) {
    val monthName = java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault())
    val progress = if (data.totalTasks > 0) data.completedTasks.toFloat() / data.totalTasks else 0f
    
    Box(
        modifier = androidx.glance.layout.Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(color = androidx.glance.Color.White.copy(alpha = 0.1f))
            .clickable(onClick),
        contentAlignment = androidx.glance.layout.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.glance.layout.Alignment.CenterHorizontal,
            verticalArrangement = androidx.glance.layout.Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = monthName,
                fontSize = 14.sp,
                fontWeight = androidx.glance.text.FontWeight.Bold,
                color = androidx.glance.Color.White
            )
            Text(
                text = "${data.completedTasks}/${data.totalTasks}",
                fontSize = 12.sp,
                color = androidx.glance.Color.White.copy(alpha = 0.8f)
            )
            androidx.glance.layout.Box(
                modifier = androidx.glance.layout.Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(color = androidx.glance.Color.White.copy(alpha = 0.2f))
            ) {
                androidx.glance.layout.Box(
                    modifier = androidx.glance.layout.Modifier
                        .width(androidx.glance.unit.Fraction(progress))
                        .height(4.dp)
                        .background(color = androidx.glance.Color.Green)
                )
            }
        }
    }
}

@Composable
fun TaskRow(
    task: TaskOccurrence,
    onComplete: androidx.glance.action.Action
) {
    Row(
        modifier = androidx.glance.layout.Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(color = androidx.glance.Color.White.copy(alpha = 0.1f)),
        horizontalArrangement = androidx.glance.layout.Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = androidx.glance.layout.Modifier
                .size(24.dp)
                .background(color = if (task.status == com.yearplanner.data.model.OccurrenceStatus.COMPLETED) 
                    androidx.glance.Color.Green 
                else androidx.glance.Color.White.copy(alpha = 0.3f))
                .clickable(onComplete),
            contentAlignment = androidx.glance.layout.Alignment.Center
        ) {
            if (task.status == com.yearplanner.data.model.OccurrenceStatus.COMPLETED) {
                Text(text = "✓", fontSize = 14.sp, fontWeight = androidx.glance.text.FontWeight.Bold, color = androidx.glance.Color.White)
            }
        }
        
        Column(modifier = androidx.glance.layout.Modifier.weight(1f)) {
            Text(
                text = task.title,
                fontSize = 14.sp,
                fontWeight = if (task.status == com.yearplanner.data.model.OccurrenceStatus.COMPLETED) 
                    androidx.glance.text.FontWeight.Normal 
                else androidx.glance.text.FontWeight.Medium,
                color = if (task.status == com.yearplanner.data.model.OccurrenceStatus.COMPLETED) 
                    androidx.glance.Color.White.copy(alpha = 0.6f) 
                else androidx.glance.Color.White,
                maxLines = 1,
                overflow = androidx.glance.text.TextOverflow.Ellipsis
            )
            
            if (task.deadlineDate != null) {
                val deadline = java.time.Instant.ofEpochMilli(task.deadlineDate!!).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                Text(
                    text = "Due: ${deadline.format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))}",
                    fontSize = 10.sp,
                    color = androidx.glance.Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

data class MonthSummary(
    val month: Int,
    val year: Int,
    val totalTasks: Int,
    val completedTasks: Int
)

data class TaskOccurrence(
    val id: String,
    val title: String,
    val status: com.yearplanner.data.model.OccurrenceStatus = com.yearplanner.data.model.OccurrenceStatus.PENDING,
    val deadlineDate: Long? = null
)