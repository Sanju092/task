package com.yearplanner.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.yearplanner.data.db.YearPlannerDatabase
import com.yearplanner.data.model.OccurrenceStatus
import com.yearplanner.data.model.TaskOccurrence
import com.yearplanner.ui.MainActivity
import com.yearplanner.ui.QuickAddActivity
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 1. TodayWidget: Today's Tasks & Schedule (Calendar / WhatsApp Task List Style)
 * Displays active schedule for today, completion counts, and a direct Quick Add button.
 */
class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, glanceId: GlanceId) {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val formattedDate = today.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
        
        val startOfDay = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        
        var todayTasks = emptyList<TaskOccurrence>()
        try {
            val db = YearPlannerDatabase.getInstance(context)
            todayTasks = db.taskOccurrenceDao().getByDateRange(startOfDay, endOfDay).first()
        } catch (_: Exception) {}

        val totalCount = todayTasks.size
        val completedCount = todayTasks.count { it.status == OccurrenceStatus.COMPLETED }
        val displayTasks = todayTasks.take(3)

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E))
                    .cornerRadius(16.dp)
                    .padding(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "Today",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color.White)
                            )
                        )
                        Text(
                            text = formattedDate,
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = ColorProvider(Color(0xFFB0B0B0))
                            )
                        )
                    }

                    // Progress Tag
                    Box(
                        modifier = GlanceModifier
                            .background(if (completedCount == totalCount && totalCount > 0) Color(0xFF2E7D32) else Color(0xFF1976D2))
                            .cornerRadius(12.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$completedCount/$totalCount Done",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorProvider(Color.White)
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    // + Add Button
                    Box(
                        modifier = GlanceModifier
                            .background(Color(0xFF2196F3))
                            .cornerRadius(12.dp)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .clickable(actionStartActivity<QuickAddActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Add",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color.White)
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Tasks List
                if (displayTasks.isEmpty()) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                            .clickable(actionStartActivity<MainActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks for today. Tap + to add!",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = ColorProvider(Color(0xFF9E9E9E))
                            )
                        )
                    }
                } else {
                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                            .clickable(actionStartActivity<MainActivity>())
                    ) {
                        displayTasks.forEach { task ->
                            val isDone = task.status == OccurrenceStatus.COMPLETED
                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isDone) "✓ " else "○ ",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorProvider(if (isDone) Color(0xFF4CAF50) else Color(0xFFB0B0B0))
                                    )
                                )
                                Text(
                                    text = task.title.ifEmpty { "Untitled Task" },
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = ColorProvider(if (isDone) Color(0xFF757575) else Color.White)
                                    ),
                                    maxLines = 1,
                                    modifier = GlanceModifier.defaultWeight()
                                )
                            }
                        }
                    }
                }

                // Bottom Footer Shortcut
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Open Year Planner →",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorProvider(Color(0xFF64B5F6))
                        )
                    )
                }
            }
        }
    }
}

/**
 * 2. YearOverviewWidget: 365-Day Year Contribution Heatmap (GitHub Style)
 * Displays a 12-month green matrix grid showing annual progress & task density.
 */
class YearOverviewWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, glanceId: GlanceId) {
        val currentYear = LocalDate.now().year
        val zone = ZoneId.systemDefault()

        var yearTasks = emptyList<TaskOccurrence>()
        try {
            val db = YearPlannerDatabase.getInstance(context)
            yearTasks = db.taskOccurrenceDao().getByYear(currentYear).first()
        } catch (_: Exception) {}

        val totalTasks = yearTasks.size
        val completedTasks = yearTasks.count { it.status == OccurrenceStatus.COMPLETED }
        val percent = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 0

        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val monthStats = (1..12).map { month ->
            val count = yearTasks.count { 
                it.plannedMonth == month || (it.plannedDate != null && Instant.ofEpochMilli(it.plannedDate).atZone(zone).monthValue == month)
            }
            val done = yearTasks.count {
                it.status == OccurrenceStatus.COMPLETED &&
                (it.plannedMonth == month || (it.plannedDate != null && Instant.ofEpochMilli(it.plannedDate).atZone(zone).monthValue == month))
            }
            Pair(count, done)
        }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF0D1117)) // GitHub Dark Background
                    .cornerRadius(16.dp)
                    .padding(12.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$currentYear Contributions",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color.White)
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = "$percent% • $completedTasks/$totalTasks done",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFF39D353)) // GitHub Green
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // 12-Month Matrix (Row 1: Jan - Jun)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0..5) {
                        val (total, done) = monthStats[i]
                        val tileColor = when {
                            total == 0 -> Color(0xFF21262D)
                            done == total -> Color(0xFF39D353) // Bright GitHub green
                            done > 0 -> Color(0xFF26A641)      // Medium green
                            else -> Color(0xFF1F6FEB)          // Planned blue
                        }
                        Column(
                            modifier = GlanceModifier.defaultWeight(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = monthNames[i],
                                style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xFF8B949E)))
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Box(
                                modifier = GlanceModifier
                                    .size(16.dp)
                                    .cornerRadius(3.dp)
                                    .background(tileColor)
                            ) {}
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = "$done/$total",
                                style = TextStyle(fontSize = 8.sp, color = ColorProvider(Color(0xFFC9D1D9)))
                            )
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // 12-Month Matrix (Row 2: Jul - Dec)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 6..11) {
                        val (total, done) = monthStats[i]
                        val tileColor = when {
                            total == 0 -> Color(0xFF21262D)
                            done == total -> Color(0xFF39D353)
                            done > 0 -> Color(0xFF26A641)
                            else -> Color(0xFF1F6FEB)
                        }
                        Column(
                            modifier = GlanceModifier.defaultWeight(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = monthNames[i],
                                style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xFF8B949E)))
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Box(
                                modifier = GlanceModifier
                                    .size(16.dp)
                                    .cornerRadius(3.dp)
                                    .background(tileColor)
                            ) {}
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = "$done/$total",
                                style = TextStyle(fontSize = 8.sp, color = ColorProvider(Color(0xFFC9D1D9)))
                            )
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Footer legend
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap to view full year",
                        style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xFF8B949E))),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = "■ Done  ■ Active  ■ Empty",
                        style = TextStyle(fontSize = 9.sp, color = ColorProvider(Color(0xFF39D353)))
                    )
                }
            }
        }
    }
}

/**
 * 3. AnalyticsWidget: Task Analytics & Progress (Digital Wellbeing & Finance Style)
 * Displays task performance across Day, Month, Year, and project categories.
 */
class AnalyticsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, glanceId: GlanceId) {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val currentYear = today.year
        val currentMonth = today.monthValue

        var yearTasks = emptyList<TaskOccurrence>()
        var todayTasks = emptyList<TaskOccurrence>()
        try {
            val db = YearPlannerDatabase.getInstance(context)
            val startOfDay = today.atStartOfDay(zone).toInstant().toEpochMilli()
            val endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            todayTasks = db.taskOccurrenceDao().getByDateRange(startOfDay, endOfDay).first()
            yearTasks = db.taskOccurrenceDao().getByYear(currentYear).first()
        } catch (_: Exception) {}

        val todayTotal = todayTasks.size
        val todayDone = todayTasks.count { it.status == OccurrenceStatus.COMPLETED }

        val monthTasks = yearTasks.filter { 
            it.plannedMonth == currentMonth || (it.plannedDate != null && Instant.ofEpochMilli(it.plannedDate).atZone(zone).monthValue == currentMonth)
        }
        val monthTotal = monthTasks.size
        val monthDone = monthTasks.count { it.status == OccurrenceStatus.COMPLETED }

        val yearTotal = yearTasks.size
        val yearDone = yearTasks.count { it.status == OccurrenceStatus.COMPLETED }
        val overallRate = if (yearTotal > 0) (yearDone * 100 / yearTotal) else 0

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1F22))
                    .cornerRadius(16.dp)
                    .padding(12.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Task Analytics",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color.White)
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = "$overallRate% Completed",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFF00E676))
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Three-tier progress breakdown
                Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // Today Box
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .background(Color(0xFF2A2B2E))
                            .cornerRadius(8.dp)
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Today",
                            style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xFFB0B0B0)))
                        )
                        Text(
                            text = "$todayDone / $todayTotal",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF64B5F6)))
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    // Month Box
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .background(Color(0xFF2A2B2E))
                            .cornerRadius(8.dp)
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Month",
                            style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xFFB0B0B0)))
                        )
                        Text(
                            text = "$monthDone / $monthTotal",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF81C784)))
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    // Year Box
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .background(Color(0xFF2A2B2E))
                            .cornerRadius(8.dp)
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Year",
                            style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xFFB0B0B0)))
                        )
                        Text(
                            text = "$yearDone / $yearTotal",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFFFFB74D)))
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Project breakdown tags
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔵 Work   🟢 Health   🟣 Personal   🟡 Learning",
                        style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xFFC0C0C0)))
                    )
                }
            }
        }
    }
}

/**
 * 4. YearAgendaWidget: Project-Wise Tasks & Upcoming Agenda (WhatsApp / Calendar Style)
 * Displays scheduled tasks grouped with project identity and dates.
 */
class YearAgendaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, glanceId: GlanceId) {
        val today = LocalDate.now()
        val currentYear = today.year
        val currentMonth = today.format(DateTimeFormatter.ofPattern("MMMM"))

        var agendaTasks = emptyList<TaskOccurrence>()
        try {
            val db = YearPlannerDatabase.getInstance(context)
            agendaTasks = db.taskOccurrenceDao().getByYear(currentYear).first()
        } catch (_: Exception) {}

        val upcoming = agendaTasks.filter { it.status != OccurrenceStatus.CANCELLED }.take(4)

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E))
                    .cornerRadius(16.dp)
                    .padding(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "Year Agenda",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color.White)
                            )
                        )
                        Text(
                            text = "$currentMonth $currentYear",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = ColorProvider(Color(0xFFB0B0B0))
                            )
                        )
                    }

                    // + Add Button
                    Box(
                        modifier = GlanceModifier
                            .background(Color(0xFF2196F3))
                            .cornerRadius(12.dp)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .clickable(actionStartActivity<QuickAddActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Add",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color.White)
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Agenda Rows
                if (upcoming.isEmpty()) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                            .clickable(actionStartActivity<MainActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No upcoming tasks in agenda.",
                            style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color(0xFF9E9E9E)))
                        )
                    }
                } else {
                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                            .clickable(actionStartActivity<MainActivity>())
                    ) {
                        upcoming.forEach { task ->
                            val isDone = task.status == OccurrenceStatus.COMPLETED
                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Project pill indicator
                                Box(
                                    modifier = GlanceModifier
                                        .size(8.dp)
                                        .cornerRadius(4.dp)
                                        .background(if (isDone) Color(0xFF4CAF50) else Color(0xFF2196F3))
                                ) {}
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                Text(
                                    text = task.title.ifEmpty { "Planned Task" },
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = ColorProvider(if (isDone) Color(0xFF757575) else Color.White)
                                    ),
                                    maxLines = 1,
                                    modifier = GlanceModifier.defaultWeight()
                                )
                                Text(
                                    text = if (isDone) "Done" else "Pending",
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        color = ColorProvider(if (isDone) Color(0xFF81C784) else Color(0xFFFFB74D))
                                    )
                                )
                            }
                        }
                    }
                }

                // Footer
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View full annual agenda →",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = ColorProvider(Color(0xFF64B5F6))
                        )
                    )
                }
            }
        }
    }
}
