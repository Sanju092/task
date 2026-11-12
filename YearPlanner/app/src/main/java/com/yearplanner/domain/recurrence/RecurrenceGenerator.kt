package com.yearplanner.domain.recurrence

import com.yearplanner.data.model.*
import kotlinx.datetime.*
import kotlinx.datetime.format.DateTimeFormatter
import java.time.YearMonth
import java.util.*

/**
 * Generates task occurrences from recurrence series.
 * Uses stable occurrence keys based on the original recurrence pattern.
 */
object RecurrenceGenerator {

    private const val MAX_GENERATION_YEARS_AHEAD = 5
    private const val MAX_OCCURRENCES_PER_GENERATION = 1000

    /**
     * Generates occurrences for a series up to a target year/month.
     * Returns a list of new occurrences to insert.
     */
    fun generateOccurrences(
        series: RecurrenceSeries,
        task: Task,
        targetYear: Int,
        targetMonth: Int? = null,
        existingOccurrences: Map<String, TaskOccurrence> = emptyMap()
    ): List<TaskOccurrence> {
        val zone = ZoneId.of(series.zoneId)
        val anchor = Instant.ofEpochMilliseconds(series.anchorDate).toLocalDateTime(zone).toLocalDate()
        val endDate = series.endDate?.let { Instant.ofEpochMilliseconds(it).toLocalDateTime(zone).toLocalDate() }
        val maxDate = LocalDate(targetYear, 12, 31)

        val occurrences = mutableListOf<TaskOccurrence>()
        var currentDate = anchor
        var count = 0

        while (shouldContinue(currentDate, targetYear, targetMonth, endDate, series.endCount, count)) {
            if (count >= MAX_OCCURRENCES_PER_GENERATION) break

            val occurrenceKey = generateOccurrenceKey(series, currentDate)
            
            // Skip if already exists (preserves overrides and completions)
            if (!existingOccurrences.containsKey(occurrenceKey)) {
                val occurrence = createOccurrenceFromSeries(
                    series = series,
                    task = task,
                    occurrenceKey = occurrenceKey,
                    date = currentDate,
                    zone = zone
                )
                occurrences.add(occurrence)
            }

            currentDate = advanceDate(currentDate, series)
            count++
        }

        return occurrences
    }

    private fun shouldContinue(
        currentDate: LocalDate,
        targetYear: Int,
        targetMonth: Int?,
        endDate: LocalDate?,
        endCount: Int?,
        currentCount: Int
    ): Boolean {
        // Check year bounds
        if (currentDate.year > targetYear + MAX_GENERATION_YEARS_AHEAD) return false
        
        // Check target month if specified
        if (targetMonth != null && currentDate.year == targetYear && currentDate.monthNumber > targetMonth) return false
        if (targetMonth != null && currentDate.year > targetYear) return false

        // Check end date
        endDate?.let { if (currentDate > it) return false }

        // Check end count
        endCount?.let { if (currentCount >= it) return false }

        return true
    }

    private fun advanceDate(date: LocalDate, series: RecurrenceSeries): LocalDate {
        return when (series.frequency) {
            RecurrenceFrequency.DAILY -> date.plusDays(series.interval)
            RecurrenceFrequency.WEEKLY -> date.plusWeeks(series.interval)
            RecurrenceFrequency.MONTHLY -> addMonths(date, series.interval)
            RecurrenceFrequency.YEARLY -> date.plusYears(series.interval)
            RecurrenceFrequency.WEEKDAYS -> advanceToNextWeekday(date, series.weekDays)
        }
    }

    private fun addMonths(date: LocalDate, months: Int): LocalDate {
        var result = date.plusMonths(months)
        // Handle month-day overflow (e.g., Jan 31 + 1 month = Feb 28/29)
        val yearMonth = YearMonth.of(result.year, result.monthNumber)
        val maxDay = yearMonth.lengthOfMonth()
        if (result.dayOfMonth > maxDay) {
            result = LocalDate(result.year, result.monthNumber, maxDay)
        }
        return result
    }

    private fun advanceToNextWeekday(date: LocalDate, weekDaysBitmask: Int): LocalDate {
        var next = date.plusDays(1)
        val maxDays = 14 // Safety limit
        var daysChecked = 0
        
        while (daysChecked < maxDays) {
            val dayOfWeek = next.dayOfWeek // 1=Mon ... 7=Sun
            val bit = when (dayOfWeek) {
                1 -> 1   // Monday
                2 -> 2   // Tuesday
                3 -> 4   // Wednesday
                4 -> 8   // Thursday
                5 -> 16  // Friday
                6 -> 32  // Saturday
                7 -> 64  // Sunday
                else -> 0
            }
            if (weekDaysBitmask and bit != 0) {
                return next
            }
            next = next.plusDays(1)
            daysChecked++
        }
        return next // Fallback
    }

    private fun generateOccurrenceKey(series: RecurrenceSeries, date: LocalDate): String {
        return "${series.id}_${date.year}_${date.monthNumber}_${date.dayOfMonth}"
    }

    private fun createOccurrenceFromSeries(
        series: RecurrenceSeries,
        task: Task,
        occurrenceKey: String,
        date: LocalDate,
        zone: ZoneId
    ): TaskOccurrence {
        val plannedDateMillis = date.atStartOfDay(zone).toInstant().toEpochMilliseconds()
        
        // Determine planning precision based on frequency
        val planningPrecision = when (series.frequency) {
            RecurrenceFrequency.DAILY, RecurrenceFrequency.WEEKLY, RecurrenceFrequency.WEEKDAYS -> PlanningPrecision.DAY
            RecurrenceFrequency.MONTHLY -> PlanningPrecision.MONTH
            RecurrenceFrequency.YEARLY -> PlanningPrecision.YEAR
        }

        return TaskOccurrence(
            taskId = task.id,
            seriesId = series.id,
            originalOccurrenceKey = occurrenceKey,
            planningPrecision = planningPrecision,
            plannedDate = if (planningPrecision == PlanningPrecision.DAY) plannedDateMillis else null,
            plannedMonth = if (planningPrecision == PlanningPrecision.MONTH) date.monthNumber else null,
            plannedYear = if (planningPrecision == PlanningPrecision.YEAR) date.year else null,
            deadlineDate = plannedDateMillis, // Default deadline = planned date
            zoneId = zone.id,
            timeMode = TimeMode.LOCAL,
            sortOrder = 0
        )
    }

    /**
     * Handles February 29 leap year logic.
     * By default, Feb 29 repeats only in leap years.
     * Alternative: Feb 28 in non-leap years (requires explicit rule).
     */
    fun handleLeapYear(date: LocalDate, series: RecurrenceSeries): LocalDate {
        if (date.monthNumber == 2 && date.dayOfMonth == 29) {
            val nextYear = date.year + series.interval
            if (!LocalDate(nextYear, 2, 29).year.isLeapYear) {
                // Default behavior: skip this year (returns Feb 28 of next year)
                // For "last day of month" rule, this would be handled differently
                return LocalDate(nextYear, 2, 28)
            }
        }
        return date
    }

    /**
     * Generates occurrences for a full year (used for annual view).
     */
    fun generateYearOccurrences(
        series: RecurrenceSeries,
        task: Task,
        year: Int,
        existingOccurrences: Map<String, TaskOccurrence> = emptyMap()
    ): List<TaskOccurrence> {
        return generateOccurrences(series, task, year, null, existingOccurrences)
    }

    /**
     * Validates a recurrence rule for import/creation.
     */
    fun validateRecurrenceRule(series: RecurrenceSeries): ValidationResult {
        val errors = mutableListOf<String>()

        if (series.interval <= 0) {
            errors.add("Interval must be positive")
        }

        when (series.frequency) {
            RecurrenceFrequency.WEEKDAYS -> {
                if (series.weekDays == 0) {
                    errors.add("Weekdays recurrence requires at least one weekday selected")
                }
            }
            RecurrenceFrequency.MONTHLY -> {
                if (series.monthDay !in 1..31) {
                    errors.add("Monthly recurrence requires a valid month day (1-31)")
                }
            }
            RecurrenceFrequency.YEARLY -> {
                // Yearly uses anchor date's month/day
            }
        }

        if (series.endCount != null && series.endCount!! <= 0) {
            errors.add("End count must be positive")
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )
}

/**
 * Handles recurrence rule parsing from string formats (for CSV import).
 */
object RecurrenceParser {

    fun parseRule(ruleString: String): RecurrenceSeries? {
        // Format: "FREQ=WEEKLY;INTERVAL=2;BYDAY=MO,WE,FR;UNTIL=20251231;COUNT=10"
        val parts = ruleString.split(";").map { it.trim() }
        var frequency: RecurrenceFrequency? = null
        var interval = 1
        var weekDays = 0
        var monthDay = 0
        var anchorDate = System.currentTimeMillis()
        var endDate: Long? = null
        var endCount: Int? = null
        var zoneId = ZoneId.systemDefault().id

        for (part in parts) {
            val keyValue = part.split("=")
            if (keyValue.size != 2) continue
            
            val key = keyValue[0].uppercase()
            val value = keyValue[1]
            
            when (key) {
                "FREQ" -> frequency = parseFrequency(value)
                "INTERVAL" -> interval = value.toIntOrNull() ?: 1
                "BYDAY" -> weekDays = parseWeekDays(value)
                "BYMONTHDAY" -> monthDay = value.toIntOrNull() ?: 0
                "DTSTART" -> anchorDate = parseDateTime(value) ?: System.currentTimeMillis()
                "UNTIL" -> endDate = parseDateTime(value)
                "COUNT" -> endCount = value.toIntOrNull()
                "TZID" -> zoneId = value
            }
        }

        return frequency?.let {
            RecurrenceSeries(
                taskId = "", // Will be set by caller
                frequency = it,
                interval = interval,
                weekDays = weekDays,
                monthDay = monthDay,
                anchorDate = anchorDate,
                zoneId = zoneId,
                endDate = endDate,
                endCount = endCount
            )
        }
    }

    private fun parseFrequency(value: String): RecurrenceFrequency? {
        return when (value.uppercase()) {
            "DAILY" -> RecurrenceFrequency.DAILY
            "WEEKLY" -> RecurrenceFrequency.WEEKLY
            "MONTHLY" -> RecurrenceFrequency.MONTHLY
            "YEARLY" -> RecurrenceFrequency.YEARLY
            else -> null
        }
    }

    private fun parseWeekDays(value: String): Int {
        var bitmask = 0
        val days = value.split(",")
        for (day in days) {
            bitmask = bitmask or when (day.uppercase()) {
                "MO", "MON" -> 1
                "TU", "TUE" -> 2
                "WE", "WED" -> 4
                "TH", "THU" -> 8
                "FR", "FRI" -> 16
                "SA", "SAT" -> 32
                "SU", "SUN" -> 64
                else -> 0
            }
        }
        return bitmask
    }

    private fun parseDateTime(value: String): Long? {
        try {
            // Try ISO format first
            return java.time.Instant.parse(value).toEpochMilli()
        } catch (e: Exception) {
            try {
                // Try basic format: 20251231T235959Z
                val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                return java.time.ZonedDateTime.parse(value, formatter).toInstant().toEpochMilli()
            } catch (e2: Exception) {
                return null
            }
        }
    }
}