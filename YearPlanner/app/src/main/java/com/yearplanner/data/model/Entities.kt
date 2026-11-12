package com.yearplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.yearplanner.data.db.converters.Converters
import java.time.ZoneId
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "projects")
data class Project(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorToken: String = "blue",
    val archivedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val projectId: String? = null,
    val priority: Int = 0, // 0=none, 1=low, 2=medium, 3=high
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)

@Serializable
@Entity(tableName = "recurrence_series",
    indices = [androidx.room.Index(value = ["taskId"])])
data class RecurrenceSeries(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val frequency: RecurrenceFrequency,
    val interval: Int = 1,
    val weekDays: Int = 0, // Bitmask for weekdays (1=Mon, 2=Tue, 4=Wed, 8=Thu, 16=Fri, 32=Sat, 64=Sun)
    val monthDay: Int = 0, // 1-31 for monthly, 0=not set
    val anchorDate: Long, // Epoch millis
    val zoneId: String = ZoneId.systemDefault().id,
    val endDate: Long? = null,
    val endCount: Int? = null,
    val ruleVersion: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    WEEKDAYS // Selected weekdays
}

@Serializable
@Entity(tableName = "task_occurrences",
    indices = [
        androidx.room.Index(value = ["taskId"]),
        androidx.room.Index(value = ["seriesId"]),
        androidx.room.Index(value = ["plannedDate"]),
        androidx.room.Index(value = ["deadlineDate"]),
        androidx.room.Index(value = ["status"]),
        androidx.room.Index(value = ["seriesId", "originalOccurrenceKey"], unique = true)
    ])
data class TaskOccurrence(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val seriesId: String? = null,
    val originalOccurrenceKey: String = UUID.randomUUID().toString(), // Stable key: seriesId + date pattern
    val planningPrecision: PlanningPrecision,
    val plannedDate: Long? = null, // For DAY precision
    val plannedMonth: Int? = null, // 1-12 for MONTH precision
    val plannedYear: Int? = null, // For YEAR precision
    val deadlineDate: Long? = null,
    val deadlineLocalTime: String? = null, // HH:mm format
    val zoneId: String = ZoneId.systemDefault().id,
    val timeMode: TimeMode = TimeMode.LOCAL,
    val status: OccurrenceStatus = OccurrenceStatus.PENDING,
    val completedAt: Long? = null,
    val sortOrder: Int = 0,
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val title: String = "",
    val notes: String = ""
)

@Serializable
enum class PlanningPrecision {
    DAY,    // Specific date
    MONTH,  // Month only (backlog)
    YEAR,   // Year only (backlog)
    NONE    // Inbox
}

@Serializable
enum class OccurrenceStatus {
    PENDING,
    COMPLETED,
    SKIPPED,
    CANCELLED
}

@Serializable
enum class TimeMode {
    LOCAL,   // Follow device local time
    FIXED    // Fixed to zoneId
}

@Serializable
@Entity(tableName = "reminders",
    indices = [
        androidx.room.Index(value = ["occurrenceId"]),
        androidx.room.Index(value = ["resolvedInstant"]),
        androidx.room.Index(value = ["enabled"])
    ])
data class Reminder(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val occurrenceId: String,
    val requestedLocalDateTime: Long, // Epoch millis in zoneId
    val zoneId: String,
    val resolvedInstant: Long, // UTC epoch millis
    val precisionMode: ReminderPrecision = ReminderPrecision.INEXACT,
    val enabled: Boolean = true,
    val scheduleGeneration: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class ReminderPrecision {
    INEXACT,
    EXACT,
    EXACT_ALARM
}

@Serializable
@Entity(tableName = "widget_configs",
    primaryKeys = ["appWidgetId"])
data class WidgetConfig(
    val appWidgetId: Int,
    val widgetKind: WidgetKind,
    val selectedYear: Int = java.time.Year.now().value,
    val selectedMonth: Int = java.time.MonthDay.now().monthValue,
    val projectFilter: String? = null,
    val showCompleted: Boolean = false,
    val sortOrder: SortOrder = SortOrder.PLANNED_DATE,
    val theme: WidgetTheme = WidgetTheme.SYSTEM,
    val surfaceOpacity: Float = 0.9f,
    val privacyMode: Boolean = false,
    val visibleBatchStart: Int = 0,
    val visibleBatchSize: Int = 20,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
enum class WidgetKind {
    TODAY,
    YEAR_OVERVIEW,
    YEAR_AGENDA,
    ANALYTICS;

    val displayName: String
        get() = when (this) {
            TODAY -> "Today"
            YEAR_OVERVIEW -> "Year Overview"
            YEAR_AGENDA -> "Year Agenda"
            ANALYTICS -> "Analytics"
        }
}

@Serializable
enum class SortOrder {
    PLANNED_DATE,
    DEADLINE_DATE,
    PRIORITY,
    CREATED_AT
}

@Serializable
enum class WidgetTheme {
    LIGHT,
    DARK,
    SYSTEM
}

@Serializable
@Entity(tableName = "undo_actions",
    indices = [androidx.room.Index(value = ["expiresAt"])])
data class UndoAction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val occurrenceId: String,
    val expectedVersion: Int,
    val previousStatus: OccurrenceStatus,
    val previousCompletedAt: Long? = null,
    val expiresAt: Long = System.currentTimeMillis() + 86400000, // 24 hours
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "generation_coverage")
data class GenerationCoverage(
    @PrimaryKey val seriesId: String,
    val generatedUpToYear: Int,
    val generatedUpToMonth: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val projects: List<Project> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val recurrenceSeries: List<RecurrenceSeries> = emptyList(),
    val taskOccurrences: List<TaskOccurrence> = emptyList(),
    val reminders: List<Reminder> = emptyList()
)