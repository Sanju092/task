package com.yearplanner.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.yearplanner.data.model.*
import com.yearplanner.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.readBytes
import kotlinx.io.core.writeBytes
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class BackupManager(private val context: Context, private val repository: TaskRepository) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    /**
     * Exports all data to a JSON file.
     */
    suspend fun exportToJson(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val projects = repository.getAllProjects().first()
            val tasks = repository.getAllTasks().first()
            
            // Get all series
            val allSeries = mutableListOf<RecurrenceSeries>()
            for (task in tasks) {
                val series = repository.getSeriesByTaskId(task.id).first()
                series?.let { allSeries.add(it) }
            }
            
            // Get all occurrences
            val allOccurrences = mutableListOf<TaskOccurrence>()
            for (task in tasks) {
                val occurrences = repository.getOccurrencesByTask(task.id).first()
                allOccurrences.addAll(occurrences)
            }
            
            // Get all reminders
            val allReminders = repository.getAllEnabledReminders().first()
            
            val backupData = BackupData(
                version = 1,
                exportedAt = System.currentTimeMillis(),
                projects = projects,
                tasks = tasks,
                recurrenceSeries = allSeries,
                taskOccurrences = allOccurrences,
                reminders = allReminders
            )
            
            val jsonString = json.encodeToString(backupData)
            
            val outputStream = context.contentResolver.openOutputStream(uri)
            val writer = OutputStreamWriter(outputStream!!)
            writer.write(jsonString)
            writer.close()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BackupManager", "Export failed", e)
            Result.failure(e)
        }
    }

    /**
     * Imports data from a JSON file.
     */
    suspend fun importFromJson(uri: Uri): Result<ImportResult> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream!!))
            val jsonString = reader.readText()
            reader.close()
            
            val backupData = json.decodeFromString<BackupData>(jsonString)
            
            return@withContext restoreBackupData(backupData)
        } catch (e: Exception) {
            Log.e("BackupManager", "Import failed", e)
            Result.failure(e)
        }
    }

    /**
     * Restores backup data to the database.
     */
    private suspend fun restoreBackupData(backupData: BackupData): Result<ImportResult> {
        var importedProjects = 0
        var importedTasks = 0
        var importedSeries = 0
        var importedOccurrences = 0
        var importedReminders = 0
        val errors = mutableListOf<String>()

        try {
            // Restore projects
            for (project in backupData.projects) {
                try {
                    repository.insertProject(project)
                    importedProjects++
                } catch (e: Exception) {
                    errors.add("Project ${project.name}: ${e.message}")
                }
            }

            // Restore tasks
            for (task in backupData.tasks) {
                try {
                    repository.insertTask(task)
                    importedTasks++
                } catch (e: Exception) {
                    errors.add("Task ${task.title}: ${e.message}")
                }
            }

            // Restore recurrence series
            for (series in backupData.recurrenceSeries) {
                try {
                    repository.insertSeries(series)
                    importedSeries++
                } catch (e: Exception) {
                    errors.add("Series ${series.id}: ${e.message}")
                }
            }

            // Restore occurrences
            for (occurrence in backupData.taskOccurrences) {
                try {
                    repository.insertOccurrence(occurrence)
                    importedOccurrences++
                } catch (e: Exception) {
                    errors.add("Occurrence ${occurrence.id}: ${e.message}")
                }
            }

            // Restore reminders
            for (reminder in backupData.reminders) {
                try {
                    repository.insertReminder(reminder)
                    importedReminders++
                } catch (e: Exception) {
                    errors.add("Reminder ${reminder.id}: ${e.message}")
                }
            }

            Result.success(ImportResult(
                projects = importedProjects,
                tasks = importedTasks,
                series = importedSeries,
                occurrences = importedOccurrences,
                reminders = importedReminders,
                errors = errors
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exports tasks to CSV format.
     */
    suspend fun exportToCsv(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val tasks = repository.getAllTasks().first()
            val allOccurrences = mutableListOf<TaskOccurrence>()
            
            for (task in tasks) {
                val occurrences = repository.getOccurrencesByTask(task.id).first()
                allOccurrences.addAll(occurrences)
            }
            
            val outputStream = context.contentResolver.openOutputStream(uri)
            val writer = OutputStreamWriter(outputStream!!)
            
            // CSV Header
            writer.write("title,project,planning_precision,planned_date,planned_month,planned_year,deadline_date,deadline_time,zone_id,priority,repeat_rule\n")
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            
            for (occurrence in allOccurrences) {
                val task = tasks.find { it.id == occurrence.taskId }
                val project = if (task?.projectId != null) {
                    repository.getProject(task.projectId!!).first()?.name ?: ""
                } else ""
                
                val plannedDateStr = occurrence.plannedDate?.let { dateFormat.format(java.util.Date(it)) } ?: ""
                val deadlineDateStr = occurrence.deadlineDate?.let { dateFormat.format(java.util.Date(it)) } ?: ""
                val deadlineTimeStr = occurrence.deadlineLocalTime ?: ""
                
                val repeatRule = if (occurrence.seriesId != null) {
                    val series = repository.getSeriesByTaskId(occurrence.taskId!!).first()
                    series?.let { formatRecurrenceRule(it) } ?: ""
                } else ""
                
                val line = "${escapeCsv(task?.title ?: occurrence.id)}," +
                    "${escapeCsv(project)}," +
                    "${occurrence.planningPrecision.name}," +
                    "$plannedDateStr," +
                    "${occurrence.plannedMonth ?: ""}," +
                    "${occurrence.plannedYear ?: ""}," +
                    "$deadlineDateStr," +
                    "$deadlineTimeStr," +
                    "${occurrence.zoneId}," +
                    "${occurrence.priority}," +
                    "${escapeCsv(repeatRule)}\n"
                
                writer.write(line)
            }
            
            writer.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BackupManager", "CSV Export failed", e)
            Result.failure(e)
        }
    }

    /**
     * Imports tasks from CSV format.
     */
    suspend fun importFromCsv(uri: Uri): Result<ImportResult> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream!!))
            
            val header = reader.readLine()?.split(",") ?: return@withContext Result.failure(Exception("Empty CSV"))
            val columnMap = header.map { it.trim().lowercase() }.withIndex().associate { it.value to it.index }
            
            var importedTasks = 0
            var importedOccurrences = 0
            val errors = mutableListOf<String>()
            var lineNumber = 1
            
            reader.forEachLine { line ->
                lineNumber++
                val columns = parseCsvLine(line)
                if (columns.size < header.size) {
                    errors.add("Line $lineNumber: Column count mismatch")
                    return@forEachLine
                }
                
                try {
                    val title = columns[columnMap["title"] ?: 0]
                    val projectName = columns[columnMap["project"] ?: 1]
                    val planningPrecisionStr = columns[columnMap["planning_precision"] ?: 2]
                    val plannedDateStr = columns[columnMap["planned_date"] ?: 3]
                    val plannedMonthStr = columns[columnMap["planned_month"] ?: 4]
                    val plannedYearStr = columns[columnMap["planned_year"] ?: 5]
                    val deadlineDateStr = columns[columnMap["deadline_date"] ?: 6]
                    val deadlineTimeStr = columns[columnMap["deadline_time"] ?: 7]
                    val zoneId = columns[columnMap["zone_id"] ?: 8].ifBlank { java.time.ZoneId.systemDefault().id }
                    val priorityStr = columns[columnMap["priority"] ?: 9]
                    val repeatRule = columns[columnMap["repeat_rule"] ?: 10]
                    
                    val planningPrecision = PlanningPrecision.valueOf(planningPrecisionStr.uppercase())
                    val priority = priorityStr.toIntOrNull() ?: 0
                    
                    // Find or create project
                    var projectId: String? = null
                    if (projectName.isNotBlank()) {
                        val existingProject = repository.getAllProjects().first().find { it.name == projectName }
                        projectId = existingProject?.id ?: run {
                            val newProject = Project(name = projectName)
                            repository.insertProject(newProject)
                            newProject.id
                        }
                    }
                    
                    // Create task
                    val task = Task(
                        title = title,
                        projectId = projectId,
                        priority = priority
                    )
                    repository.insertTask(task)
                    importedTasks++
                    
                    // Parse dates
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val plannedDate = plannedDateStr.ifBlank { null }?.let { dateFormat.parse(it).time }
                    val deadlineDate = deadlineDateStr.ifBlank { null }?.let { dateFormat.parse(it).time }
                    
                    val plannedMonth = plannedMonthStr.toIntOrNull()
                    val plannedYear = plannedYearStr.toIntOrNull()
                    
                    // Create occurrence
                    val occurrence = TaskOccurrence(
                        taskId = task.id,
                        planningPrecision = planningPrecision,
                        plannedDate = plannedDate,
                        plannedMonth = plannedMonth,
                        plannedYear = plannedYear,
                        deadlineDate = deadlineDate,
                        deadlineLocalTime = deadlineTimeStr.ifBlank { null },
                        zoneId = zoneId
                    )
                    repository.insertOccurrence(occurrence)
                    importedOccurrences++
                    
                    // Parse recurrence rule if present
                    if (repeatRule.isNotBlank()) {
                        val series = com.yearplanner.domain.recurrence.RecurrenceParser.parseRule(repeatRule)?.copy(taskId = task.id)
                        series?.let {
                            repository.insertSeries(it)
                        }
                    }
                    
                } catch (e: Exception) {
                    errors.add("Line $lineNumber: ${e.message}")
                }
            }
            
            reader.close()
            
            Result.success(ImportResult(
                tasks = importedTasks,
                occurrences = importedOccurrences,
                errors = errors
            ))
        } catch (e: Exception) {
            Log.e("BackupManager", "CSV Import failed", e)
            Result.failure(e)
        }
    }

    private fun formatRecurrenceRule(series: RecurrenceSeries): String {
        val parts = mutableListOf<String>()
        parts.add("FREQ=${series.frequency.name}")
        if (series.interval > 1) parts.add("INTERVAL=${series.interval}")
        if (series.weekDays != 0) {
            val days = mutableListOf<String>()
            if (series.weekDays and 1 != 0) days.add("MO")
            if (series.weekDays and 2 != 0) days.add("TU")
            if (series.weekDays and 4 != 0) days.add("WE")
            if (series.weekDays and 8 != 0) days.add("TH")
            if (series.weekDays and 16 != 0) days.add("FR")
            if (series.weekDays and 32 != 0) days.add("SA")
            if (series.weekDays and 64 != 0) days.add("SU")
            parts.add("BYDAY=${days.joinToString(",")}")
        }
        if (series.monthDay != 0) parts.add("BYMONTHDAY=${series.monthDay}")
        if (series.endDate != null) {
            val dateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.getDefault())
            parts.add("UNTIL=${dateFormat.format(java.util.Date(series.endDate!!))}")
        }
        if (series.endCount != null) parts.add("COUNT=${series.endCount}")
        parts.add("TZID=${series.zoneId}")
        return parts.joinToString(";")
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    sb.append('"')
                    i += 2
                }
                c == '"' -> {
                    inQuotes = !inQuotes
                    i++
                }
                c == ',' && !inQuotes -> {
                    result.add(sb.toString())
                    sb.setLength(0)
                    i++
                }
                else -> {
                    sb.append(c)
                    i++
                }
            }
        }
        result.add(sb.toString())
        return result
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }
}

data class ImportResult(
    val projects: Int = 0,
    val tasks: Int = 0,
    val series: Int = 0,
    val occurrences: Int = 0,
    val reminders: Int = 0,
    val errors: List<String> = emptyList()
) {
    val totalImported: Int = projects + tasks + series + occurrences + reminders
    val hasErrors: Boolean = errors.isNotEmpty()
}