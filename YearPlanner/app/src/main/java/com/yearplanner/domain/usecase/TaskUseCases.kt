package com.yearplanner.domain.usecase

import com.yearplanner.data.model.*
import com.yearplanner.data.repository.MonthSummary
import com.yearplanner.data.repository.TaskRepository
import com.yearplanner.data.repository.YearSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import java.util.concurrent.Executors

class TaskUseCases(private val repository: TaskRepository) {

    private val ioExecutor = Executors.newSingleThreadExecutor()

    // Project Use Cases
    fun getAllProjects(): Flow<List<Project>> = repository.getAllProjects()
    fun getProject(id: String): Flow<Project?> = repository.getProject(id)
    suspend fun createProject(name: String, colorToken: String = "blue"): Project {
        val project = Project(name = name, colorToken = colorToken)
        repository.insertProject(project)
        return project
    }
    suspend fun updateProject(project: Project) = repository.updateProject(project)
    suspend fun archiveProject(id: String) {
        val project = repository.getProject(id).first()?.copy(archivedAt = System.currentTimeMillis())
        project?.let { repository.updateProject(it) }
    }

    // Task Use Cases
    fun getAllTasks(): Flow<List<Task>> = repository.getAllTasks()
    fun getTask(id: String): Flow<Task?> = repository.getTask(id)
    fun getTasksByProject(projectId: String): Flow<List<Task>> = repository.getTasksByProject(projectId)
    fun searchTasks(query: String): Flow<List<Task>> = repository.searchTasks(query)

    suspend fun createTask(
        title: String,
        notes: String = "",
        projectId: String? = null,
        priority: Int = 0
    ): Task {
        val task = Task(title = title, notes = notes, projectId = projectId, priority = priority)
        repository.insertTask(task)
        return task
    }

    suspend fun updateTask(task: Task) = repository.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteTask(id: String) = repository.deleteTask(id)

    // Recurrence Use Cases
    suspend fun createRecurrenceSeries(
        taskId: String,
        frequency: RecurrenceFrequency,
        interval: Int = 1,
        weekDays: Int = 0,
        monthDay: Int = 0,
        anchorDate: Long,
        zoneId: String = java.time.ZoneId.systemDefault().id,
        endDate: Long? = null,
        endCount: Int? = null
    ): RecurrenceSeries {
        val series = RecurrenceSeries(
            taskId = taskId,
            frequency = frequency,
            interval = interval,
            weekDays = weekDays,
            monthDay = monthDay,
            anchorDate = anchorDate,
            zoneId = zoneId,
            endDate = endDate,
            endCount = endCount
        )
        repository.insertSeries(series)
        return series
    }

    suspend fun updateRecurrenceSeries(series: RecurrenceSeries) = repository.updateSeries(series.copy(updatedAt = System.currentTimeMillis(), ruleVersion = series.ruleVersion + 1))

    // Occurrence Use Cases
    fun getOccurrence(id: String): Flow<TaskOccurrence?> = repository.getOccurrence(id)
    fun getOccurrencesByTask(taskId: String): Flow<List<TaskOccurrence>> = repository.getOccurrencesByTask(taskId)
    fun getOccurrencesBySeries(seriesId: String): Flow<List<TaskOccurrence>> = repository.getOccurrencesBySeries(seriesId)
    fun getOccurrencesByDate(date: Long): Flow<List<TaskOccurrence>> = repository.getOccurrencesByDate(date)
    fun getOccurrencesByDateRange(startDate: Long, endDate: Long): Flow<List<TaskOccurrence>> = repository.getOccurrencesByDateRange(startDate, endDate)
    fun getOccurrencesByYear(year: Int): Flow<List<TaskOccurrence>> = repository.getOccurrencesByYear(year)
    fun getOccurrencesByMonth(year: Int, month: Int): Flow<List<TaskOccurrence>> = repository.getOccurrencesByMonth(year, month)
    fun getOverdueOccurrences(now: Long): Flow<List<TaskOccurrence>> = repository.getOverdueOccurrences(now)
    fun getOccurrencesForWidget(date: Long, month: Int, year: Int, limit: Int, offset: Int): Flow<List<TaskOccurrence>> = repository.getOccurrencesForWidget(date, month, year, limit, offset)
    suspend fun getCompletedCountForMonth(year: Int, month: Int) = repository.getCompletedCountForMonth(year, month)
    suspend fun getTotalCountForMonth(year: Int, month: Int) = repository.getTotalCountForMonth(year, month)
    fun getOccurrencesByProject(projectId: String): Flow<List<TaskOccurrence>> = repository.getOccurrencesByProject(projectId)
    fun getOccurrencesByStatus(status: OccurrenceStatus): Flow<List<TaskOccurrence>> = repository.getOccurrencesByStatus(status)
    fun getTodayTasks(): Flow<List<TaskOccurrence>> = repository.getTodayTasks()
    fun getYearSummary(year: Int): Flow<YearSummary> = repository.getYearSummary(year)
    
    // New: Get all occurrences for streak calculation
    fun getAllOccurrences(): Flow<List<TaskOccurrence>> = repository.getAllOccurrences()

    suspend fun createOccurrence(
        taskId: String,
        seriesId: String? = null,
        originalOccurrenceKey: String,
        planningPrecision: PlanningPrecision,
        plannedDate: Long? = null,
        plannedMonth: Int? = null,
        plannedYear: Int? = null,
        deadlineDate: Long? = null,
        deadlineLocalTime: String? = null,
        zoneId: String = java.time.ZoneId.systemDefault().id,
        timeMode: TimeMode = TimeMode.LOCAL,
        sortOrder: Int = 0
    ): TaskOccurrence {
        val occurrence = TaskOccurrence(
            taskId = taskId,
            seriesId = seriesId,
            originalOccurrenceKey = originalOccurrenceKey,
            planningPrecision = planningPrecision,
            plannedDate = plannedDate,
            plannedMonth = plannedMonth,
            plannedYear = plannedYear,
            deadlineDate = deadlineDate,
            deadlineLocalTime = deadlineLocalTime,
            zoneId = zoneId,
            timeMode = timeMode,
            sortOrder = sortOrder
        )
        repository.insertOccurrence(occurrence)
        return occurrence
    }

    suspend fun createOccurrences(occurrences: List<TaskOccurrence>) = repository.insertOccurrences(occurrences)
    suspend fun updateOccurrence(occurrence: TaskOccurrence) = repository.updateOccurrence(occurrence.copy(updatedAt = System.currentTimeMillis(), version = occurrence.version + 1))
    suspend fun updateOccurrences(occurrences: List<TaskOccurrence>) = repository.updateOccurrences(occurrences.map { it.copy(updatedAt = System.currentTimeMillis(), version = it.version + 1) })
    suspend fun deleteOccurrence(id: String) = repository.deleteOccurrence(id)

    // Completion with undo support
    suspend fun completeOccurrence(occurrenceId: String): Boolean {
        val occurrence = repository.getOccurrence(occurrenceId).first() ?: return false
        return repository.completeOccurrence(occurrenceId, occurrence.version)
    }

    suspend fun toggleOccurrenceCompletion(occurrenceId: String): Boolean {
        val occurrence = repository.getOccurrence(occurrenceId).first() ?: return false
        return if (occurrence.status == OccurrenceStatus.COMPLETED) {
            val updated = occurrence.copy(
                status = OccurrenceStatus.PENDING,
                completedAt = null,
                version = occurrence.version + 1,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateOccurrence(updated)
            true
        } else {
            repository.completeOccurrence(occurrenceId, occurrence.version)
        }
    }

    suspend fun undoCompletion(undoActionId: String): Boolean = repository.undoCompletion(undoActionId)

    suspend fun seedIfNeeded() {
        com.yearplanner.data.seed.SeedDataManager(repository).seedIfNeeded()
    }

    suspend fun seedSampleData() {
        com.yearplanner.data.seed.SeedDataManager(repository).seedSampleData()
    }

    suspend fun clearAllData() {
        val tasks = repository.getAllTasks().first()
        for (task in tasks) {
            repository.deleteTask(task.id)
            val occurrences = repository.getOccurrencesByTask(task.id).first()
            for (occ in occurrences) {
                repository.deleteOccurrence(occ.id)
            }
        }
        val projects = repository.getAllProjects().first()
        for (proj in projects) {
            repository.deleteProject(proj.id)
        }
    }

    // NEW: Snooze occurrence
    suspend fun snoozeOccurrence(occurrenceId: String, minutes: Int): Boolean {
        val occurrence = repository.getOccurrence(occurrenceId).first() ?: return false
        val currentPlanned = occurrence.plannedDate ?: System.currentTimeMillis()
        val newPlanned = currentPlanned + (minutes * 60 * 1000L)
        
        val updated = occurrence.copy(
            plannedDate = newPlanned,
            deadlineDate = if (occurrence.deadlineDate != null) newPlanned else null,
            version = occurrence.version + 1,
            updatedAt = System.currentTimeMillis()
        )
        repository.updateOccurrence(updated)
        
        // Reschedule reminders
        repository.disableRemindersByOccurrence(occurrenceId)
        val reminder = Reminder(
            occurrenceId = occurrenceId,
            requestedLocalDateTime = newPlanned,
            zoneId = java.time.ZoneId.systemDefault().id,
            resolvedInstant = newPlanned,
            precisionMode = ReminderPrecision.INEXACT
        )
        repository.insertReminder(reminder)
        
        return true
    }

    // NEW: Reschedule occurrence
    suspend fun rescheduleOccurrence(occurrenceId: String, target: String): Boolean {
        val occurrence = repository.getOccurrence(occurrenceId).first() ?: return false
        val zone = java.time.ZoneId.systemDefault()
        val now = java.time.LocalDateTime.now(zone)
        val newDate = when (target) {
            "tomorrow" -> now.plusDays(1).withHour(9).withMinute(0)
            "next_week" -> now.plusWeeks(1).with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.MONDAY)).withHour(9).withMinute(0)
            else -> now.plusDays(1).withHour(9).withMinute(0)
        }
        val newMillis = newDate.atZone(zone).toInstant().toEpochMilli()
        
        val updated = occurrence.copy(
            plannedDate = newMillis,
            deadlineDate = if (occurrence.deadlineDate != null) newMillis else null,
            version = occurrence.version + 1,
            updatedAt = System.currentTimeMillis()
        )
        repository.updateOccurrence(updated)
        
        // Reschedule reminders
        repository.disableRemindersByOccurrence(occurrenceId)
        val reminder = Reminder(
            occurrenceId = occurrenceId,
            requestedLocalDateTime = newMillis,
            zoneId = zone.id,
            resolvedInstant = newMillis,
            precisionMode = ReminderPrecision.INEXACT
        )
        repository.insertReminder(reminder)
        
        return true
    }

    // NEW: Streak calculation
    suspend fun calculateStreaks(): Pair<Int, Int> {
        val allTasks = repository.getAllOccurrences().first()
        val completedDates = allTasks
            .filter { it.status == OccurrenceStatus.COMPLETED && it.completedAt != null }
            .map { java.time.Instant.ofEpochMilli(it.completedAt!!).atZone(java.time.ZoneId.systemDefault()).toLocalDate() }
            .distinct()
            .sortedDescending()
        
        if (completedDates.isEmpty()) return Pair(0, 0)
        
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 1
        var prevDate = completedDates.first()
        
        // Current streak (from today backwards)
        val today = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
        if (completedDates.contains(today) || completedDates.contains(today.minusDays(1))) {
            for (date in completedDates) {
                if (date == today || date == prevDate.minusDays(1)) {
                    currentStreak++
                    prevDate = date
                } else {
                    break
                }
            }
        }
        
        // Longest streak
        prevDate = completedDates.first()
        for (i in 1 until completedDates.size) {
            val date = completedDates[i]
            if (date == prevDate.minusDays(1)) {
                tempStreak++
            } else {
                longestStreak = maxOf(longestStreak, tempStreak)
                tempStreak = 1
            }
            prevDate = date
        }
        longestStreak = maxOf(longestStreak, tempStreak)
        
        return Pair(currentStreak, longestStreak)
    }

    // NEW: Export data
    suspend fun exportData(format: String) {
        if (format == "json") {
            repository.exportToJson()
        } else if (format == "csv") {
            repository.exportToCsv()
        }
    }

    // Reminder Use Cases
    suspend fun scheduleReminder(
        occurrenceId: String,
        requestedLocalDateTime: Long,
        zoneId: String,
        precisionMode: ReminderPrecision = ReminderPrecision.INEXACT
    ): Reminder {
        val reminder = Reminder(
            occurrenceId = occurrenceId,
            requestedLocalDateTime = requestedLocalDateTime,
            zoneId = zoneId,
            resolvedInstant = requestedLocalDateTime, // Will be adjusted by scheduler
            precisionMode = precisionMode
        )
        repository.insertReminder(reminder)
        return reminder
    }

    suspend fun cancelRemindersForOccurrence(occurrenceId: String) = repository.disableRemindersByOccurrence(occurrenceId)
    fun getDueReminders(now: Long): Flow<List<Reminder>> = repository.getDueReminders(now)
    fun getAllEnabledReminders(): Flow<List<Reminder>> = repository.getAllEnabledReminders()

    // Widget Config Use Cases
    fun getWidgetConfig(appWidgetId: Int): Flow<WidgetConfig?> = repository.getWidgetConfig(appWidgetId)
    fun getWidgetConfigsByKind(kind: WidgetKind): Flow<List<WidgetConfig>> = repository.getWidgetConfigsByKind(kind)
    suspend fun updateWidgetConfig(config: WidgetConfig) = repository.updateWidgetConfig(config.copy(updatedAt = System.currentTimeMillis()))
    suspend fun createWidgetConfig(config: WidgetConfig) = repository.insertWidgetConfig(config)
    suspend fun deleteWidgetConfig(appWidgetId: Int) = repository.deleteWidgetConfig(appWidgetId)

    // Generation Coverage
    suspend fun updateGenerationCoverage(seriesId: String, year: Int, month: Int) {
        val coverage = GenerationCoverage(
            seriesId = seriesId,
            generatedUpToYear = year,
            generatedUpToMonth = month
        )
        repository.insertCoverage(coverage)
    }

    fun getGenerationCoverage(seriesId: String): Flow<GenerationCoverage?> = repository.getCoverage(seriesId)
}