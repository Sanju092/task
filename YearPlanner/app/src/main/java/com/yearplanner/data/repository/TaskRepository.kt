package com.yearplanner.data.repository

import androidx.room.withTransaction
import com.yearplanner.data.db.YearPlannerDatabase
import com.yearplanner.data.db.dao.*
import com.yearplanner.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

class TaskRepository(private val database: YearPlannerDatabase) {

    private val projectDao = database.projectDao()
    private val taskDao = database.taskDao()
    private val seriesDao = database.recurrenceSeriesDao()
    private val occurrenceDao = database.taskOccurrenceDao()
    private val reminderDao = database.reminderDao()
    private val widgetConfigDao = database.widgetConfigDao()
    private val undoActionDao = database.undoActionDao()
    private val coverageDao = database.generationCoverageDao()

    private val ioExecutor = Executors.newSingleThreadExecutor()

    // Projects
    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllActive()
    fun getProject(id: String): Flow<Project?> = projectDao.getById(id)
    suspend fun insertProject(project: Project) = projectDao.insert(project)
    suspend fun updateProject(project: Project) = projectDao.update(project)
    suspend fun deleteProject(id: String) = projectDao.softDelete(id, System.currentTimeMillis(), System.currentTimeMillis())

    // Tasks
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAll()
    fun getTask(id: String): Flow<Task?> = taskDao.getById(id)
    fun getTasksByProject(projectId: String): Flow<List<Task>> = taskDao.getByProject(projectId)
    suspend fun insertTask(task: Task) = taskDao.insert(task)
    suspend fun updateTask(task: Task) = taskDao.update(task)
    suspend fun deleteTask(id: String) = taskDao.softDelete(id, System.currentTimeMillis(), System.currentTimeMillis())
    fun searchTasks(query: String): Flow<List<Task>> = taskDao.searchTasks(query)

    // Recurrence Series
    fun getSeriesByTaskId(taskId: String): Flow<RecurrenceSeries?> = seriesDao.getByTaskId(taskId)
    fun getSeries(id: String): Flow<RecurrenceSeries?> = seriesDao.getById(id)
    suspend fun insertSeries(series: RecurrenceSeries) = seriesDao.insert(series)
    suspend fun updateSeries(series: RecurrenceSeries) = seriesDao.update(series)
    suspend fun deleteSeries(id: String) = seriesDao.delete(RecurrenceSeries(id = id, taskId = "", frequency = RecurrenceFrequency.DAILY, anchorDate = 0))

    // Task Occurrences
    fun getOccurrence(id: String): Flow<TaskOccurrence?> = occurrenceDao.getById(id)
    fun getOccurrencesByTask(taskId: String): Flow<List<TaskOccurrence>> = occurrenceDao.getByTaskId(taskId)
    fun getOccurrencesBySeries(seriesId: String): Flow<List<TaskOccurrence>> = occurrenceDao.getBySeriesId(seriesId)
    fun getOccurrencesByDateRange(startDate: Long, endDate: Long): Flow<List<TaskOccurrence>> = occurrenceDao.getByDateRange(startDate, endDate)
    fun getOccurrencesByYear(year: Int): Flow<List<TaskOccurrence>> = occurrenceDao.getByYear(year)
    fun getOccurrencesByMonth(year: Int, month: Int): Flow<List<TaskOccurrence>> = occurrenceDao.getByMonth(year, month)
    fun getOccurrencesByDate(date: Long): Flow<List<TaskOccurrence>> = occurrenceDao.getByDate(date)
    fun getOverdueOccurrences(now: Long): Flow<List<TaskOccurrence>> = occurrenceDao.getOverdue(now)
    fun getOccurrencesForWidget(date: Long, month: Int, year: Int, limit: Int, offset: Int): Flow<List<TaskOccurrence>> = occurrenceDao.getForWidget(date, month, year, limit, offset)
    suspend fun getCompletedCountForMonth(year: Int, month: Int) = occurrenceDao.getCompletedCountForMonth(year, month)
    suspend fun getTotalCountForMonth(year: Int, month: Int) = occurrenceDao.getTotalCountForMonth(year, month)
    suspend fun getCompletedCountForDate(date: Long) = occurrenceDao.getCompletedCountForDate(date)
    suspend fun getTotalCountForDate(date: Long) = occurrenceDao.getTotalCountForDate(date)
    fun getOccurrencesByProject(projectId: String): Flow<List<TaskOccurrence>> = occurrenceDao.getByProject(projectId)
    fun getOccurrencesByStatus(status: OccurrenceStatus): Flow<List<TaskOccurrence>> = occurrenceDao.getByStatus(status)
    
    // NEW: Get all occurrences for streak calculation
    fun getAllOccurrences(): Flow<List<TaskOccurrence>> = occurrenceDao.getAll()

    suspend fun insertOccurrence(occurrence: TaskOccurrence) = occurrenceDao.insert(occurrence)
    suspend fun insertOccurrences(occurrences: List<TaskOccurrence>) = occurrenceDao.insertAll(occurrences)
    suspend fun updateOccurrence(occurrence: TaskOccurrence) = occurrenceDao.update(occurrence)
    suspend fun updateOccurrences(occurrences: List<TaskOccurrence>) = occurrenceDao.updateAll(occurrences)
    suspend fun deleteOccurrence(id: String) = occurrenceDao.delete(id)
    suspend fun deleteOccurrencesBySeries(seriesId: String) = occurrenceDao.deleteBySeriesId(seriesId)

    // Reminders
    suspend fun insertReminder(reminder: Reminder) = reminderDao.insert(reminder)
    suspend fun insertReminders(reminders: List<Reminder>) = reminderDao.insertAll(reminders)
    suspend fun updateReminder(reminder: Reminder) = reminderDao.update(reminder)
    suspend fun deleteReminder(id: String) {
        reminderDao.getById(id).first()?.let { reminderDao.delete(it) }
    }
    suspend fun deleteRemindersByOccurrence(occurrenceId: String) = reminderDao.deleteByOccurrenceId(occurrenceId)
    suspend fun disableRemindersByOccurrence(occurrenceId: String) = reminderDao.disableByOccurrenceId(occurrenceId)
    fun getDueReminders(now: Long): Flow<List<Reminder>> = reminderDao.getDueReminders(now)
    fun getAllEnabledReminders(): Flow<List<Reminder>> = reminderDao.getAllEnabled()

    // Widget Configs
    fun getWidgetConfig(appWidgetId: Int): Flow<WidgetConfig?> = widgetConfigDao.getById(appWidgetId)
    fun getWidgetConfigsByKind(kind: WidgetKind): Flow<List<WidgetConfig>> = widgetConfigDao.getByKind(kind)
    fun getAllWidgetConfigs(): Flow<List<WidgetConfig>> = widgetConfigDao.getAll()
    suspend fun insertWidgetConfig(config: WidgetConfig) = widgetConfigDao.insert(config)
    suspend fun updateWidgetConfig(config: WidgetConfig) = widgetConfigDao.update(config)
    suspend fun deleteWidgetConfig(appWidgetId: Int) = widgetConfigDao.delete(appWidgetId)

    // Undo Actions
    suspend fun insertUndoAction(action: UndoAction) = undoActionDao.insert(action)
    fun getLatestUndoAction(occurrenceId: String): Flow<UndoAction?> = undoActionDao.getLatestForOccurrence(occurrenceId, System.currentTimeMillis())
    suspend fun deleteUndoAction(id: String) = undoActionDao.delete(id)
    suspend fun cleanupExpiredUndoActions() = undoActionDao.cleanupExpired(System.currentTimeMillis())

    // Generation Coverage
    suspend fun insertCoverage(coverage: GenerationCoverage) = coverageDao.insert(coverage)
    fun getCoverage(seriesId: String): Flow<GenerationCoverage?> = coverageDao.getBySeriesId(seriesId)

    // Combined queries for UI
    fun getTodayTasks(): Flow<List<TaskOccurrence>> {
        val today = java.time.LocalDate.now()
        val startOfDay = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        return occurrenceDao.getByDateRange(startOfDay, endOfDay)
    }

    fun getYearSummary(year: Int): Flow<YearSummary> {
        return combine(
            occurrenceDao.getByYear(year),
            projectDao.getAllActive()
        ) { occurrences, projects ->
            val projectMap = projects.associateBy { it.id }
            val monthData = mutableMapOf<Int, MonthSummary>()
            
            for (month in 1..12) {
                val monthOccurrences = occurrences.filter { occ ->
                    (occ.plannedYear == year && occ.plannedMonth == month) ||
                    (occ.deadlineDate != null && java.time.Instant.ofEpochMilli(occ.deadlineDate!!).atZone(java.time.ZoneId.systemDefault()).monthValue == month &&
                     java.time.Instant.ofEpochMilli(occ.deadlineDate!!).atZone(java.time.ZoneId.systemDefault()).year == year)
                }.distinctBy { it.id }
                
                val completed = monthOccurrences.count { it.status == OccurrenceStatus.COMPLETED }
                val total = monthOccurrences.size
                
                monthData[month] = MonthSummary(
                    month = month,
                    year = year,
                    totalTasks = total,
                    completedTasks = completed,
                    tasks = monthOccurrences
                )
            }
            
            YearSummary(year = year, months = monthData.toMap())
        }
    }

    // NEW: Export to JSON
    suspend fun exportToJson(): Result<android.net.Uri> = withContext(ioExecutor.asCoroutineDispatcher()) {
        try {
            val projects = projectDao.getAll().first()
            val tasks = taskDao.getAll().first()
            
            // Get all series
            val allSeries = mutableListOf<RecurrenceSeries>()
            for (task in tasks) {
                val series = seriesDao.getByTaskId(task.id).first()
                series?.let { allSeries.add(it) }
            }
            
            // Get all occurrences
            val allOccurrences = occurrenceDao.getAll().first()
            
            // Get all reminders
            val allReminders = reminderDao.getAllEnabled().first()
            
            val backupData = BackupData(
                version = 1,
                exportedAt = System.currentTimeMillis(),
                projects = projects,
                tasks = tasks,
                recurrenceSeries = allSeries,
                taskOccurrences = allOccurrences,
                reminders = allReminders
            )
            
            // Use system document picker for user-chosen export location
            // This would be called from the UI with a proper URI
            // For now, return a placeholder
            Result.success(android.net.Uri.EMPTY)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NEW: Export to CSV
    suspend fun exportToCsv(): Result<android.net.Uri> = withContext(ioExecutor.asCoroutineDispatcher()) {
        try {
            // Similar to JSON export but CSV format
            Result.success(android.net.Uri.EMPTY)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Transactional operations
    suspend fun completeOccurrence(occurrenceId: String, expectedVersion: Int): Boolean {
        return database.withTransaction {
            val occurrence = occurrenceDao.getById(occurrenceId).first()
                ?: return@withTransaction false
            
            if (occurrence.version != expectedVersion) {
                return@withTransaction false
            }
            
            if (occurrence.status == OccurrenceStatus.COMPLETED) {
                return@withTransaction true // Idempotent
            }
            
            val previousStatus = occurrence.status
            val previousCompletedAt = occurrence.completedAt
            
            val updatedOccurrence = occurrence.copy(
                status = OccurrenceStatus.COMPLETED,
                completedAt = System.currentTimeMillis(),
                version = occurrence.version + 1,
                updatedAt = System.currentTimeMillis()
            )
            
            occurrenceDao.update(updatedOccurrence)
            
            // Create undo action
            val undoAction = UndoAction(
                occurrenceId = occurrenceId,
                expectedVersion = occurrence.version,
                previousStatus = previousStatus,
                previousCompletedAt = previousCompletedAt
            )
            undoActionDao.insert(undoAction)
            
            // Disable reminders for this occurrence
            reminderDao.disableByOccurrenceId(occurrenceId)
            
            true
        }
    }

    suspend fun undoCompletion(undoActionId: String): Boolean {
        return database.withTransaction {
            val undoAction = undoActionDao.getById(undoActionId).first()
                ?: return@withTransaction false
            
            val occurrence = occurrenceDao.getById(undoAction.occurrenceId).first()
                ?: return@withTransaction false
            
            // Check if the occurrence is still at the expected version (hasn't been modified since)
            if (occurrence.version != undoAction.expectedVersion + 1) {
                return@withTransaction false
            }
            
            val restoredOccurrence = occurrence.copy(
                status = undoAction.previousStatus,
                completedAt = undoAction.previousCompletedAt,
                version = occurrence.version + 1,
                updatedAt = System.currentTimeMillis()
            )
            
            occurrenceDao.update(restoredOccurrence)
            undoActionDao.delete(undoActionId)
            
            true
        }
    }

    companion object {
        @Volatile
        private var instance: TaskRepository? = null

        fun getInstance(context: android.content.Context): TaskRepository {
            return instance ?: synchronized(this) {
                val db = com.yearplanner.data.db.YearPlannerDatabase.getInstance(context)
                val repo = TaskRepository(db)
                instance = repo
                repo
            }
        }
    }
}

data class MonthSummary(
    val month: Int,
    val year: Int,
    val totalTasks: Int,
    val completedTasks: Int,
    val tasks: List<TaskOccurrence>
)

data class YearSummary(
    val year: Int,
    val months: Map<Int, MonthSummary>
)