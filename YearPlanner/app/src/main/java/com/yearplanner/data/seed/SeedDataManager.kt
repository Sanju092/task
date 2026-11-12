package com.yearplanner.data.seed

import com.yearplanner.data.model.*
import com.yearplanner.data.repository.TaskRepository
import com.yearplanner.domain.recurrence.RecurrenceGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class SeedDataManager(private val repository: TaskRepository) {

    suspend fun seedIfNeeded() {
        val existingTasks = repository.getAllTasks().first()
        if (existingTasks.isNotEmpty()) return // Already seeded
        
        seedSampleData()
    }

    @Suppress("UNUSED_PARAMETER")
    private suspend fun seedSampleData() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val thisYear = LocalDate.now().year
        
        // Create sample projects
        val workProject = Project(name = "Work", colorToken = "blue")
        val personalProject = Project(name = "Personal", colorToken = "green")
        val healthProject = Project(name = "Health", colorToken = "red")
        val learningProject = Project(name = "Learning", colorToken = "purple")
        
        repository.insertProject(workProject)
        repository.insertProject(personalProject)
        repository.insertProject(healthProject)
        repository.insertProject(learningProject)
        
        // Sample tasks across 12 months
        val sampleTasks = listOf(
            // January
            SeedTask("Plan Q1 goals", "Set objectives for the quarter", workProject.id, 3, thisYear, 1, 5),
            SeedTask("Team meeting prep", "Prepare slides for Monday meeting", workProject.id, 2, thisYear, 1, 8),
            SeedTask("New year workout", "Start fitness routine", healthProject.id, 2, thisYear, 1, 1),
            
            // February
            SeedTask("Valentine's dinner planning", "Book restaurant and buy gift", personalProject.id, 3, thisYear, 2, 10),
            SeedTask("Performance review", "Complete self-assessment", workProject.id, 2, thisYear, 2, 15),
            SeedTask("Learn Kotlin Coroutines", "Complete advanced coroutines course", learningProject.id, 1, thisYear, 2, 20),
            
            // March
            SeedTask("Spring cleaning", "Deep clean the house", personalProject.id, 1, thisYear, 3, 10),
            SeedTask("Project proposal", "Write Q2 project proposal", workProject.id, 3, thisYear, 3, 15),
            SeedTask("Marathon training starts", "Begin 16-week training plan", healthProject.id, 2, thisYear, 3, 1),
            
            // April
            SeedTask("Tax filing deadline", "Submit tax returns", personalProject.id, 3, thisYear, 4, 15),
            SeedTask("Conference attendance", "Attend tech conference", learningProject.id, 2, thisYear, 4, 22),
            SeedTask("Quarterly review", "Review Q1 performance", workProject.id, 2, thisYear, 4, 1),
            
            // May
            SeedTask("Mother's Day gift", "Order flowers and card", personalProject.id, 2, thisYear, 5, 12),
            SeedTask("Health checkup", "Annual physical exam", healthProject.id, 1, thisYear, 5, 20),
            SeedTask("Side project launch", "Deploy personal website", learningProject.id, 2, thisYear, 5, 31),
            
            // June
            SeedTask("Mid-year planning", "Review and adjust yearly goals", workProject.id, 2, thisYear, 6, 15),
            SeedTask("Summer vacation planning", "Book flights and accommodation", personalProject.id, 1, thisYear, 6, 10),
            SeedTask("Half marathon race", "Run 21km race", healthProject.id, 3, thisYear, 6, 22),
            
            // July
            SeedTask("Summer break", "Take time off work", personalProject.id, 1, thisYear, 7, 1),
            SeedTask("Skill certification", "Complete cloud certification", learningProject.id, 3, thisYear, 7, 31),
            SeedTask("Team building event", "Organize summer outing", workProject.id, 2, thisYear, 7, 15),
            
            // August
            SeedTask("Back to school prep", "Buy supplies for kids", personalProject.id, 2, thisYear, 8, 20),
            SeedTask("Performance optimization", "Optimize app startup time", workProject.id, 2, thisYear, 8, 15),
            SeedTask("Yoga retreat", "Weekend wellness retreat", healthProject.id, 1, thisYear, 8, 10),
            
            // September
            SeedTask("Q3 review", "Analyze quarterly metrics", workProject.id, 2, thisYear, 9, 30),
            SeedTask("Learn Rust basics", "Complete Rust book chapters 1-10", learningProject.id, 1, thisYear, 9, 15),
            SeedTask("Annual checkup", "Dentist and eye exam", healthProject.id, 1, thisYear, 9, 10),
            
            // October
            SeedTask("Halloween party planning", "Costumes and decorations", personalProject.id, 1, thisYear, 10, 25),
            SeedTask("Security audit", "Complete annual security review", workProject.id, 3, thisYear, 10, 15),
            SeedTask("Flu shot", "Get seasonal vaccination", healthProject.id, 1, thisYear, 10, 20),
            
            // November
            SeedTask("Thanksgiving dinner", "Host family dinner", personalProject.id, 2, thisYear, 11, 28),
            SeedTask("Black Friday shopping", "Buy planned items", personalProject.id, 1, thisYear, 11, 29),
            SeedTask("Year-end budget review", "Finalize department budget", workProject.id, 2, thisYear, 11, 15),
            
            // December
            SeedTask("Holiday gift shopping", "Complete gift list", personalProject.id, 2, thisYear, 12, 15),
            SeedTask("Year review & planning", "Reflect on year and plan next", workProject.id, 2, thisYear, 12, 31),
            SeedTask("New Year's Eve party", "Plan celebration", personalProject.id, 1, thisYear, 12, 31)
        )
        
        // Insert tasks and occurrences
        for (seedTask in sampleTasks) {
            val task = Task(
                title = seedTask.title,
                notes = seedTask.notes,
                projectId = seedTask.projectId,
                priority = seedTask.priority
            )
            repository.insertTask(task)
            
            val plannedDate = LocalDate.of(seedTask.year, seedTask.month, seedTask.day)
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()
            
            val occurrence = TaskOccurrence(
                taskId = task.id,
                originalOccurrenceKey = "seed_${task.id}",
                planningPrecision = PlanningPrecision.DAY,
                plannedDate = plannedDate,
                deadlineDate = plannedDate,
                zoneId = zone.id,
                sortOrder = 0
            )
            repository.insertOccurrence(occurrence)
        }
        
        // Add a recurring task example
        val weeklyMeeting = Task(title = "Weekly Team Sync", projectId = workProject.id, priority = 2)
        repository.insertTask(weeklyMeeting)
        
        val meetingSeries = RecurrenceSeries(
            taskId = weeklyMeeting.id,
            frequency = RecurrenceFrequency.WEEKLY,
            interval = 1,
            anchorDate = LocalDate.of(thisYear, 1, 6).atStartOfDay(zone).toInstant().toEpochMilli(), // First Monday
            zoneId = zone.id
        )
        repository.insertSeries(meetingSeries)
        
        // Generate occurrences for the year
        val meetingOccurrences = RecurrenceGenerator.generateOccurrences(
            meetingSeries, weeklyMeeting, thisYear, null, emptyMap()
        )
        repository.insertOccurrences(meetingOccurrences)
        
        // Add a monthly recurring task
        val monthlyReport = Task(title = "Monthly Report", projectId = workProject.id, priority = 3)
        repository.insertTask(monthlyReport)
        
        val reportSeries = RecurrenceSeries(
            taskId = monthlyReport.id,
            frequency = RecurrenceFrequency.MONTHLY,
            interval = 1,
            monthDay = 1,
            anchorDate = LocalDate.of(thisYear, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli(),
            zoneId = zone.id
        )
        repository.insertSeries(reportSeries)
        
        val reportOccurrences = RecurrenceGenerator.generateOccurrences(
            reportSeries, monthlyReport, thisYear, null, emptyMap()
        )
        repository.insertOccurrences(reportOccurrences)
    }
}

data class SeedTask(
    val title: String,
    val notes: String,
    val projectId: String?,
    val priority: Int,
    val year: Int,
    val month: Int,
    val day: Int
)