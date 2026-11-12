package com.yearplanner.domain.recurrence

import com.yearplanner.data.model.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.ZoneId
import kotlinx.datetime.Instant
import org.junit.Assert.*
import org.junit.Test

class RecurrenceGeneratorTest {

    @Test
    fun testDailyRecurrence() {
        val series = RecurrenceSeries(
            taskId = "task1",
            frequency = RecurrenceFrequency.DAILY,
            interval = 1,
            anchorDate = Instant.parse("2025-01-01T00:00:00Z").toEpochMilliseconds(),
            zoneId = ZoneId.systemDefault().id
        )
        
        val task = Task(id = "task1", title = "Daily Task")
        val existing = emptyMap<String, TaskOccurrence>()
        
        val occurrences = RecurrenceGenerator.generateOccurrences(series, task, 2025, 1, existing)
        
        assertEquals(31, occurrences.size) // January has 31 days
        assertEquals(LocalDate(2025, 1, 1), occurrences[0].plannedDate?.let { Instant.ofEpochMilliseconds(it).atZone(ZoneId.systemDefault()).toLocalDate() })
        assertEquals(LocalDate(2025, 1, 31), occurrences[30].plannedDate?.let { Instant.ofEpochMilliseconds(it).atZone(ZoneId.systemDefault()).toLocalDate() })
    }

    @Test
    fun testWeeklyRecurrence() {
        val series = RecurrenceSeries(
            taskId = "task1",
            frequency = RecurrenceFrequency.WEEKLY,
            interval = 1,
            anchorDate = Instant.parse("2025-01-01T00:00:00Z").toEpochMilliseconds(), // Wednesday
            zoneId = ZoneId.systemDefault().id
        )
        
        val task = Task(id = "task1", title = "Weekly Task")
        val existing = emptyMap<String, TaskOccurrence>()
        
        val occurrences = RecurrenceGenerator.generateOccurrences(series, task, 2025, 1, existing)
        
        // January 2025 has 5 Wednesdays
        assertEquals(5, occurrences.size)
    }

    @Test
    fun testMonthlyRecurrence() {
        val series = RecurrenceSeries(
            taskId = "task1",
            frequency = RecurrenceFrequency.MONTHLY,
            interval = 1,
            monthDay = 15,
            anchorDate = Instant.parse("2025-01-15T00:00:00Z").toEpochMilliseconds(),
            zoneId = ZoneId.systemDefault().id
        )
        
        val task = Task(id = "task1", title = "Monthly Task")
        val existing = emptyMap<String, TaskOccurrence>()
        
        val occurrences = RecurrenceGenerator.generateOccurrences(series, task, 2025, null, existing)
        
        assertEquals(12, occurrences.size) // 12 months
        occurrences.forEach { occ ->
            val date = Instant.ofEpochMilliseconds(occ.plannedDate!!).atZone(ZoneId.systemDefault()).toLocalDate()
            assertEquals(15, date.dayOfMonth)
        }
    }

    @Test
    fun testYearlyRecurrence() {
        val series = RecurrenceSeries(
            taskId = "task1",
            frequency = RecurrenceFrequency.YEARLY,
            interval = 1,
            anchorDate = Instant.parse("2025-06-15T00:00:00Z").toEpochMilliseconds(),
            zoneId = ZoneId.systemDefault().id
        )
        
        val task = Task(id = "task1", title = "Yearly Task")
        val existing = emptyMap<String, TaskOccurrence>()
        
        val occurrences = RecurrenceGenerator.generateOccurrences(series, task, 2025, null, existing)
        
        assertEquals(1, occurrences.size)
        val date = Instant.ofEpochMilliseconds(occurrences[0].plannedDate!!).atZone(ZoneId.systemDefault()).toLocalDate()
        assertEquals(6, date.monthNumber)
        assertEquals(15, date.dayOfMonth)
    }

    @Test
    fun testWeekdaysRecurrence() {
        val series = RecurrenceSeries(
            taskId = "task1",
            frequency = RecurrenceFrequency.WEEKDAYS,
            interval = 1,
            weekDays = 1 or 2 or 4 or 8 or 16, // Mon-Fri
            anchorDate = Instant.parse("2025-01-01T00:00:00Z").toEpochMilliseconds(), // Wednesday
            zoneId = ZoneId.systemDefault().id
        )
        
        val task = Task(id = "task1", title = "Weekday Task")
        val existing = emptyMap<String, TaskOccurrence>()
        
        val occurrences = RecurrenceGenerator.generateOccurrences(series, task, 2025, 1, existing)
        
        // January 2025 has 23 weekdays (Mon-Fri)
        assertEquals(23, occurrences.size)
        occurrences.forEach { occ ->
            val date = Instant.ofEpochMilliseconds(occ.plannedDate!!).atZone(ZoneId.systemDefault()).toLocalDate()
            val dayOfWeek = date.dayOfWeek // 1=Mon ... 7=Sun
            assertTrue(dayOfWeek >= 1 && dayOfWeek <= 5)
        }
    }

    @Test
    fun testLeapYearFeb29() {
        // 2024 is a leap year
        val series = RecurrenceSeries(
            taskId = "task1",
            frequency = RecurrenceFrequency.YEARLY,
            interval = 1,
            anchorDate = Instant.parse("2024-02-29T00:00:00Z").toEpochMilliseconds(),
            zoneId = ZoneId.systemDefault().id
        )
        
        val task = Task(id = "task1", title = "Leap Year Task")
        val existing = emptyMap<String, TaskOccurrence>()
        
        val occurrences2024 = RecurrenceGenerator.generateOccurrences(series, task, 2024, null, existing)
        val occurrences2025 = RecurrenceGenerator.generateOccurrences(series, task, 2025, null, existing)
        
        assertEquals(1, occurrences2024.size)
        assertEquals(1, occurrences2025.size)
        
        // 2025 is not a leap year, should fall back to Feb 28
        val date2025 = Instant.ofEpochMilliseconds(occurrences2025[0].plannedDate!!).atZone(ZoneId.systemDefault()).toLocalDate()
        assertEquals(2, date2025.monthNumber)
        assertEquals(28, date2025.dayOfMonth)
    }

    @Test
    fun testOccurrenceKeyGeneration() {
        val series = RecurrenceSeries(
            id = "series1",
            taskId = "task1",
            frequency = RecurrenceFrequency.DAILY,
            anchorDate = Instant.parse("2025-01-01T00:00:00Z").toEpochMilliseconds(),
            zoneId = ZoneId.systemDefault().id
        )
        
        val date = LocalDate(2025, 1, 15)
        val key = RecurrenceGenerator.generateOccurrenceKey(series, date)
        
        assertEquals("series1_2025_1_15", key)
    }

    @Test
    fun testValidation() {
        val validSeries = RecurrenceSeries(
            taskId = "task1",
            frequency = RecurrenceFrequency.DAILY,
            interval = 1,
            anchorDate = System.currentTimeMillis()
        )
        
        val result = RecurrenceGenerator.validateRecurrenceRule(validSeries)
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
        
        val invalidSeries = RecurrenceSeries(
            taskId = "task1",
            frequency = RecurrenceFrequency.WEEKDAYS,
            interval = 1,
            weekDays = 0, // No weekdays selected
            anchorDate = System.currentTimeMillis()
        )
        
        val invalidResult = RecurrenceGenerator.validateRecurrenceRule(invalidSeries)
        assertFalse(invalidResult.isValid)
        assertTrue(invalidResult.errors.any { it.contains("weekday") })
    }
}