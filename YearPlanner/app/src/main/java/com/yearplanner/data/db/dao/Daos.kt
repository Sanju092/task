package com.yearplanner.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yearplanner.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: Project): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<Project>)

    @Update
    suspend fun update(project: Project): Int

    @Delete
    suspend fun delete(project: Project): Int

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getById(id: String): Flow<Project?>

    @Query("SELECT * FROM projects WHERE archivedAt IS NULL ORDER BY name")
    fun getAllActive(): Flow<List<Project>>

    @Query("SELECT * FROM projects ORDER BY name")
    fun getAll(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id IN (:ids)")
    fun getByIds(ids: List<String>): Flow<List<Project>>

    @Query("UPDATE projects SET archivedAt = :archivedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, archivedAt: Long, updatedAt: Long): Int

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun hardDelete(id: String): Int
}

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)

    @Update
    suspend fun update(task: Task): Int

    @Query("UPDATE tasks SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long, updatedAt: Long): Int

    @Query("SELECT * FROM tasks WHERE id = :id AND deletedAt IS NULL")
    fun getById(id: String): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getByProject(projectId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id IN (:ids) AND deletedAt IS NULL")
    fun getByIds(ids: List<String>): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE (title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%') AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun searchTasks(query: String): Flow<List<Task>>

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun hardDelete(id: String): Int
}

@Dao
interface RecurrenceSeriesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(series: RecurrenceSeries): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(series: List<RecurrenceSeries>)

    @Update
    suspend fun update(series: RecurrenceSeries): Int

    @Delete
    suspend fun delete(series: RecurrenceSeries): Int

    @Query("SELECT * FROM recurrence_series WHERE id = :id")
    fun getById(id: String): Flow<RecurrenceSeries?>

    @Query("SELECT * FROM recurrence_series WHERE taskId = :taskId")
    fun getByTaskId(taskId: String): Flow<RecurrenceSeries?>

    @Query("SELECT * FROM recurrence_series")
    fun getAll(): Flow<List<RecurrenceSeries>>
}

@Dao
interface TaskOccurrenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(occurrence: TaskOccurrence): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(occurrences: List<TaskOccurrence>)

    @Update
    suspend fun update(occurrence: TaskOccurrence): Int

    @Update
    suspend fun updateAll(occurrences: List<TaskOccurrence>): Int

    @Query("SELECT * FROM task_occurrences WHERE id = :id")
    fun getById(id: String): Flow<TaskOccurrence?>

    @Query("SELECT * FROM task_occurrences WHERE taskId = :taskId ORDER BY plannedDate ASC, deadlineDate ASC")
    fun getByTaskId(taskId: String): Flow<List<TaskOccurrence>>

    @Query("SELECT * FROM task_occurrences WHERE seriesId = :seriesId ORDER BY plannedDate ASC, deadlineDate ASC")
    fun getBySeriesId(seriesId: String): Flow<List<TaskOccurrence>>

    @Query("SELECT * FROM task_occurrences WHERE seriesId = :seriesId AND originalOccurrenceKey = :key")
    fun getBySeriesAndKey(seriesId: String, key: String): Flow<TaskOccurrence?>

    @Query("""
        SELECT * FROM task_occurrences
        WHERE plannedDate >= :startDate AND plannedDate <= :endDate
        AND status != 'CANCELLED'
        ORDER BY plannedDate ASC, sortOrder ASC
    """)
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<TaskOccurrence>>

    @Query("""
        SELECT * FROM task_occurrences
        WHERE (plannedYear = :year OR (deadlineDate IS NOT NULL AND CAST(strftime('%Y', deadlineDate/1000, 'unixepoch') AS INTEGER) = :year))
        AND status != 'CANCELLED'
        ORDER BY
            CASE WHEN plannedDate IS NOT NULL THEN 0 ELSE 1 END,
            plannedDate ASC,
            plannedMonth ASC,
            plannedYear ASC,
            sortOrder ASC
    """)
    fun getByYear(year: Int): Flow<List<TaskOccurrence>>

    @Query("""
        SELECT * FROM task_occurrences
        WHERE plannedYear = :year AND plannedMonth = :month
        AND status != 'CANCELLED'
        ORDER BY
            CASE WHEN plannedDate IS NOT NULL THEN 0 ELSE 1 END,
            plannedDate ASC,
            sortOrder ASC
    """)
    fun getByMonth(year: Int, month: Int): Flow<List<TaskOccurrence>>

    @Query("""
        SELECT * FROM task_occurrences
        WHERE plannedDate = :date
        AND status != 'CANCELLED'
        ORDER BY sortOrder ASC
    """)
    fun getByDate(date: Long): Flow<List<TaskOccurrence>>

    @Query("""
        SELECT * FROM task_occurrences
        WHERE deadlineDate < :now AND status = 'PENDING'
        ORDER BY deadlineDate ASC
    """)
    fun getOverdue(now: Long): Flow<List<TaskOccurrence>>

    @Query("""
        SELECT * FROM task_occurrences
        WHERE (plannedDate = :date OR (plannedDate IS NULL AND plannedMonth = :month AND plannedYear = :year))
        AND status != 'CANCELLED'
        ORDER BY sortOrder ASC
        LIMIT :limit OFFSET :offset
    """)
    fun getForWidget(date: Long, month: Int, year: Int, limit: Int, offset: Int): Flow<List<TaskOccurrence>>

    @Query("""
        SELECT COUNT(*) FROM task_occurrences
        WHERE plannedYear = :year AND plannedMonth = :month AND status = 'COMPLETED'
    """)
    suspend fun getCompletedCountForMonth(year: Int, month: Int): Int

    @Query("""
        SELECT COUNT(*) FROM task_occurrences
        WHERE plannedYear = :year AND plannedMonth = :month AND status != 'CANCELLED'
    """)
    suspend fun getTotalCountForMonth(year: Int, month: Int): Int

    @Query("""
        SELECT COUNT(*) FROM task_occurrences
        WHERE plannedDate = :date AND status = 'COMPLETED'
    """)
    suspend fun getCompletedCountForDate(date: Long): Int

    @Query("""
        SELECT COUNT(*) FROM task_occurrences
        WHERE plannedDate = :date AND status != 'CANCELLED'
    """)
    suspend fun getTotalCountForDate(date: Long): Int

    @Query("SELECT task_occurrences.* FROM task_occurrences INNER JOIN tasks ON task_occurrences.taskId = tasks.id WHERE tasks.projectId = :projectId AND tasks.deletedAt IS NULL ORDER BY task_occurrences.plannedDate ASC")
    fun getByProject(projectId: String): Flow<List<TaskOccurrence>>

    @Query("SELECT * FROM task_occurrences WHERE status = :status ORDER BY plannedDate ASC")
    fun getByStatus(status: OccurrenceStatus): Flow<List<TaskOccurrence>>

    @Query("DELETE FROM task_occurrences WHERE id = :id")
    suspend fun delete(id: String): Int

    @Query("DELETE FROM task_occurrences WHERE seriesId = :seriesId")
    suspend fun deleteBySeriesId(seriesId: String): Int

    // NEW: Get all occurrences for streak calculation
    @Query("SELECT * FROM task_occurrences WHERE status != 'CANCELLED' ORDER BY plannedDate ASC")
    fun getAll(): Flow<List<TaskOccurrence>>
}

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<Reminder>)

    @Update
    suspend fun update(reminder: Reminder): Int

    @Delete
    suspend fun delete(reminder: Reminder): Int

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getById(id: String): Flow<Reminder?>

    @Query("SELECT * FROM reminders WHERE occurrenceId = :occurrenceId")
    fun getByOccurrenceId(occurrenceId: String): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE resolvedInstant <= :now AND enabled = 1 ORDER BY resolvedInstant ASC")
    fun getDueReminders(now: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE enabled = 1 ORDER BY resolvedInstant ASC")
    fun getAllEnabled(): Flow<List<Reminder>>

    @Query("DELETE FROM reminders WHERE occurrenceId = :occurrenceId")
    suspend fun deleteByOccurrenceId(occurrenceId: String): Int

    @Query("UPDATE reminders SET enabled = 0 WHERE occurrenceId = :occurrenceId")
    suspend fun disableByOccurrenceId(occurrenceId: String): Int
}

@Dao
interface WidgetConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: WidgetConfig): Long

    @Update
    suspend fun update(config: WidgetConfig): Int

    @Query("SELECT * FROM widget_configs WHERE appWidgetId = :appWidgetId")
    fun getById(appWidgetId: Int): Flow<WidgetConfig?>

    @Query("SELECT * FROM widget_configs WHERE widgetKind = :kind")
    fun getByKind(kind: WidgetKind): Flow<List<WidgetConfig>>

    @Query("SELECT * FROM widget_configs")
    fun getAll(): Flow<List<WidgetConfig>>

    @Query("DELETE FROM widget_configs WHERE appWidgetId = :appWidgetId")
    suspend fun delete(appWidgetId: Int): Int
}

@Dao
interface UndoActionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: UndoAction): Long

    @Query("SELECT * FROM undo_actions WHERE id = :id")
    fun getById(id: String): Flow<UndoAction?>

    @Query("SELECT * FROM undo_actions WHERE occurrenceId = :occurrenceId AND expiresAt > :now ORDER BY createdAt DESC LIMIT 1")
    fun getLatestForOccurrence(occurrenceId: String, now: Long): Flow<UndoAction?>

    @Query("DELETE FROM undo_actions WHERE id = :id")
    suspend fun delete(id: String): Int

    @Query("DELETE FROM undo_actions WHERE expiresAt <= :now")
    suspend fun cleanupExpired(now: Long): Int
}

@Dao
interface GenerationCoverageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(coverage: GenerationCoverage): Long

    @Query("SELECT * FROM generation_coverage WHERE seriesId = :seriesId")
    fun getBySeriesId(seriesId: String): Flow<GenerationCoverage?>

    @Query("SELECT * FROM generation_coverage")
    fun getAll(): Flow<List<GenerationCoverage>>

    @Query("DELETE FROM generation_coverage WHERE seriesId = :seriesId")
    suspend fun delete(seriesId: String): Int
}