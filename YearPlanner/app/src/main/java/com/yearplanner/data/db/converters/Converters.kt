package com.yearplanner.data.db.converters

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.ZoneId
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId as JavaZoneId

class Converters {

    @TypeConverter
    fun fromLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it / 86400000) }
    }

    @TypeConverter
    fun toLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()?.times(86400000)
    }

    @TypeConverter
    fun fromLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochMilliseconds(it, ZoneId.currentSystemDefault()) }
    }

    @TypeConverter
    fun toLocalDateTime(dateTime: LocalDateTime?): Long? {
        return dateTime?.toEpochMilliseconds(ZoneId.currentSystemDefault())
    }

    @TypeConverter
    fun fromZoneId(value: String?): ZoneId? {
        return value?.let { ZoneId.of(it) }
    }

    @TypeConverter
    fun toZoneId(zoneId: ZoneId?): String? {
        return zoneId?.id
    }

    @TypeConverter
    fun fromYear(value: Int?): Year? {
        return value?.let { Year.of(it) }
    }

    @TypeConverter
    fun toYear(year: Year?): Int? {
        return year?.value
    }

    @TypeConverter
    fun fromYearMonth(value: Long?): YearMonth? {
        return value?.let { YearMonth.ofEpochMonth(it) }
    }

    @TypeConverter
    fun toYearMonth(yearMonth: YearMonth?): Long? {
        return yearMonth?.toEpochMonth()
    }
}